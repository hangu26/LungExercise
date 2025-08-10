package kr.daejeonuinversity.lungexercise.view.history.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.FragmentWalkHistoryBinding
import kr.daejeonuinversity.lungexercise.util.adapter.CalendarAdapter
import kr.daejeonuinversity.lungexercise.util.base.BaseFragment
import kr.daejeonuinversity.lungexercise.viewmodel.WalkHistoryViewModel
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.YearMonth

class WalkHistoryFragment : BaseFragment<FragmentWalkHistoryBinding>(R.layout.fragment_walk_history) {

    private val wViewModel : WalkHistoryViewModel by inject()
    private var isClickedDate: LocalDate? = null
    private val calendarAdapter = CalendarAdapter { calendarDay ->
        val day = calendarDay.day ?: return@CalendarAdapter
        val yearMonth = wViewModel.currentYearMonth.value ?: YearMonth.now()
        val clickedDate = yearMonth.atDay(day)

        isClickedDate = clickedDate

        wViewModel.loadWeeklyBreathData(clickedDate)
    }

    override fun initView() {

        binding.apply {
            fragment = this@WalkHistoryFragment
            viewmodel = wViewModel
            calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
            calendarRecyclerView.adapter = calendarAdapter
        }

        observe()

    }

    private fun observe() = wViewModel.let { vm ->

        vm.calendarDays.observe(viewLifecycleOwner) { days ->
            calendarAdapter.submitList(days)
        }

        vm.currentYearMonth.observe(viewLifecycleOwner) { yearMonth ->
            val text = "${yearMonth.monthValue}월 ${yearMonth.year}"
            binding.tvCurrentMonth.text = text
        }

    }

}