package kr.daejeonuinversity.lungexercise.util.event

sealed class ExhaleEvent {
    object Start : ExhaleEvent()
    data class End(val duration: Long) : ExhaleEvent()
}