package kr.daejeonuinversity.lungexercise.view.insight

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityInsightBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.InsightViewModel
import org.koin.android.ext.android.inject

class InsightActivity : BaseActivity<ActivityInsightBinding>(R.layout.activity_insight) {

    private val iViewModel: InsightViewModel by inject()
    private val backPressedCallback = BackPressedCallback(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            activity = this@InsightActivity
            viewmodel = iViewModel
            lifecycleOwner = this@InsightActivity
        }

        observe()
        backPressedCallback.addCallbackActivity(this, MainActivity::class.java)

    }

    private fun observe() = iViewModel.let { vm ->

        vm.btnBackState.observe(this@InsightActivity) {

            if (it) {

                val intent = Intent(this@InsightActivity, MainActivity::class.java)
                startActivityBackAnimation(intent, this@InsightActivity)

            }

        }

    }

}