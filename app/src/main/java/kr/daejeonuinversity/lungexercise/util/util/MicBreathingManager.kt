package kr.daejeonuinversity.lungexercise.util.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 휴대폰 마이크로 호기(날숨)를 감지해 MaskBluetoothManager.BreathingEventListener 와 동일한
 * 이벤트(onExhaleStart / onExhaleEnd)를 발행합니다.
 *
 * - RMS + ZCR + Crest Factor + 저주파 에너지 비율(Biquad IIR) 조합으로 날숨 판별
 * - FVC / FEV1 / Ratio / Pressure 는 마스크와 동일한 보정 로직으로 계산
 */
object MicBreathingManager {

    // ─── AudioRecord 설정 ─────────────────────────────────────────────────
    private const val SAMPLE_RATE    = 16000                           // Hz
    private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private const val AUDIO_FORMAT   = AudioFormat.ENCODING_PCM_16BIT

    // ─── 진폭 → 유량 변환 상수 (마스크와 동일 체계) ──────────────────────
    private const val MAX_AMPLITUDE  = 8000.0   // 입으로 불 때 기대 최대 RMS
    private const val MAX_FLOW_LPS   = 15.0     // L/s 최대 유량 (마스크 동일)
    private const val FVC_CORRECTION = 0.55     // 마스크와 동일한 볼륨 보정 계수
    private const val RHO            = 1.18     // 공기 밀도 kg/m³

    // ─── 호기 감지 임계값 ────────────────────────────────────────────────
    private const val EXHALE_RMS_THRESHOLD = 700.0   // 시작 감지: 이 RMS 이상 = 날숨
    private const val EXHALE_RMS_KEEP      = 350.0   // 유지 감지: 이 RMS 미만이면 종료 후보
    private const val START_CONSECUTIVE    = 3        // 연속 N 버퍼 초과 → 호기 시작
    private const val END_CONSECUTIVE      = 12       // 연속 N 버퍼 미만 → 호기 종료

    // ─── 4-Feature 분류기 상수 ────────────────────────────────────────────
    private const val ZCR_THRESHOLD      = 0.15   // ZCR < 이 값 → 날숨 (저주파 연속신호)
    private const val CREST_THRESHOLD    = 12.0   // Crest Factor < 이 값 → 날숨 (지속 신호)
    private const val LOW_FREQ_THRESHOLD = 0.55   // 저주파 에너지 비율 >= 이 값 → 날숨
    private const val SCORE_THRESHOLD    = 0.70   // 가중 점수 >= 이 값 → 날숨 판정
    private const val WEIGHT_ZCR         = 0.40
    private const val WEIGHT_CREST       = 0.30
    private const val WEIGHT_LOW_FREQ    = 0.30

    // ─── 주기성(Periodicity) 검사 상수 ───────────────────────────────────
    // 말소리 성대 진동 주파수: 80~250Hz → 16kHz 기준 lag 64~200 샘플
    private const val PITCH_LAG_MIN      = 64     // ~250Hz
    private const val PITCH_LAG_MAX      = 200    // ~80Hz
    private const val PITCH_LAG_STEP     = 4      // 성능: 4샘플 간격으로 검사
    private const val PERIODICITY_THRESHOLD = 0.35 // NACF 이 값 초과 → 주기신호 → 말소리

    // ─── Biquad LP 필터 계수 (Butterworth 2차, fc=1000 Hz, fs=16000 Hz) ──
    private const val BQ_B0 =  0.02008337
    private const val BQ_B1 =  0.04016673
    private const val BQ_B2 =  0.02008337
    private const val BQ_A1 = -1.56101808
    private const val BQ_A2 =  0.64135154

    // ─── 내부 상태 ────────────────────────────────────────────────────────
    private var audioRecord: AudioRecord? = null
    @Volatile private var isRunning = false
    private var recordThread: Thread? = null

    private var listener: MaskBluetoothManager.BreathingEventListener? = null

    // Biquad 필터 상태 (세션 내 버퍼 간 연속 유지)
    private var biquadZ1 = 0.0
    private var biquadZ2 = 0.0

    // ─── 공개 API ─────────────────────────────────────────────────────────

    fun setListener(l: MaskBluetoothManager.BreathingEventListener) {
        listener = l
    }

    @SuppressLint("MissingPermission")
    fun start(context: Context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e("MicBreathing", "RECORD_AUDIO 권한 없음")
            return
        }
        stop() // 기존 세션 정리

