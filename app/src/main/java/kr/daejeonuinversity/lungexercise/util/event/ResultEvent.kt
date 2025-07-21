package kr.daejeonuinversity.lungexercise.util.event

sealed class ResultEvent {
    object ShowResultDialog : ResultEvent()
    object ShowResultToast : ResultEvent()
}