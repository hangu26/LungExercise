package kr.daejeonuinversity.lungexercise.view.fitexercise

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityFitExerciseBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.util.util.RecommendWalkTimer
import kr.daejeonuinversity.lungexercise.view.fitplan.FitPlanActivity
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.FitExerciseViewModel
import org.koin.android.ext.android.inject

class FitExerciseActivity : BaseActivity<ActivityFitExerciseBinding>(R.layout.activity_fit_exercise) {

    private val backPressedCallback = BackPressedCallback(this)
    private lateinit var recommendWalkTimer: RecommendWalkTimer

    private val fViewModel : FitExerciseViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {

            activity = this@FitExerciseActivity
            lifecycleOwner = this@FitExerciseActivity
            viewmodel = fViewModel

        }

        recommendWalkTimer = binding.recommendWalkTimer

        backPressedCallback.addCallbackActivity(this, FitPlanActivity::class.java)

    }
}