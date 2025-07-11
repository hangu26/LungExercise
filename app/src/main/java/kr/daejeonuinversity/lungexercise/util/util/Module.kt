package kr.daejeonuinversity.lungexercise.util.util

import kr.daejeonuinversity.lungexercise.viewmodel.BirthdayViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.BodyViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.BreathingViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.GenderViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.InfoInputViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.LungExerciseViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.MainViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.SplashViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.WalkingTestViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val module = module {
    viewModel { SplashViewModel(androidApplication()) }
    viewModel { MainViewModel(androidApplication()) }
    viewModel { InfoInputViewModel(androidApplication()) }
    viewModel { BirthdayViewModel(androidApplication()) }
    viewModel { GenderViewModel(androidApplication()) }
    viewModel { BodyViewModel(androidApplication()) }
    viewModel { LungExerciseViewModel(androidApplication()) }
    viewModel { WalkingTestViewModel(androidApplication()) }
    viewModel { BreathingViewModel(androidApplication()) }

}