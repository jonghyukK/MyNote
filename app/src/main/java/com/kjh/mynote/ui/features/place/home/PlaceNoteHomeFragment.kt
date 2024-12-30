package com.kjh.mynote.ui.features.place.home

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.FragmentPlaceNoteCalendarBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.place.home.list.PlaceNoteListTypeFragment
import com.kjh.mynote.ui.features.place.home.weekview.PlaceNoteWeekViewTypeFragment
import com.kjh.mynote.ui.features.place.make.MakeOrModifyPlaceNoteActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.makeGone
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 31..
 * Description:
 */

@AndroidEntryPoint
class PlaceNoteHomeFragment: BaseFragment<FragmentPlaceNoteCalendarBinding>({ FragmentPlaceNoteCalendarBinding.inflate(it) }) {

    private val viewModel: PlaceNoteHomeViewModel by viewModels()

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
                    viewModel.placeNotesUiState.collect { uiState ->
                        when (uiState) {
                            is PlaceNotesUiState.Loading -> {
                                binding.layoutLoading.root.makeVisible()
                            }
                            is PlaceNotesUiState.Error -> {
                                binding.layoutLoading.root.makeGone()
                                uiState.error.message?.let {
                                    showToast(it)
                                }
                            }
                            is PlaceNotesUiState.Success -> {
                                binding.layoutLoading.root.makeGone()
                            }
                        }
                    }
                }

                launch {
                    viewModel.displayType.collect { viewType ->
                        changeFragmentBy(viewType)
                        toggleDisplayTypeFabIcon(viewType)
                    }
                }
            }
        }
    }

    private fun changeFragmentBy(viewType: DisplayType) {
        val targetFragmentType = when (viewType) {
            DisplayType.WEEK_VIEW -> CalendarChildFragments.WEEK_VIEW_TYPE_FRAGMENT
            DisplayType.LIST -> CalendarChildFragments.LIST_TYPE_FRAGMENT
        }

        var targetFragment = childFragmentManager.findFragmentByTag(targetFragmentType.tag)

        childFragmentManager.commit {
            if (targetFragment == null) {
                targetFragment = getFragmentBy(targetFragmentType)
                add(R.id.fcv_container, targetFragment!!, targetFragmentType.tag)
            }

            targetFragment?.let { show(it) }

            CalendarChildFragments.entries
                .filterNot { it == targetFragmentType }
                .forEach { type ->
                    childFragmentManager.findFragmentByTag(type.tag)?.let { hide(it) }
                }
        }
    }

    private fun getFragmentBy(type: CalendarChildFragments) = when (type) {
        CalendarChildFragments.WEEK_VIEW_TYPE_FRAGMENT -> {
            PlaceNoteWeekViewTypeFragment.newInstance()
        }
        CalendarChildFragments.LIST_TYPE_FRAGMENT -> {
            PlaceNoteListTypeFragment.newInstance()
        }
    }

    private fun toggleDisplayTypeFabIcon(viewType: DisplayType) {
        val icon = when (viewType) {
            DisplayType.WEEK_VIEW ->
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_list_24)
            DisplayType.LIST ->
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_calendar_24_purple)
        }

        binding.fabChangeViewType.setImageDrawable(icon)
    }

    private val makeNoteResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val insertedPlaceNoteItem = result.data?.parcelable<PlaceNoteUiModel>(
                AppConstants.INTENT_PLACE_NOTE_ITEM
            ) ?: return@registerForActivityResult

            viewModel.updateCurrentDate(insertedPlaceNoteItem.localDate)
        }
    }

    private val changeViewTypeFabButtonClickListener = View.OnClickListener {
        viewModel.changeViewType()
    }

    private val makeNoteFabButtonClickListener = View.OnClickListener {
        val currentDate = viewModel.currentDate.value

        Intent(requireContext(), MakeOrModifyPlaceNoteActivity::class.java).apply {
            putExtra(AppConstants.INTENT_PLACE_VISIT_DATE, currentDate.toMillis())
            makeNoteResultLauncher.launch(this)
        }
    }

    companion object {
        const val TAG = "PlaceNoteCalendarHomeFragment"

        enum class CalendarChildFragments(val tag: String) {
            LIST_TYPE_FRAGMENT(PlaceNoteListTypeFragment.TAG),
            WEEK_VIEW_TYPE_FRAGMENT(PlaceNoteWeekViewTypeFragment.TAG)
        }

        fun newInstance(): PlaceNoteHomeFragment {
            return PlaceNoteHomeFragment()
        }
    }
}