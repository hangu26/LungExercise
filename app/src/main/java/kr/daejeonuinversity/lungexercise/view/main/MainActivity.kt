package kr.daejeonuinversity.lungexercise.view.main

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import kr.daejeonuinversity.lungexercise.util.util.HeartRateReceiver
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityMainBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.view.lungexercise.LungExerciseActivity
import kr.daejeonuinversity.lungexercise.view.walkingtest.WalkingTestActivity
import kr.daejeonuinversity.lungexercise.viewmodel.MainViewModel
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private lateinit var heartRateReceiver: HeartRateReceiver
    private val mViewModel: MainViewModel by inject()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            activity = this@MainActivity
            lifecycleOwner = this@MainActivity
            viewModel = mViewModel
        }

        initButton()

        observe()

    }

    @SuppressLint("ClickableViewAccessibility")
    fun initButton() {

        binding.btnLungExerciseDetail.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)

            if (event?.action == MotionEvent.ACTION_UP) {
                val intent = Intent(this@MainActivity, LungExerciseActivity::class.java)
                startActivityAnimation(intent,this@MainActivity)
                finish()
            }

            false
        }

        binding.btnWalkingTest.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)

            val intent = Intent(this@MainActivity, WalkingTestActivity::class.java)
            startActivityAnimation(intent, this@MainActivity)
            finish()

            false
        }
    }

    private fun observe() = mViewModel.let { vm ->

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
//        mViewModel.stopReceiving()
    }
}
