package kr.daejeonuinversity.lungexercise.util.base

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kr.daejeonuinversity.lungexercise.R
import kotlin.math.roundToInt

abstract class BaseActivity<T: ViewDataBinding>(@LayoutRes val layoutRes: Int)
    : AppCompatActivity() {
    protected lateinit var binding: T
    private val BLUETOOTH_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 초기화된 layoutResId로 DataBinding 객체 생성
        binding = DataBindingUtil.setContentView(this, layoutRes)
        // LiveData를 사용하기 위한 lifecycleOwner 지정
        binding.lifecycleOwner = this@BaseActivity

        // 상태바 아이콘을 어두운 색(검은색)으로 변경 (API 23 이상)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val rootView = binding.root
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBarsInsets.top,
            )
            WindowInsetsCompat.CONSUMED
        }

    }

    fun checkAndRequestBluetoothPermissions() {
        val requiredPermissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 이상
            requiredPermissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
            requiredPermissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
            requiredPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION) // 위치 권한 필요
        } else {
            // Android 11 이하
            requiredPermissions.add(android.Manifest.permission.BLUETOOTH)
            requiredPermissions.add(android.Manifest.permission.BLUETOOTH_ADMIN)
            requiredPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), BLUETOOTH_PERMISSION_CODE)
        } else {
            onBluetoothPermissionsGranted()
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == BLUETOOTH_PERMISSION_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12 이상: BLUETOOTH_CONNECT 권한 필요
                if (allGranted) {
                    onBluetoothPermissionsGranted()
                } else {
                    Toast.makeText(this, "블루투스 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Android 11 이하: 위치 권한 등만 확인하면 됨
                onBluetoothPermissionsGranted()
            }
        }
    }


    private fun onBluetoothPermissionsGranted() {
        // 권한 허용 후 실행할 코드 작성 (예: 블루투스 초기화)
        // 여기서 바로 블루투스 연결 시도해도 됨
    }

    fun startActivityAnimation(intent: Intent, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val options = ActivityOptions.makeCustomAnimation(
                context,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            context.startActivity(intent, options.toBundle())
            finish()
        } else {
            context.startActivity(intent)
            finish()
        }
    }

    fun startActivityBackAnimation(intent: Intent, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val options = ActivityOptions.makeCustomAnimation(
                context,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            context.startActivity(intent, options.toBundle())
            finish()
        } else {
            context.startActivity(intent)
            finish()
        }
    }

    fun setTouchAnimation(view: View, event: MotionEvent?) {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(0.97f).scaleY(0.97f).translationZ(5f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1f).scaleY(1f).translationZ(20f).setDuration(100).start()
                }
            }
        }
    }

    /** 맞춤형 운동
     * 추천 거리가 1km 미만이면 m로 표시, 1km 이상이면 km로 표시 함수 **/
    fun formatDistance(distance: Double) : String {
        return if (distance < 1000) {
            String.format("%.0f m", distance)   // 1km 미만 → 미터
        } else {
            String.format("%.1f km", distance / 1000.0)  // 1km 이상 → 킬로미터
        }
    }

    fun formatDistanceKm(distanceKm: Double): String {
        return if (distanceKm < 1) {
            val distanceM = (distanceKm * 1000).roundToInt()
            "$distanceM m"
        } else {
            String.format("%.1f km", distanceKm)
        }
    }

}