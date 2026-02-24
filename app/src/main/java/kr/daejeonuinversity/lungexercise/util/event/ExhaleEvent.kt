package kr.daejeonuinversity.lungexercise.util.event

sealed class ExhaleEvent {
    object Start : ExhaleEvent()
    data class End(
        val duration: Long,
        val fvc: Double,
        val fev1: Double,
        val ratio: Double,
        val pressure: Double
    ) : ExhaleEvent()
}