package kr.daejeonuinversity.lungexercise.data.repository

import kr.daejeonuinversity.lungexercise.data.local.dao.BreathRecordDao
import kr.daejeonuinversity.lungexercise.data.local.dao.UserInfoDao
import kr.daejeonuinversity.lungexercise.data.local.entity.BreathRecord
import kr.daejeonuinversity.lungexercise.data.local.entity.UserInfo
import java.time.LocalDate

class DeveloperRepository(private val userDao : UserInfoDao, private val breathDao: BreathRecordDao) {

    suspend fun insertUserInfo(birthdayDate : String, gender : String, stature : Int, weight : Int){

        val userInfo = UserInfo(
            birthday = birthdayDate,
            gender = gender,
            height = stature,
            weight = weight
        )

        userDao.insert(userInfo)

    }

    suspend fun getUserDates(): UserInfo? {
        return userDao.getUserInfo()
    }

    suspend fun getAllRecordedDates(): List<String> {
        return breathDao.getAllDates()
    }

    suspend fun getBreathRecordsByDates(dates: List<String>): List<BreathRecord> {
        return breathDao.getBreathRecordsByDates(dates)
    }

    suspend fun removeClickedData(date: LocalDate) {

        val dateString = date.toString()
        val exists = breathDao.existsByDate(dateString)
        if (exists) {
            breathDao.deleteByDate(dateString)
        }

    }

    suspend fun insertOrUpdateBreathRecord(time: Int, isClear: Int, date: String) {
        val existingRecord = breathDao.getRecordByDate(date)

        if (existingRecord != null) {
            // 기존 데이터 업데이트
            val newTotalCount = existingRecord.totalCount + 1
            val newTotalTime = existingRecord.totalTime + time
            val newAverage = newTotalTime / newTotalCount
            val newClear = existingRecord.clear + isClear

            val updatedRecord = existingRecord.copy(
                totalCount = newTotalCount,
                totalTime = newTotalTime,
                average = newAverage,
                clear = newClear
            )

            breathDao.insertOrUpdate(updatedRecord)

        } else {
            // 새 데이터 생성
            val newRecord = BreathRecord(
                date = date,
                totalCount = 1,
                totalTime = time,
                average = time,
                clear = isClear
            )
            breathDao.insertOrUpdate(newRecord)
        }
    }

}