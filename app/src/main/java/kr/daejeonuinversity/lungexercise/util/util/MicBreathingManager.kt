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
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 휴대폰 마이크로 호기(날숨)를 감지해 MaskBluetoothManager.BreathingEventListener 와 동일한
 * 이벤트(onExhaleStart / onExhaleEnd)를 발행합니다.
 *
 * - 진폭(RMS) 기반 임계값으로 날숨 시작/종료를 판단
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
    private const val EXHALE_RMS_THRESHOLD = 700.0   // 이 RMS 이상 = 날숨
    private const val START_CONSECUTIVE    = 3        // 연속 N 버퍼 초과 → 호기 시작
    private const val END_CONSECUTIVE      = 6        // 연속 N 버퍼 미만 → 호기 종료

    // ─── 내부 상태 ────────────────────────────────────────────────────────
    private var audioRecord: AudioRecord? = null
    @Volatile private var isRunning = false
    private var recordThread: Thread? = null

    private var listener: MaskBluetoothManager.BreathingEventListener? = null

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

            if (!isExhaling) {
                // ── 호기 시작 감지 ──────────────────────────────────────
                if (rms >= EXHALE_RMS_THRESHOLD) {
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

                if (rms < EXHALE_RMS_THRESHOLD) {
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

