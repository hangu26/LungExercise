package kr.daejeonuinversity.lungexercise.util.util

import kr.daejeonuinversity.lungexercise.data.local.repository.PreferenceRepo
import kr.daejeonuinversity.lungexercise.data.repository.AirRepository
import kr.daejeonuinversity.lungexercise.data.repository.BreathRepository
import kr.daejeonuinversity.lungexercise.data.repository.DeveloperRepository
import kr.daejeonuinversity.lungexercise.data.repository.FitExerciseRepository
import kr.daejeonuinversity.lungexercise.data.repository.InfoRepository
import kr.daejeonuinversity.lungexercise.data.repository.SixWalkTestRepository
import kr.daejeonuinversity.lungexercise.data.repository.TotalStepRepository
import org.koin.core.scope.get
import org.koin.dsl.module

val repositoryModule = module {
    single { BreathRepository(get()) }
    single { InfoRepository(get()) }
    single { PreferenceRepo(get()) }
    single { DeveloperRepository(get(), get()) }
    single { AirRepository(get()) }
    single { SixWalkTestRepository(get()) }
    single { FitExerciseRepository(get(), get()) }
    single { TotalStepRepository(get()) }


}