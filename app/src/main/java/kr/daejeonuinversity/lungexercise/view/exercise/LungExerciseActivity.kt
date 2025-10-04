package kr.daejeonuinversity.lungexercise.view.exercise

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityLungExerciseBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.util.util.MaskBluetoothManager
import kr.daejeonuinversity.lungexercise.view.breathing.BreathingActivity
import kr.daejeonuinversity.lungexercise.view.exercise.fragment.BluetoothFragment
import kr.daejeonuinversity.lungexercise.view.exercise.fragment.MaskSettingFragment
import kr.daejeonuinversity.lungexercise.view.exercise.fragment.VideoDialogFragment
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.LungExerciseViewModel
import org.koin.android.ext.android.inject

class LungExerciseActivity :
    BaseActivity<ActivityLungExerciseBinding>(R.layout.activity_lung_exercise),
    MaskBluetoothManager.BreathingEventListener {

    private val lViewModel: LungExerciseViewModel by inject()
    private val backPressedCallback = BackPressedCallback(this)
    private lateinit var bluetoothFragment: BluetoothFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            activity = this@LungExerciseActivity
            viewmodel = lViewModel
            lifecycleOwner = this@LungExerciseActivity
        }

        printPairedDevices(this)

        setupClickListeners()
        initView()
        observe()
        setupBluetoothCallbacks()
        checkAndRequestBluetoothPermissions()
        backPressedCallback.addCallbackActivity(this, MainActivity::class.java)
    }

    @SuppressLint("MissingPermission")
    fun printPairedDevices(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없으면 요청하거나 그냥 return
            Log.e("페어링된 기기", "BLUETOOTH_CONNECT 권한 없음")
            return
        }

        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

        pairedDevices?.forEach { device ->
            Log.d("페어링된 기기", "이름: ${device.name}, 주소: ${device.address}")
        }
    }

    private fun initView() {
        updateConnectionUI(MaskBluetoothManager.isConnectedPublic)
    }

    private fun updateConnectionUI(isConnected: Boolean) {
        if (isConnected) {
            val name = MaskBluetoothManager.connectedDeviceNamePublic ?: "기기 이름 알 수 없음"
            binding.txBluetooth01.text = name
            binding.txBluetooth02.text = "마스크와 연결되었습니다"

            binding.btnConnectMask.visibility = View.GONE
            binding.btnDisconnectMask.visibility = View.VISIBLE

        } else {
            binding.txBluetooth01.text = "블루투스 마스크"
            binding.txBluetooth02.text = "마스크를 연결해보세요"

            binding.btnConnectMask.visibility = View.VISIBLE
            binding.btnDisconnectMask.visibility = View.GONE

        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupClickListeners() {
        binding.constraintBreath.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)
            if (event?.action == MotionEvent.ACTION_UP) {
                startActivityAnimation(Intent(this, BreathingActivity::class.java), this)
            }
            false
        }

        binding.constraintVideo.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)
            if (event?.action == MotionEvent.ACTION_UP) {
                showExoPlayerPopup()
            }
            false
        }
    }

    private fun observe() = lViewModel.let { vm ->

        vm.backClicked.observe(this) {
            if (it) {
                val intent = Intent(this, MainActivity::class.java)
                startActivityBackAnimation(intent, this)
                finish()
            }
        }

        vm.isConnected.observe(this) { connected ->
            if (connected) {
                binding.btnConnectMask.visibility = View.GONE
                binding.btnDisconnectMask.visibility = View.VISIBLE

                val name = MaskBluetoothManager.connectedDeviceNamePublic ?: "기기 이름 알 수 없음"
                binding.txBluetooth01.text = name
                binding.txBluetooth02.text = "마스크와 연결되었습니다"

            } else {
                binding.btnConnectMask.visibility = View.VISIBLE
                binding.btnDisconnectMask.visibility = View.GONE

                binding.txBluetooth01.text = "블루투스 마스크"
                binding.txBluetooth02.text = "마스크를 연결해보세요"
            }
        }

        vm.btnConnectState.observe(this) { connectRequested ->
            if (connectRequested) {
                MaskBluetoothManager.setBreathingEventListener(this)
                showBluetoothDialog()
            }
        }

        vm.btnDisconnectState.observe(this@LungExerciseActivity) { disconnectRequested ->
            if (disconnectRequested) {
                MaskBluetoothManager.disconnect()
                // 연결 끊기 후 UI 초기화
                lViewModel.setIsConnected(false)
                resetConnectionUI()
            }
        }

    }

    private fun resetConnectionUI() {
        binding.btnConnectMask.visibility = View.VISIBLE
        binding.btnDisconnectMask.visibility = View.GONE

        binding.txBluetooth01.text = "블루투스 마스크"
        binding.txBluetooth02.text = "마스크를 연결해보세요"

    }

    private fun setupBluetoothCallbacks() {
        MaskBluetoothManager.connectCallback = object : MaskBluetoothManager.ConnectCallback {
            override fun onDeviceFound(deviceName: String) {
                runOnUiThread {
                    if (::bluetoothFragment.isInitialized && bluetoothFragment.isAdded) {
                        bluetoothFragment.setStatusText("페어링된 디바이스 발견: $deviceName")
                        bluetoothFragment.setBluetoothListVisible(true)
                        bluetoothFragment.setNextDialogVisible(false)
                    }
                }
            }

            override fun onDeviceNotFound(deviceName: String) {
                runOnUiThread {
                    if (::bluetoothFragment.isInitialized && bluetoothFragment.isAdded) {
                        bluetoothFragment.setStatusText("연결할 디바이스 ($deviceName) 미발견")
                        bluetoothFragment.setBluetoothListVisible(false)
                        bluetoothFragment.setNextDialogVisible(false)
                    }
                    lViewModel.setIsConnected(false)  // 연결 실패 상태 반영
                }
            }

            override fun onConnectSuccess() {
                runOnUiThread {
                    if (::bluetoothFragment.isInitialized && bluetoothFragment.isAdded) {
                        bluetoothFragment.setStatusText("블루투스 연결 성공")
                        bluetoothFragment.setBluetoothListVisible(true)
                        bluetoothFragment.setNextDialogVisible(true)
                    }
                    lViewModel.setIsConnected(true)  // 연결 성공 상태 반영
                }
            }

            override fun onConnectFailed(reason: String) {
                runOnUiThread {
                    if (::bluetoothFragment.isInitialized && bluetoothFragment.isAdded) {
                        bluetoothFragment.setStatusText("블루투스 연결 실패. 마스크의 전원을 키거나 다시 연결을 시도하세요")
                        bluetoothFragment.setBluetoothListVisible(true)
                        bluetoothFragment.setNextDialogVisible(false)
                    }
                    lViewModel.setIsConnected(false)  // 연결 실패 상태 반영
                }
            }
        }
    }

    /** 마스크 마다 처리 필요. 수정 예정 **/
    private fun showBluetoothDialog() {
        bluetoothFragment = BluetoothFragment(
            onConnectClick = { connectImmediately ->
                MaskBluetoothManager.connectToDevice(this, deviceName = "MASK2", connectImmediately)
                if (connectImmediately) {
                    Toast.makeText(this, "연결중입니다...", Toast.LENGTH_SHORT).show()
                }
            },
            onNextClick = {
                bluetoothFragment.dismiss()

                Handler(Looper.getMainLooper()).postDelayed({
                    showMaskSettingDialogWithAnimation()
                }, 200)
            }
        )
        bluetoothFragment.show(supportFragmentManager, "bluetooth_dialog")
    }

    private fun showMaskSettingDialogWithAnimation() {
        val maskSettingDialog = MaskSettingFragment { cleared ->
            // 완료 콜백 필요 시 처리
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.slide_in_right,    // enter
            R.anim.slide_out_left,    // exit
            R.anim.slide_in_left,     // popEnter
            R.anim.slide_out_right    // popExit
        )
        maskSettingDialog.show(transaction, "mask_setting_dialog")
    }

    private fun showExoPlayerPopup() {
        val fragment = VideoDialogFragment()
        fragment.show(supportFragmentManager, "video_dialog")
    }

    override fun onExhaleStart() {
        // TODO: 필요시 구현
    }

    override fun onExhaleEnd(durationMs: Long) {
        // TODO: 필요시 구현
    }

}
