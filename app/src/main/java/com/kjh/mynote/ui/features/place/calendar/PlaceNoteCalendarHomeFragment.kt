package com.kjh.mynote.ui.features.place.calendar

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.FragmentPlaceNoteCalendarBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.place.calendar.list.PlaceNoteListTypeFragment
import com.kjh.mynote.ui.features.place.calendar.weekview.PlaceNoteWeekViewTypeFragment
import com.kjh.mynote.ui.features.place.make.MakeOrModifyPlaceNoteActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 31..
 * Description:
 */

@AndroidEntryPoint
class PlaceNoteCalendarHomeFragment: BaseFragment<FragmentPlaceNoteCalendarBinding>({ FragmentPlaceNoteCalendarBinding.inflate(it) }) {

    private val viewModel: PlaceNoteCalendarHomeViewModel by activityViewModels()

    override fun onInitView() {
        with (binding) {
            fabChangeViewType.setOnThrottleClickListener(changeViewTypeFabButtonClickListener)
            fabMakeNote.setOnThrottleClickListener(makeNoteFabButtonClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.displayType.collect { viewType ->
                        when (viewType) {
                            DisplayType.WEEK_VIEW -> {
                                binding.fabChangeViewType.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_list_24))
                                replaceFragmentBy(PlaceNoteWeekViewTypeFragment.TAG)
                            }
                            DisplayType.LIST -> {
                                binding.fabChangeViewType.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_calendar_24_purple))
                                replaceFragmentBy(PlaceNoteListTypeFragment.TAG)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun replaceFragmentBy(tag: String) {
        var targetFragment = childFragmentManager.findFragmentByTag(tag)

        childFragmentManager.commit {
            if (targetFragment == null) {
                targetFragment = getFragmentBy(tag)
            }
            replace(R.id.fcv_container, targetFragment!!, tag)
        }
    }

    private fun getFragmentBy(tag: String) = when (tag) {
        PlaceNoteWeekViewTypeFragment.TAG -> PlaceNoteWeekViewTypeFragment.newInstance()
        PlaceNoteListTypeFragment.TAG -> PlaceNoteListTypeFragment.newInstance()
        else -> throw Exception("Wrong Fragment Tag")
    }

    private val makeNoteResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val insertedPlaceNoteItem = result.data?.parcelable<PlaceNoteUiModel>(
                AppConstants.INTENT_PLACE_NOTE_ITEM
            ) ?: return@registerForActivityResult

            viewModel.selectDay(insertedPlaceNoteItem.localDate)
        }
    }

    private val changeViewTypeFabButtonClickListener = View.OnClickListener {
        viewModel.changeViewType()
    }

    private val makeNoteFabButtonClickListener = View.OnClickListener {
        Intent(requireContext(), MakeOrModifyPlaceNoteActivity::class.java).apply {
            makeNoteResultLauncher.launch(this)
        }
    }

    companion object {
        const val TAG = "PlaceNoteCalendarHomeFragment"

        fun newInstance(): PlaceNoteCalendarHomeFragment {
            return PlaceNoteCalendarHomeFragment()
        }
    }
}