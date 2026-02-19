package kr.daejeonuinversity.lungexercise.view.history

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityHistoryRecordBinding
import kr.daejeonuinversity.lungexercise.util.adapter.TabPagerAdapter
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.HistoryRecordViewModel
import org.koin.android.ext.android.inject

class HistoryRecordActivity :
    BaseActivity<ActivityHistoryRecordBinding>(R.layout.activity_history_record) {

    private val backPressedCallback = BackPressedCallback(this)
    private val hViewModel : HistoryRecordViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            activity = this@HistoryRecordActivity
            viewmodel = hViewModel
            lifecycleOwner = this@HistoryRecordActivity
        }

        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        val adapter = TabPagerAdapter(this)
        viewPager.adapter = adapter

        // 탭 생성
        val titles = listOf("호흡 활동량", "보행 활동량")
        val icons = listOf(R.drawable.icon_breath_tab, R.drawable.icon_step)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.customView =
                createCustomTab(titles[position], icons[position], isSelected = position == 0)
        }.attach()

        // 탭 선택 리스너
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateTabStyle(tab, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                updateTabStyle(tab, false)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        observe()
        backPressedCallback.addCallbackActivity(this, MainActivity::class.java)
    }

    override fun onStart() = hViewModel.let{ vm ->
        super.onStart()
        vm.startReceiving()
        vm.requestStepsFromWatch()
    }

    override fun onStop() {
        super.onStop()
        hViewModel.stopReceiving()
    }

    private fun observe() = hViewModel.let{ vm ->

        vm.btnBackState.observe(this@HistoryRecordActivity){
            if (it){

                val intent = Intent(this@HistoryRecordActivity, MainActivity::class.java)
                startActivityBackAnimation(intent,this@HistoryRecordActivity)
                finish()

            }
        }

    }

    private fun createCustomTab(text: String, iconRes: Int, isSelected: Boolean): View {
        val view = LayoutInflater.from(this).inflate(R.layout.history_tab_custom, null)
        val tabText = view.findViewById<TextView>(R.id.tabText)
        val tabIcon = view.findViewById<ImageView>(R.id.tabIcon)

        tabText.text = text
        tabIcon.setImageResource(iconRes)

        updateTabViewStyle(view, isSelected)
        return view
    }

    private fun updateTabStyle(tab: TabLayout.Tab, isSelected: Boolean) {
        val view = tab.customView ?: return
        updateTabViewStyle(view, isSelected)
    }

    private fun updateTabViewStyle(view: View, isSelected: Boolean) {
        val text = view.findViewById<TextView>(R.id.tabText)
        val layout = view as LinearLayout

        // 배경 설정
        layout.setBackgroundResource(
            if (isSelected) R.drawable.border_not_clicked_tab_item else R.drawable.border_tab_item
        )

        // 텍스트 색상 설정
        text.setTextColor(
            ContextCompat.getColor(
                view.context,
                if (isSelected) R.color.color_clicked_tab_text else R.color.color_not_clicked_tab_text
            )
        )

        // 높이 설정
        val newHeight = if (isSelected) {
            view.context.resources.getDimensionPixelSize(R.dimen.height_tab_layout_item) // 40dp
        } else {
            ViewGroup.LayoutParams.WRAP_CONTENT
        }

        val newParams = layout.layoutParams ?: ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            newHeight
        )
        newParams.height = newHeight
        layout.layoutParams = newParams
    }

}