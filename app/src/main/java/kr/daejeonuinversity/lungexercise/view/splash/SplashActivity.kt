package kr.daejeonuinversity.lungexercise.view.splash

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.databinding.DataBindingUtil
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivitySplashBinding
import kr.daejeonuinversity.lungexercise.view.infoinput.InfoInputActivity
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.SplashViewModel
import org.koin.android.ext.android.inject

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var sBinding: ActivitySplashBinding
    private val sViewModel: SplashViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = DataBindingUtil.setContentView(this@SplashActivity, R.layout.activity_splash)
        sBinding.apply {
            lifecycleOwner = this@SplashActivity
            activity = this@SplashActivity
            viewmodel = sViewModel
        }

        val isTutorialClear =
            getSharedPreferences("tutorial", Context.MODE_PRIVATE).getInt("isClear", 0)

        Handler(Looper.getMainLooper()).postDelayed({

            if (isTutorialClear == 0) {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)

                val options = ActivityOptions.makeCustomAnimation(
                    this@SplashActivity,
                    androidx.appcompat.R.anim.abc_fade_in,
                    androidx.appcompat.R.anim.abc_fade_out
                )
                startActivity(intent, options.toBundle())

                // 액세스 토큰을 가져와서 Constants에 설정

                finish()
            } else {

                val intent = Intent(this@SplashActivity, InfoInputActivity::class.java)

                val options = ActivityOptions.makeCustomAnimation(
                    this@SplashActivity,
                    androidx.appcompat.R.anim.abc_fade_in,
                    androidx.appcompat.R.anim.abc_fade_out
                )
                startActivity(intent, options.toBundle())

                // 액세스 토큰을 가져와서 Constants에 설정

                finish()
            }


        }, 2000)
    }
}
