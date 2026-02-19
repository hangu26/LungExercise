package kr.daejeonuinversity.lungexercise.view.fitexercise.result

import android.content.Intent

data class FitResultArgs(
    val age: Int,
    val weight: Double,
    val latestDistance: Double,
    val timer: Int,
    val fitDistance: Double,
    val currentDate: String,
    val currentWarningCount: Int,
    val userDistance: Double,
    val userCalories: Double,
    val userSteps: Int,
    val avgHeartRate : Double
) {
    companion object {
        fun fromIntent(intent: Intent) = FitResultArgs(
            age = intent.getIntExtra("userAge", 0),
            weight = intent.getDoubleExtra("userWeight", 0.0),
            latestDistance = intent.getDoubleExtra("latestDistance", 0.0),
            timer = intent.getIntExtra("timer", 0),
            fitDistance = intent.getDoubleExtra("fitDistance", 0.0),
            currentDate = intent.getStringExtra("currentDate").orEmpty(),
            currentWarningCount = intent.getIntExtra("currentWarningCount", 0),
            userDistance = intent.getDoubleExtra("distance", 0.0),
            userCalories = intent.getDoubleExtra("calories", 0.0),
            userSteps = intent.getIntExtra("steps", 0),
            avgHeartRate = intent.getDoubleExtra("avgHeartRate", 0.0)
        )
    }

    fun Intent.putFitResultArgs(args: FitResultArgs): Intent {
        return this.apply {
            putExtra("userAge", args.age)
            putExtra("userWeight", args.weight)
            putExtra("latestDistance", args.latestDistance)
            putExtra("timer", args.timer)
            putExtra("fitDistance", args.fitDistance)
        }
    }

}