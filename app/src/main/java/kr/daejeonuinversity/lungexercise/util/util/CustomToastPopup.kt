package kr.daejeonuinversity.lungexercise.util.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import kr.daejeonuinversity.lungexercise.R

class CustomToastPopup(
    private val rootView: View,        // Snackbar를 붙일 최상위 뷰
    private val inflater: LayoutInflater // 레이아웃을 inflate할 LayoutInflater
) {

    @SuppressLint("RestrictedApi")
    fun showMaskPopupToast(message: String = "동영상 마스크 팝업이 활성화되었습니다") {
        val snackbar = Snackbar.make(rootView, "", Snackbar.LENGTH_SHORT)
        val customLayout = inflater.inflate(R.layout.custom_mask_popup_toast, null)
        customLayout.findViewById<TextView>(R.id.toast_text_icon_mask_popup).text = message

        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        snackbarLayout.apply {
            background = null
            elevation = 0f
            setPadding(0, 0, 0, 0)
            layoutParams = (layoutParams as FrameLayout.LayoutParams).apply {
                width = FrameLayout.LayoutParams.WRAP_CONTENT
                gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                bottomMargin = 100
            }
            addView(customLayout, 0)
        }

        snackbar.duration = 1000
        snackbar.show()
    }

    /**
     * 다이얼로그 위에 확실히 표시되는 커스텀 토스트 (WindowManager 이용)
     */
    @SuppressLint("InflateParams")
    fun showAboveDialogToast(context: Context, message: String) {
        val view = inflater.inflate(R.layout.custom_mask_popup_toast, null)
        view.findViewById<TextView>(R.id.toast_text_icon_mask_popup).text = message

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_PANEL, // 다이얼로그보다 위
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            y = 200 // 화면 하단에서 조금 띄워줌
        }

        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(view, params)

        // 1초 후 자동 제거
        view.postDelayed({
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 1000)
    }
}
