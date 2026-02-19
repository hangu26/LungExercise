package kr.daejeonuinversity.lungexercise.util.util

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.view.fitexercise.result.FitResultArgs

class BackPressedCallback(private val activity: FragmentActivity) {

    private var backPressedTime = 0L
    private val EXIT_INTERVAL = 2000L // 2초
    fun addCallbackActivity(context: Activity, toActivity: Class<out Activity>) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 원하는 로직을 직접 구현
                val intent = Intent(context, toActivity)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val options = ActivityOptions.makeCustomAnimation(
                        context,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                    context.startActivity(intent, options.toBundle())
                } else {
                    context.startActivity(intent)
                }
                context.finish()
            }
        }
        activity.onBackPressedDispatcher.addCallback(activity, callback)
    }

    fun intentCallbackActivity(
        context: Activity,
        toActivity: Class<out Activity>,
        args: FitResultArgs
    ) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(context, toActivity).apply {
                    putExtra("userAge", args.age)
                    putExtra("userWeight", args.weight)
                    putExtra("latestDistance", args.latestDistance)
                    putExtra("timer", args.timer)
                    putExtra("fitDistance", args.fitDistance)
                    putExtra("currentDate", args.currentDate)
                    putExtra("currentWarningCount", args.currentWarningCount)
                    putExtra("distance", args.userDistance)
                    putExtra("calories", args.userCalories)
                    putExtra("steps", args.userSteps)
                }

                val options = ActivityOptions.makeCustomAnimation(
                    context,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                context.startActivity(intent, options.toBundle())
                context.finish()
            }
        }
        activity.onBackPressedDispatcher.addCallback(activity, callback)
    }


    fun finishActivity(context: Activity){

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                context.finish()
            }
        }
        activity.onBackPressedDispatcher.addCallback(activity, callback)

    }

    fun doubleBackToExit(context: Context) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime < EXIT_INTERVAL) {
                    (context as Activity).finish()
                } else {
                    backPressedTime = currentTime
                    Toast.makeText(context, "한번 더 누를 시 앱이 종료됩니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
        activity.onBackPressedDispatcher.addCallback(activity, callback)
    }

} // BackPressedCallback class