package kr.daejeonuinversity.lungexercise.util.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import kr.daejeonuinversity.lungexercise.view.history.fragment.BreathHistoryFragment
import kr.daejeonuinversity.lungexercise.view.history.fragment.WalkHistoryFragment

class TabPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BreathHistoryFragment()
            else -> WalkHistoryFragment()
        }
    }
}