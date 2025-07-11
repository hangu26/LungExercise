package kr.daejeonuinversity.lungexercise.util.util

import android.content.Context
import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import kr.daejeonuinversity.lungexercise.R
import java.util.Calendar

class ShowCustomDatePicker {

    fun showCustomDatePicker(context: Context, onDateSelected: (String) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_custom_date_picker, null)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.year)
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.month)
        val dayPicker = dialogView.findViewById<NumberPicker>(R.id.day)

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        // 설정
        yearPicker.minValue = 1900
        yearPicker.maxValue = currentYear
        yearPicker.value = 2000

        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = 1

        dayPicker.minValue = 1
        dayPicker.maxValue = getDaysInMonth(yearPicker.value, monthPicker.value)
        dayPicker.value = 1

        // 리스너 설정 - 월, 년도 변경 시 일수 조정
        val updateDayPicker = {
            val maxDay = getDaysInMonth(yearPicker.value, monthPicker.value)
            val currentDay = dayPicker.value
            dayPicker.maxValue = maxDay
            if (currentDay > maxDay) dayPicker.value = maxDay
        }

        yearPicker.setOnValueChangedListener { _, _, _ -> updateDayPicker() }
        monthPicker.setOnValueChangedListener { _, _, _ -> updateDayPicker() }

        AlertDialog.Builder(context)
            .setTitle("생년월일 선택")
            .setView(dialogView)
            .setPositiveButton("확인") { _, _ ->
                val selectedDate = String.format("%04d%02d%02d", yearPicker.value, monthPicker.value, dayPicker.value)
                onDateSelected(selectedDate)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 월별 일수 계산 (윤년 고려)
    private fun getDaysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }
    }

    // 윤년 판별 함수
    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }


}