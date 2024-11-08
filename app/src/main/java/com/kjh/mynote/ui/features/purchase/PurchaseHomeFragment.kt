package com.kjh.mynote.ui.features.purchase

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.view.MonthScrollListener
import com.kjh.mynote.databinding.FragmentPurchaseBinding
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.purchase.make.MakePurchaseNoteActivity
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.toStringWithPattern
import com.naver.maps.map.overlay.Overlay.OnClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

@AndroidEntryPoint
class PurchaseHomeFragment: BaseFragment<FragmentPurchaseBinding>({ FragmentPurchaseBinding.inflate(it) }) {

    private val viewModel: PurchaseHomeViewModel by viewModels()

    override fun onInitView() {
        with (binding) {
            calendarMonthView.setMonthScrollListener(monthScrollListener)
            calendarMonthView.setDayClickAction(monthDayClickAction)

            fabMakePurchaseNote.setOnThrottleClickListener(makePurchaseFabClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentMonth.collect { currentDate ->
                        binding.tvCurrentYearMonth.text = currentDate.toStringWithPattern("yyyy년 MM월")
                    }
                }

                launch {
                    viewModel.selectedDay.collect { selectedDay ->
                        binding.calendarMonthView.setSelectedDay(selectedDay)
                    }
                }
            }
        }
    }

    private val monthScrollListener: (CalendarMonth) -> Unit = { date ->
        Timber.tag("abc123").e("monthScrolled.. $date")
        viewModel.setCurrentMonth(date.yearMonth)
    }

    private val monthDayClickAction: (LocalDate) -> Unit = { date ->
        viewModel.setSelectedDay(date)
    }

    private val makePurchaseFabClickListener = View.OnClickListener {
        Intent(requireContext(), MakePurchaseNoteActivity::class.java).apply {
            startActivity(this)
        }
    }

    companion object {
        const val TAG = "PurchaseHomeFragment"

        fun newInstance() = PurchaseHomeFragment()
    }
}