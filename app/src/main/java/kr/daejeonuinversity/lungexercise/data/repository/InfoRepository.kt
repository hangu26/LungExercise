package kr.daejeonuinversity.lungexercise.data.repository

import kr.daejeonuinversity.lungexercise.data.local.dao.UserInfoDao
import kr.daejeonuinversity.lungexercise.data.local.entity.UserInfo

class InfoRepository(private val dao : UserInfoDao) {

    suspend fun insertUserInfo(birthdayDate : String, gender : String, stature : Int, weight : Int, screeningNum : String, initial : String, visit : String){

        val userInfo = UserInfo(
            birthday = birthdayDate,
            gender = gender,
            height = stature,
            weight = weight,
            screeningNum = screeningNum,
            initial = initial,
            visit = visit
        )

        dao.insert(userInfo)

    }

    suspend fun getUserDates(): UserInfo? {
        return dao.getUserInfo()
    }

}