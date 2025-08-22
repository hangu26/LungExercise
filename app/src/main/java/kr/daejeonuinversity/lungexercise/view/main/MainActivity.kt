package kr.daejeonuinversity.lungexercise.view.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityMainBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.view.developer.DeveloperActivity
import kr.daejeonuinversity.lungexercise.view.editinfo.EditInfoActivity
import kr.daejeonuinversity.lungexercise.view.history.HistoryActivity
import kr.daejeonuinversity.lungexercise.view.exercise.LungExerciseActivity
import kr.daejeonuinversity.lungexercise.view.history.HistoryRecordActivity
import kr.daejeonuinversity.lungexercise.view.insight.InsightActivity
import kr.daejeonuinversity.lungexercise.view.setting.SettingActivity
import kr.daejeonuinversity.lungexercise.view.walkingtest.WalkingTestActivity
import kr.daejeonuinversity.lungexercise.viewmodel.MainViewModel
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val mViewModel: MainViewModel by inject()
    private val backPressedCallback = BackPressedCallback(this)
    private val clickTimestamps = mutableListOf<Long>()
    companion object {
        private const val REQUEST_CODE_BLUETOOTH_CONNECT = 1001
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            activity = this@MainActivity
            lifecycleOwner = this@MainActivity
            viewModel = mViewModel
        }

        binding.ibSetting.setOnClickListener {
            handleRapidClicks(
                onThresholdReached = {
                    // 개발자 액티비티로 이동
                    val intent = Intent(this, DeveloperActivity::class.java)
                    startActivityAnimation(intent,this@MainActivity)
                    finish()
                }
            )
        }

        initButton()
        observe()

        // 권한 체크 및 요청 후 블루투스 연결 시도
//        checkBluetoothConnectPermissionAndConnect()
        backPressedCallback.doubleBackToExit(this)

    }

    private fun handleRapidClicks(
        timeWindowMillis: Long = 5000,
        requiredClicks: Int = 10,
        onThresholdReached: () -> Unit
    ) {
        val now = System.currentTimeMillis()
        clickTimestamps.add(now)

        // 5초 이전 타임스탬프 삭제
        clickTimestamps.removeAll { it < now - timeWindowMillis }

        if (clickTimestamps.size >= requiredClicks) {
            clickTimestamps.clear()
            onThresholdReached()
        }
    }
    private fun checkBluetoothConnectPermissionAndConnect() {
        Log.d("메인 액티비티", "checkBluetoothConnectPermissionAndConnect 호출됨")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 이상 권한 필요
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("메인 액티비티", "BLUETOOTH_CONNECT 권한 없음, 요청 시작")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_CODE_BLUETOOTH_CONNECT
                )
                return
            }
        }
        Log.d("메인 액티비티", "권한 있음 - connectToDevice 호출")
        mViewModel.connectToDevice()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_BLUETOOTH_CONNECT) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허용됨 -> 블루투스 연결 시도
                mViewModel.connectToDevice()
            } else {
                // 권한 거부됨 -> 사용자 안내 등 처리 필요
                Log.d("메인 액티비티", "BLUETOOTH_CONNECT permission denied")
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initButton() {
        binding.btnLungExerciseDetail.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)

            if (event?.action == MotionEvent.ACTION_UP) {
                val intent = Intent(this@MainActivity, LungExerciseActivity::class.java)
                startActivityAnimation(intent, this@MainActivity)
                finish()
            }

            false
        }

        binding.btnWalkingTest.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)
            if (event?.action == MotionEvent.ACTION_UP) {

                val intent = Intent(this@MainActivity, WalkingTestActivity::class.java)
                startActivityAnimation(intent, this@MainActivity)
                finish()
            }
            false
        }

        binding.btnHistory.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)
            if (event?.action == MotionEvent.ACTION_UP) {

                val intent = Intent(this@MainActivity, HistoryRecordActivity::class.java)
                startActivityAnimation(intent, this@MainActivity)
                finish()
            }
            false
        }

        binding.btnEditInfo.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)
            if (event?.action == MotionEvent.ACTION_UP) {

                val intent = Intent(this@MainActivity, EditInfoActivity::class.java)
                startActivityAnimation(intent, this@MainActivity)
                finish()
            }
            false
        }

        binding.btnSetting.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)
            if (event?.action == MotionEvent.ACTION_UP) {

                val intent = Intent(this@MainActivity, SettingActivity::class.java)
                startActivityAnimation(intent, this@MainActivity)
                finish()
            }
            false
        }

        binding.btnInsight.setOnTouchListener{ v, event ->
            setTouchAnimation(v,event)
            if (event?.action == MotionEvent.ACTION_UP){
                val intent = Intent(this@MainActivity, InsightActivity::class.java)
                startActivityAnimation(intent, this@MainActivity)
                finish()
            }
            false
        }

    }

    private fun observe() = mViewModel.let { vm ->

        vm.breathData.observe(this@MainActivity) { data ->
            Log.d("마스크 응답", "호흡량 $data")
        }

        /**
        vm.heartRate.observe(this) {
        binding.tvHeartRate.text = "❤️ 심박수: $it bpm"
        }

        vm.stepCount.observe(this){
        binding.tvStepCount.text = "👟 걸음수: $it 걸음"
        }

        vm.startReceiving()
         **/
    }

    override fun onDestroy() {
        super.onDestroy()
        // 연결 해제 등 필요한 정리 작업 있으면 ViewModel 함수 호출
        // mViewModel.disconnect()
    }
}
