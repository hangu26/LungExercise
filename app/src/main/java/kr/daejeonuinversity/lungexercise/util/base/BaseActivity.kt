package kr.daejeonuinversity.lungexercise.util.base

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseActivity<T: ViewDataBinding>(@LayoutRes val layoutRes: Int)
    : AppCompatActivity() {
    protected lateinit var binding: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 초기화된 layoutResId로 DataBinding 객체 생성
        binding = DataBindingUtil.setContentView(this, layoutRes)
        // LiveData를 사용하기 위한 lifecycleOwner 지정
        binding.lifecycleOwner = this@BaseActivity
    }

    fun startActivityAnimation(intent: Intent, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val options = ActivityOptions.makeCustomAnimation(
                context,
                androidx.appcompat.R.anim.abc_fade_in,
                androidx.appcompat.R.anim.abc_fade_out
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


}