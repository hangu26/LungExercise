package kr.daejeonuinversity.lungexercise.util.event

class Event<out T>(private val content: T) {
    private var hasBeenHandled = false

    /** 한 번만 소비할 수 있도록 체크 */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /** 항상 내용을 보고 싶을 때 (디버그 용도 등) */
    fun peekContent(): T = content
}