        val minBuffer = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
            .takeIf { it > 0 } ?: 2048

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            minBuffer * 4
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("MicBreathing", "AudioRecord 초기화 실패")
            audioRecord?.release()
            audioRecord = null
            return
        }

        isRunning = true
        recordThread = Thread { runDetectionLoop(minBuffer) }.also {
            it.isDaemon = true
            it.start()
        }
        Log.d("MicBreathing", "마이크 호흡 감지 시작 (bufferSize=$minBuffer)")
    }

    fun stop() {
        isRunning = false
        recordThread?.join(500)
        recordThread = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) { }
        audioRecord = null
        Log.d("MicBreathing", "마이크 호흡 감지 중지")
    }

    // ─── 감지 루프 ────────────────────────────────────────────────────────

    private fun runDetectionLoop(readSize: Int) {
        resetFilterState()
        val buffer = ShortArray(readSize)
        audioRecord?.startRecording()

        // 버퍼 하나당 경과 시간(초)
        val dt = readSize.toDouble() / SAMPLE_RATE

        var isExhaling  = false
        var exhaleStart = 0L
        var aboveCount  = 0
        var belowCount  = 0

        val flowSamples     = mutableListOf<Double>()
        val pressureSamples = mutableListOf<Double>()

        while (isRunning) {
            val read = audioRecord?.read(buffer, 0, readSize) ?: break
            if (read <= 0) continue

            val rms      = calculateRms(buffer, read)
            val flowLps  = rmsToFlowLps(rms)
            val pressure = flowToPressure(flowLps)
            val isBreath = classifyBuffer(buffer, read, rms)

            if (!isExhaling) {
                // ── 호기 시작 감지 ──────────────────────────────────────
                if (isBreath) {
                    belowCount = 0
                    if (++aboveCount >= START_CONSECUTIVE) {
                        isExhaling  = true
                        aboveCount  = 0
                        exhaleStart = System.currentTimeMillis()
                        flowSamples.clear()
                        pressureSamples.clear()
                        Log.d("MicBreathing", "호기 시작 RMS=${"%.0f".format(rms)}")
                        listener?.onExhaleStart()
                    }
                } else {
                    aboveCount = 0
                }
            } else {
                // ── 호기 중: 샘플 수집 ──────────────────────────────────
                flowSamples.add(flowLps)
                pressureSamples.add(pressure)

                // 유지 감지는 완화된 RMS 임계값만 사용 (히스테리시스)
                if (rms < EXHALE_RMS_KEEP) {
                    if (++belowCount >= END_CONSECUTIVE) {
                        isExhaling = false
                        belowCount = 0
                        val durationMs = System.currentTimeMillis() - exhaleStart
                        Log.d("MicBreathing", "호기 종료 duration=${durationMs}ms")
                        processAndNotify(durationMs, flowSamples.toList(), pressureSamples.toList(), dt)
                    }
                } else {
                    belowCount = 0
                }
            }
        }
    }

    // ─── 4-Feature 분류기 ─────────────────────────────────────────────────

    /**
     * 하드 RMS 게이트 + 주기성 하드 게이트 + ZCR/CrestFactor/저주파에너지 가중 투표.
     *
     * 판별 순서:
     *   1) RMS < 하한 → false (에너지 없음)
     *   2) 주기성(NACF) > 임계값 → false (말소리: 성대 주기 신호)
     *   3) 가중 score >= 0.70 → true (날숨)
     */
    private fun classifyBuffer(buffer: ShortArray, size: Int, rms: Double): Boolean {
        if (rms < EXHALE_RMS_THRESHOLD) return false

        // 주기성 하드 게이트: 말소리는 여기서 차단
        if (calculatePeriodicity(buffer, size, rms * rms) > PERIODICITY_THRESHOLD) return false

        val zcr          = calculateZcr(buffer, size)
        val crestFactor  = calculateCrestFactor(buffer, size, rms)
        val lowFreqRatio = calculateLowFreqRatio(buffer, size, rms * rms)

        val zcrScore   = if (zcr < ZCR_THRESHOLD) WEIGHT_ZCR else 0.0
        val crestScore = if (crestFactor < CREST_THRESHOLD) WEIGHT_CREST else 0.0
        val lfScore    = if (lowFreqRatio >= LOW_FREQ_THRESHOLD) WEIGHT_LOW_FREQ else 0.0
        val score      = zcrScore + crestScore + lfScore

        return score >= SCORE_THRESHOLD
    }

    /**
     * 정규화 자기상관(NACF)의 피치 lag 범위 최댓값 반환.
     * 말소리(유성음): 성대 주기 lag에서 NACF > 0.35
     * 날숨: 잡음형이라 NACF ≈ 0
     */
    private fun calculatePeriodicity(buffer: ShortArray, size: Int, rmsSquared: Double): Double {
        if (rmsSquared < 1.0) return 0.0
        val r0 = rmsSquared * size  // sum of squares

        var maxNacf = 0.0
        for (lag in PITCH_LAG_MIN..PITCH_LAG_MAX step PITCH_LAG_STEP) {
            val n = size - lag
            if (n <= 0) break
            var corr = 0.0
            for (i in 0 until n) {
                corr += buffer[i].toDouble() * buffer[i + lag].toDouble()
            }
            val nacf = corr / r0
            if (nacf > maxNacf) maxNacf = nacf
        }
        return maxNacf
    }

    /** ZCR (Zero Crossing Rate): 부호 변화 횟수 / (size - 1) */
    private fun calculateZcr(buffer: ShortArray, size: Int): Double {
        if (size < 2) return 0.0
        var crossings = 0
        for (i in 1 until size) {
            if (buffer[i - 1].toLong() * buffer[i].toLong() < 0) crossings++
        }
        return crossings.toDouble() / (size - 1)
    }

    /** Crest Factor: peak / RMS. 신호 없으면 99.0 반환 (날숨 아님으로 처리) */
    private fun calculateCrestFactor(buffer: ShortArray, size: Int, rms: Double): Double {
        if (rms < 1.0) return 99.0
        var peak = 0
        for (i in 0 until size) {
            val a = abs(buffer[i].toInt())
            if (a > peak) peak = a
        }
        return peak.toDouble() / rms
    }

    /**
     * 저주파 에너지 비율: Biquad IIR LP 필터(1000 Hz) 적용 후 에너지 / 전체 에너지.
     * 필터 상태(biquadZ1/Z2)는 세션 내 연속 유지.
     */
    private fun calculateLowFreqRatio(buffer: ShortArray, size: Int, rmsSquared: Double): Double {
        if (rmsSquared < 1.0) return 0.0
        var filteredSumSq = 0.0
        for (i in 0 until size) {
            val x = buffer[i].toDouble()
            val y = BQ_B0 * x + biquadZ1
            biquadZ1 = BQ_B1 * x - BQ_A1 * y + biquadZ2
            biquadZ2 = BQ_B2 * x - BQ_A2 * y
            filteredSumSq += y * y
        }
        return (filteredSumSq / size) / rmsSquared
    }

    /** 세션 시작 전 Biquad 필터 상태 초기화 */
    private fun resetFilterState() {
        biquadZ1 = 0.0
        biquadZ2 = 0.0
    }

    // ─── 수치 계산 (마스크와 동일 로직) ──────────────────────────────────

    /** 버퍼의 RMS 진폭 계산 */
    private fun calculateRms(buffer: ShortArray, size: Int): Double {
        if (size == 0) return 0.0
        var sum = 0.0
        for (i in 0 until size) {
            val s = buffer[i].toDouble()
            sum += s * s
        }
        return sqrt(sum / size)
    }

    /** RMS → 유량(L/s) 변환 */
    private fun rmsToFlowLps(rms: Double): Double =
        ((rms / MAX_AMPLITUDE) * MAX_FLOW_LPS).coerceIn(0.0, MAX_FLOW_LPS)

    /** 유량 → 동압(Pa) 변환 (베르누이) */
    private fun flowToPressure(flowLps: Double): Double =
        0.5 * RHO * flowLps.pow(2.0)

    /**
     * FVC / FEV1 / Ratio / Pressure 계산 후 리스너에 전달.
     * 마스크의 processLungFunctionData 와 동일한 보정 공식 사용.
     */
    private fun processAndNotify(
        durationMs: Long,
        flowSamples: List<Double>,
        pressureSamples: List<Double>,
        dt: Double
    ) {
        if (flowSamples.isEmpty()) return

        // FVC: 전체 유량 적분 × 보정
        val fvc = (flowSamples.sum() * dt) * FVC_CORRECTION

        // FEV1: 초반 1.5초 구간 × 가중 보정 (마스크 동일)
        val samplesIn15s = (1.5 / dt).toInt().coerceAtMost(flowSamples.size)
        val fev1 = (flowSamples.take(samplesIn15s).sum() * dt) * FVC_CORRECTION * 1.5

        // FEV1/FVC 비율 (최대 95% 제한)
        var ratio = if (fvc > 0) (fev1 / fvc) * 100.0 else 0.0
        if (ratio > 95.0) ratio = 95.0

        val avgPressure = if (pressureSamples.isNotEmpty()) pressureSamples.average() else 0.0

        Log.d("MicBreathing", "================================")
        Log.d("MicBreathing", "FVC  : ${"%.2f".format(fvc)} L")
        Log.d("MicBreathing", "FEV1 : ${"%.2f".format(fev1)} L")
        Log.d("MicBreathing", "Ratio: ${"%.1f".format(ratio)} %")
        Log.d("MicBreathing", "Pres : ${"%.2f".format(avgPressure)} Pa")
        Log.d("MicBreathing", "Time : ${"%.1f".format(durationMs / 1000.0)} 초")
        Log.d("MicBreathing", "================================")

        listener?.onExhaleEnd(durationMs, fvc, fev1, ratio, avgPressure)
    }
}
