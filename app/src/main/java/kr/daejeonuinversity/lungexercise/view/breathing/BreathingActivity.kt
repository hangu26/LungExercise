package kr.daejeonuinversity.lungexercise.view.breathing

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityBreathingBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.event.ExhaleEvent
import kr.daejeonuinversity.lungexercise.util.event.ResultEvent
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.util.util.MaskBluetoothManager
import kr.daejeonuinversity.lungexercise.view.exercise.LungExerciseActivity
import kr.daejeonuinversity.lungexercise.viewmodel.BreathingViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.EditInfoViewModel
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BreathingActivity : BaseActivity<ActivityBreathingBinding>(R.layout.activity_breathing) {

    private val eViewModel: EditInfoViewModel by inject()
    private val bViewModel: BreathingViewModel by inject()
    private var userProgressAnimator: ObjectAnimator? = null
    private var time = 8000
    private var userSeconds = 7000
    private var fvc : Double = 0.0
    private var fev1 : Double = 0.0
    private var ratio : Double = 0.0
    private var pressure : Double = 0.0
    private var isMicMeasureMode = false
    private var hasExhaleHandled = false
    private val backPressedCallback = BackPressedCallback(this)
    private val RECORD_AUDIO_REQUEST = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            activity = this@BreathingActivity
            viewmodel = bViewModel
            lifecycleOwner = this@BreathingActivity
        }

        backPressedCallback.addCallbackActivity(this, LungExerciseActivity::class.java)

        observe()
        observeInfo()
    }

    private fun observe() = bViewModel.let { vm ->

        vm.backClicked.observe(this) {
            if (it) {
                if (isMicMeasureMode) bViewModel.stopMicListening()
                val intent = Intent(this@BreathingActivity, LungExerciseActivity::class.java)
                startActivityAnimation(intent, this@BreathingActivity)
                finish()
            }
        }

        vm.btnStartState.observe(this) {
            if (it) {
                binding.btnStart.visibility = View.GONE
                binding.btnStop.visibility = View.VISIBLE
                resetProgressBar()
                hasExhaleHandled = false
            }
        }

        vm.isResultAvailable.observe(this@BreathingActivity) {
            if (it) {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                vm.saveBreathData(
                    time,
                    userSeconds,
                    date,
                    fvc,
                    fev1,
                    ratio,
                    pressure
                )
            }
        }

        vm.btnStopState.observe(this) {
            if (it) {
                binding.btnStart.visibility = View.VISIBLE
                binding.btnStop.visibility = View.GONE
                stopUserProgress()
            }
        }

        vm.exhaleEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { exhale ->
                if (hasExhaleHandled) return@observe

                when (exhale) {
                    is ExhaleEvent.Start -> {
                        startUserProgress()
                        binding.txUserTimeDetail.text =
                            resources.getString(R.string.tx_is_measuring)
                    }

                    is ExhaleEvent.End -> {
                        stopUserProgress()
                        binding.txUserTimeDetail.text =
                            resources.getString(R.string.tx_user_time_detail)
                        vm.btnReset()
                        userSeconds = exhale.duration.toInt()
                        fvc = exhale.fvc
                        fev1 = exhale.fev1
                        ratio = exhale.ratio
                        pressure = exhale.pressure
                        hasExhaleHandled = true
                    }
                }
            }
        }

        vm.btnSettingReset.observe(this@BreathingActivity) {
            if (it) {

                binding.btnStart.visibility = View.VISIBLE
                binding.btnStop.visibility = View.GONE

            }
        }

        vm.userTime.observe(this) {
            binding.txUserTime.text = it
        }

        vm.resultEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result) {
                    ResultEvent.ShowResultDialog -> {
                        showDialog()

                    }

                    ResultEvent.ShowResultToast -> Toast.makeText(
                        this,
                        "호흡 연습을 완료하세요.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun observeInfo() {
        eViewModel.fetchUserInfo()

        eViewModel.genderState.observe(this) { gender ->
            val yearStr = eViewModel.txYear.value ?: "1990"
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val age = currentYear - (yearStr.toIntOrNull() ?: 1990)

            time = calculateTargetExhaleTime(age, gender)
            bViewModel.setTargetExhaleTime(time)

            binding.txNormalTime.text = (time/1000).toString()
            binding.txEightSecond.text = "${time / 1000}초"

            Log.d("목표설정", "나이: $age, 성별: $gender -> 목표시간: ${time/1000.0}초")
        }
    }

    private fun calculateTargetExhaleTime(age: Int, gender: String): Int {
        val logTime = if (gender == "man" || gender == "남") {
            3.1226 - (0.0036 * age)
        } else {
            3.2274 - (0.0102 * age)
        }
        return (kotlin.math.exp(logTime) * 1000).toInt()
    }

    fun onStartClicked(_view: View) {
        if (MaskBluetoothManager.isConnectedPublic) {
            isMicMeasureMode = false
            bViewModel.btnStart()
            return
        }

        showMicConnectDialog()
    }

    fun onStopClicked(_view: View) {
        if (isMicMeasureMode) bViewModel.stopMicListening()
        bViewModel.btnStop()
    }

    private fun showMicConnectDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_mic_connect_title))
            .setMessage(getString(R.string.dialog_mic_connect_message))
            .setPositiveButton(R.string.dialog_mic_connect_yes) { _, _ ->
                isMicMeasureMode = true
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    showMicBlowDialog()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        RECORD_AUDIO_REQUEST
                    )
                }
            }
            .setNegativeButton(R.string.dialog_mic_connect_no) { _, _ ->
                val intent = Intent(this, LungExerciseActivity::class.java)
                startActivityAnimation(intent, this)
                finish()
            }
            .show()
    }

    private fun showMicBlowDialog() {
        val dialog = MicBlowDialog(this) {
            // 5초 카운트 종료 → 측정 시작
            bViewModel.btnStart()
            bViewModel.startMicListening()
        }
        dialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isMicMeasureMode) showMicBlowDialog()
            } else {
                isMicMeasureMode = false
                Toast.makeText(this, "마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetProgressBar() {
        binding.progressBarUser.progress = 0
    }

    private fun startUserProgress() {
        val progressBar = binding.progressBarUser
        progressBar.progress = 0
        userProgressAnimator?.cancel()
        userProgressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100).apply {
            duration = time.toLong()
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun stopUserProgress() {
        userProgressAnimator?.cancel()
        userProgressAnimator = null
    }

    private fun showDialog() {
        val dlg = BreathingDialog(this)
        dlg.show(
            (time / 1000).toLong(),
            bViewModel.userTime.value?.replace(" 초", "")?.toLongOrNull() ?: 0L
        )
    }

}



