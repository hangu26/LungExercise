package kr.daejeonuinversity.lungexercise.util.util

import kr.daejeonuinversity.lungexercise.viewmodel.BirthdayViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.BodyViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.BreathingViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.DeveloperViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.EditInfoViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.GenderViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.HistoryRecordViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.HistoryViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.InfoInputViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.LungExerciseViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.MainViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.SplashViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.VideoViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.WalkHistoryViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.WalkingTestViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val module = module {
    viewModel { SplashViewModel(androidApplication()) }
    viewModel { MainViewModel(androidApplication()) }
    viewModel { InfoInputViewModel(get(),androidApplication()) }
    viewModel { BirthdayViewModel(androidApplication()) }
    viewModel { GenderViewModel(androidApplication()) }
    viewModel { BodyViewModel(androidApplication()) }
    viewModel { LungExerciseViewModel(androidApplication()) }
    viewModel { WalkingTestViewModel(androidApplication()) }
    viewModel { BreathingViewModel(get(),androidApplication()) }
    viewModel { HistoryViewModel(get(),androidApplication()) }
    viewModel { VideoViewModel(androidApplication()) }
    viewModel { EditInfoViewModel(get(),androidApplication()) }
    viewModel { HistoryRecordViewModel(androidApplication()) }
    viewModel { WalkHistoryViewModel(androidApplication()) }
    viewModel { DeveloperViewModel(get(),androidApplication()) }

}