package kr.daejeonuinversity.lungexercise.util.util

import com.google.gson.GsonBuilder
import kr.daejeonuinversity.lungexercise.data.remote.api.AirKoreaApi
import kr.daejeonuinversity.lungexercise.data.remote.api.RetrofitClient
import kr.daejeonuinversity.lungexercise.viewmodel.BirthdayViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.BodyViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.BreathingViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.DeveloperViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.EditInfoViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.FitExerciseViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.FitPlanViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.FitResultViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.GenderViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.HistoryRecordViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.HistoryViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.InfoInputViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.InsightViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.LungExerciseViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.MainViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.SettingViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.SplashViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.VideoViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.WalkHistoryViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.WalkingResultViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.WalkingTestViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val module = module {

    single<AirKoreaApi> { RetrofitClient.api }

    viewModel { SplashViewModel(androidApplication()) }
    viewModel { MainViewModel(get(),androidApplication()) }
    viewModel { InfoInputViewModel(get(), androidApplication()) }
    viewModel { BirthdayViewModel(androidApplication()) }
    viewModel { GenderViewModel(androidApplication()) }
    viewModel { BodyViewModel(androidApplication()) }
    viewModel { LungExerciseViewModel(androidApplication()) }
    viewModel { WalkingTestViewModel(get(), get(), androidApplication()) }
    viewModel { BreathingViewModel(get(), androidApplication()) }
    viewModel { HistoryViewModel(get(), androidApplication()) }
    viewModel { VideoViewModel(androidApplication()) }
    viewModel { EditInfoViewModel(get(), androidApplication()) }
    viewModel { HistoryRecordViewModel(get(),androidApplication()) }
    viewModel { WalkHistoryViewModel(get(),get(),get(), androidApplication()) }
    viewModel { DeveloperViewModel(get(), androidApplication()) }
    viewModel { SettingViewModel(androidApplication(), get()) }
    viewModel { InsightViewModel(androidApplication(), get()) }
    viewModel { FitPlanViewModel(get(), get(), androidApplication()) }
    viewModel { FitExerciseViewModel(get(), androidApplication()) }
    viewModel { WalkingResultViewModel(get(),androidApplication()) }
    viewModel { FitResultViewModel(get(), androidApplication()) }

}