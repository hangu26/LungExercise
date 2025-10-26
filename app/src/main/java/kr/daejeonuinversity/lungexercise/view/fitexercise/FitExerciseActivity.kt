package kr.daejeonuinversity.lungexercise.view.fitexercise

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.android.gms.wearable.Wearable
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityFitExerciseBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.util.util.RecommendWalkTimer
import kr.daejeonuinversity.lungexercise.view.fitexercise.result.FitResultActivity
import kr.daejeonuinversity.lungexercise.view.fitplan.FitPlanActivity
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.view.walkingtest.result.WalkingResultActivity
import kr.daejeonuinversity.lungexercise.viewmodel.FitExerciseViewModel
import org.koin.android.ext.android.inject
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FitExerciseActivity :
    BaseActivity<ActivityFitExerciseBinding>(R.layout.activity_fit_exercise) {

    private var lastElapsedSeconds = 0 // 클래스 변수로 선언
    private val backPressedCallback = BackPressedCallback(this)
    private var totalTime: Long = 0
    private lateinit var recommendWalkTimer: RecommendWalkTimer
    private var countDownTimer: CountDownTimer? = null
    private var remainingTime: Long = totalTime
    private var isRunning = false
    var age = 0
    var weight = 0.0
    var height = 0.0
    var latestDistance = 0.0
    var timer = 0
    var fitDistance = 0.0
    var currentDate = ""
    private var clickCount = 0  // 삭제 예정(개발자 모드)

    private val fViewModel: FitExerciseViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {

            activity = this@FitExerciseActivity
            lifecycleOwner = this@FitExerciseActivity
            viewmodel = fViewModel

        }

        /** 개발자 모드 **/
        binding.clRecommendDistance.setOnClickListener {
            clickCount++

            if (clickCount >= 6) {
                val intent = Intent(this, FitResultActivity::class.java)
                val distanceValue = 2500.4

                intent.putExtra("distance", distanceValue)
                intent.putExtra("userAge", 28)
                intent.putExtra("userWeight", 88)
                intent.putExtra("latestDistance", 100.0)
                intent.putExtra("timer", 6)
                intent.putExtra("fitDistance", 1600.5)
                intent.putExtra("currentDate", "2025년10월01일")
                intent.putExtra("currentWarningCount", 4)

                // distance를 m 단위(Double)로 보내기
                intent.putExtra("distance", distanceValue)

                intent.putExtra("calories", 20.5)
                intent.putExtra("steps", 120)
                intent.putExtra("avgHeartRate", 168.5)

                startActivityAnimation(intent, this@FitExerciseActivity)
                finish()
            }
        }

        recommendWalkTimer = binding.recommendWalkTimer

        init()
        backPressedCallback.addCallbackActivity(this, FitPlanActivity::class.java)
        initButton()
        observe()
    }

    private fun init() = with(intent) {
        sendResetMessageToWatch() // 액티비티 들어오면 워치 걸음수 초기화
        age = getIntExtra("userAge", 0)
        weight = getDoubleExtra("userWeight", 0.0)
        height = getDoubleExtra("userHeight", 0.0)
        latestDistance = getDoubleExtra("latestDistance", 0.0)
        timer = getIntExtra("timer", 0)
        fitDistance = getDoubleExtra("fitDistance", 0.0)

        val fitDistanceText = if (fitDistance < 1000) {
            String.format("%.0f m", fitDistance)   // 1km 미만 → 미터
        } else {
            String.format("%.3f km", fitDistance / 1000.0)  // 1km 이상 → 킬로미터
        }

        binding.txFitDistance.text = fitDistanceText

        totalTime = timer.toLong()
        val seconds = 0
        val timeText = String.format("%02d:%02d", totalTime, seconds)

        recommendWalkTimer.updateProgress(1f, timeText)


        fViewModel.setUserInfo(weight, timer, age)

        totalTime = timer * 60 * 1000L // 분 → 밀리초
        remainingTime = totalTime
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initButton() {

        binding.btnStart.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)

            if (event?.action == MotionEvent.ACTION_UP) {

                remainingTime = totalTime

                val minutes = (totalTime / 1000) / 60
                val seconds = (totalTime / 1000) % 60
                val timeText = String.format("%02d:%02d", minutes, seconds)

                recommendWalkTimer.updateProgress(1f, timeText)

                if (!isRunning) {
                    startTimer() // 처음 실행 또는 pause 후 resume

                    fViewModel.heartRate.observe(this) {
                        binding.txHeartRate.text = "$it"
                    }

                    fViewModel.stepCount.observe(this) {
                        binding.txStepCount.text = "$it"
                    }

                    fViewModel.calories.observe(this) { cal ->
                        val formattedCal = String.format("%.2f", cal)
                        binding.txCalorieValue.text = formattedCal
                    }

                    fViewModel.startReceiving()

                }


            }

            false
        }

        binding.btnStop.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)

            if (event?.action == MotionEvent.ACTION_UP) {

                if (isRunning) {
                    pauseTimer()
                    fViewModel.stopReceiving()
                }


            }

            false
        }

        binding.btnReset.setOnTouchListener { v, event ->

            setTouchAnimation(v, event)

            if (event?.action == MotionEvent.ACTION_UP) {

                stopTimer()
                fViewModel.stopReceiving()
                binding.txStepCount.text = "0"
                binding.txHeartRate.text = "0"
                binding.txDistanceValue.text = "0"

            }

            false

        }

    }

    private fun observe() = fViewModel.let { vm ->

        vm.backClicked.observe(this@FitExerciseActivity) {
            if (it) {
                val intent = Intent(this@FitExerciseActivity, FitPlanActivity::class.java)
                startActivityBackAnimation(intent, this@FitExerciseActivity)
                finish()
            }
        }

        vm.btnStartState.observe(this@FitExerciseActivity) {

            if (it) {

                remainingTime = totalTime

                val minutes = (totalTime / 1000) / 60
                val seconds = (totalTime / 1000) % 60
                val timeText = String.format("%02d:%02d", minutes, seconds)

                recommendWalkTimer.updateProgress(1f, timeText)

                vm.isReset()

                sendResetMessageToWatch()

                binding.btnStart.visibility = View.GONE
                binding.btnStop.visibility = View.VISIBLE
                val date = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyy년MM월dd일", Locale.getDefault())
                currentDate = dateFormat.format(Date(date))
                sendStartSignalToWatch()

            }

        }

        vm.btnStopState.observe(this@FitExerciseActivity) {

            if (it) {

                binding.btnStart.visibility = View.VISIBLE
                binding.btnResult.visibility = View.VISIBLE
                binding.txStart.text = "다시하기"
                binding.btnStop.visibility = View.GONE
                sendStopMessageToWatch()
                vm.isEnded()

            }

        }

        vm.btnResetState.observe(this@FitExerciseActivity) {

            if (it) {

                binding.btnStart.visibility = View.VISIBLE
                binding.btnStop.visibility = View.GONE
                sendResetMessageToWatch()
                binding.btnResult.visibility = View.GONE

                vm.isReset()

            }

        }

        vm.isEndedState.observe(this@FitExerciseActivity) {

            if (it) {

                binding.btnResult.visibility = View.VISIBLE

                val distanceStr = vm.txWalkDistance.value ?: "0 m"
                val calories = vm.calories.value ?: 0.0
                val steps = vm.stepCount.value ?: 0

                val distanceValue = distanceStr.replace(" m", "").trim().toDoubleOrNull() ?: 0.0

                val warningCount = vm.currentWarningCount.value ?: 0

                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                val elapsedSeconds = ((totalTime - remainingTime + 500) / 1000).toInt()

                Log.e("운동한 시간" , elapsedSeconds.toString())

                /**
                 * timer -> elapsedSeconds 저장으로 변경(실제 운동 시간)
                 * **/
                vm.saveFitResultData(
                    elapsedSeconds,
                    distanceValue,
                    calories,
                    warningCount,
                    steps,
                    date
                )

            } else {
                binding.btnResult.visibility = View.GONE
            }

        }

        vm.btnResultState.observe(this@FitExerciseActivity) {
            if (it) {
                val intent = Intent(this@FitExerciseActivity, FitResultActivity::class.java)

                val distanceStr = vm.txWalkDistance.value ?: "0 m"
                val calories = vm.calories.value ?: 0.0
                val steps = vm.stepCount.value ?: 0

                val avgHR = updateAverageHeartRate()

                val distanceValue = distanceStr.replace(" m", "").trim().toDoubleOrNull() ?: 0.0

                val warningCount = vm.currentWarningCount.value ?: 0

                val elapsedSeconds = ((totalTime - remainingTime + 500) / 1000).toInt()
                Log.e("운동한 시간" , elapsedSeconds.toString())

                intent.apply {
                    putExtra("userAge", age)
                    putExtra("userWeight", weight)
                    putExtra("userHeight", height)
                    putExtra("latestDistance", latestDistance)
                    putExtra("elapsedSeconds", elapsedSeconds)
                    putExtra("timer", timer)
                    putExtra("fitDistance", fitDistance)
                    putExtra("currentDate", currentDate)
                    putExtra("currentWarningCount", warningCount)
                    putExtra("distance", distanceValue)
                    putExtra("calories", calories)
                    putExtra("steps", steps)
                    putExtra("avgHeartRate", avgHR)
                }

                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                /**
                vm.saveFitResultData(
                timer,
                distanceValue,
                calories,
                warningCount,
                steps,
                date
                )
                 **/
                sendResetMessageToWatch()
                startActivityAnimation(intent, this@FitExerciseActivity)
                finish()
            }
        }



        vm.heartRateWarning.observe(this@FitExerciseActivity) {

            if (it) {

                Toast.makeText(this@FitExerciseActivity, "위험 수치입니다", Toast.LENGTH_SHORT).show()
                sendHeartRateWarningToWatch()

            }

        }

    }

    private fun updateAverageHeartRate(): Double {
        val durationSeconds = timer * 60  // timer는 분 단위
        return fViewModel.getAverageHeartRate(durationSeconds)
    }

    // MessageClient를 통해 시계에 메시지 전송
    private fun sendStartSignalToWatch() {
        val nodeClient = Wearable.getNodeClient(this)
        val messageClient = Wearable.getMessageClient(this)
        val exerciseTime = timer * 60 * 1000L // 분 → 밀리초

        // Long을 ByteArray로 변환
        val payload = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(exerciseTime).array()

        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, "/start_heart_rate_service", payload)
                    .addOnSuccessListener {
                        Log.d("PhoneApp", "시작 신호 전송 성공")
                    }
                    .addOnFailureListener {
                        Log.e("PhoneApp", "시작 신호 전송 실패", it)
                    }
            }
        }
    }


    private fun sendHeartRateWarningToWatch() {
        val nodeClient = Wearable.getNodeClient(this)
        val messageClient = Wearable.getMessageClient(this)

        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, "/heart_rate_warning", byteArrayOf())
                    .addOnSuccessListener {
                        Log.d("PhoneApp", "심박수 경고 신호 전송 성공")
                    }
                    .addOnFailureListener {
                        Log.e("PhoneApp", "심박수 경고 신호 전송 실패", it)
                    }
            }
        }
    }

    private fun sendResetMessageToWatch() {
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                Wearable.getMessageClient(this).sendMessage(
                    node.id,
                    "/reset_step_count", // 워치에서 수신하는 path
                    ByteArray(0)
                ).addOnSuccessListener {
                    Log.d("시계 걸음 수 초기화", "📤 워치 걸음수 초기화 메시지 전송 성공")
                }.addOnFailureListener {
                    Log.e("시계 걸음 수 초기화", "❌ 워치 초기화 메시지 전송 실패", it)
                }
            }
        }
    }

    private fun sendStopMessageToWatch() {
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                Wearable.getMessageClient(this).sendMessage(
                    node.id,
                    "/stop_step_count", // 워치에서 수신하는 path
                    ByteArray(0)
                ).addOnSuccessListener {
                    Log.d("시계 걸음 수 정지", "📤 워치 걸음수 초기화 메시지 전송 성공")
                }.addOnFailureListener {
                    Log.e("시계 걸음 수 정지", "❌ 워치 초기화 메시지 전송 실패", it)
                }
            }
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                fViewModel.startReceiving()

                remainingTime = millisUntilFinished
                val percentage = remainingTime.toFloat() / totalTime
                val totalSeconds = ((remainingTime + 500) / 1000).toInt()
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                val timeText = String.format("%02d:%02d", minutes, seconds)

                recommendWalkTimer.updateProgress(percentage, timeText)

                // ✅ 여기서 ViewModel에 경과 시간 전달
                lastElapsedSeconds = ((totalTime - remainingTime) / 1000).toInt()
                fViewModel.updateElapsedTime(lastElapsedSeconds)

            }

            override fun onFinish() {

                fViewModel.stopReceiving()

                val elapsedSeconds = (totalTime / 1000).toInt()

                binding.btnStart.visibility = View.VISIBLE
                binding.txStart.text = "다시하기"
                binding.btnStop.visibility = View.GONE
                fViewModel.btnStop()
                stopTimer()
                // ✅ 마지막에 남은 시간 초기화
                remainingTime = 0L
            }
        }.also {
            it.start()
        }
        isRunning = true
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        isRunning = false
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        remainingTime = totalTime
        isRunning = false
        fViewModel.stopReceiving()

        val minutes = (totalTime / 1000) / 60
        val seconds = (totalTime / 1000) % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)

        recommendWalkTimer.updateProgress(1f, timeText) // 초기 상태로 리셋
    }

}