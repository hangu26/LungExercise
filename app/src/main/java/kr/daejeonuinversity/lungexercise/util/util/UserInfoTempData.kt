package kr.daejeonuinversity.lungexercise.util.util

object UserInfoTempData {
    var birthday: String = ""
    var gender: String = ""
    var stature: Int = 0
    var weight: Int = 0
    var screeningNum: String = ""
    var initial: String = ""
    var visit: String = ""

    fun clear() {
        birthday = ""
        gender = ""
        stature = 0
        weight = 0
    }
}