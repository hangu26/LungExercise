package kr.daejeonuinversity.lungexercise.util.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ItemCalendarDayBinding
import kr.daejeonuinversity.lungexercise.model.CalendarDay
import java.time.LocalDate

class CalendarAdapter(private val onDateClickListener: (CalendarDay) -> Unit) :
    ListAdapter<CalendarDay, CalendarAdapter.DayViewHolder>(DiffCallback()) {

    private var selectedDay: Int? = null

    fun removedDataSet(date: LocalDate) {
        val updatedList = currentList.map {
            if (it.day == date.dayOfMonth) {
                it.copy(isRecorded = false)
            } else {
                it
            }
        }

        selectedDay = null  // 선택 초기화
        submitList(updatedList) // 새로운 리스트로 갱신
    }

    inner class DayViewHolder(private val binding: ItemCalendarDayBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(item: CalendarDay) {
            binding.tvDay.text = item.day?.toString() ?: ""

            if (item.day != null && item.day == selectedDay) {
                binding.tvDay.setBackgroundResource(R.drawable.background_clicked_calendar)
            } else if (item.isRecorded) {
                binding.tvDay.setBackgroundResource(R.drawable.background_recorded_calendar)
            } else {
                binding.tvDay.background = null
            }

            binding.root.setOnClickListener {
                item.day?.let { day ->
                    val wasSelected = selectedDay == day
                    selectedDay = if (wasSelected) null else day
                    onDateClickListener(item.copy(day = day))
                    notifyDataSetChanged()
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding =
            ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<CalendarDay>() {
        override fun areItemsTheSame(oldItem: CalendarDay, newItem: CalendarDay) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: CalendarDay, newItem: CalendarDay) =
            oldItem == newItem
    }
}