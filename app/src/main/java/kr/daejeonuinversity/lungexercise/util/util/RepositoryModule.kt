package kr.daejeonuinversity.lungexercise.util.util

import kr.daejeonuinversity.lungexercise.data.repository.BreathRepository
import org.koin.dsl.module

val repositoryModule = module{
    single { BreathRepository(get()) }
}