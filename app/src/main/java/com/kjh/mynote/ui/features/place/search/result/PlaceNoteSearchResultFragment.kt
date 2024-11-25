package com.kjh.mynote.ui.features.place.search.result

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.view.View.OnClickListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.FragmentPlaceNoteSearchResultBinding
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.common.dialog.SortBSDialog
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailActivity
import com.kjh.mynote.ui.features.place.search.result.adapter.PlaceNoteSearchResultOuterListAdapter
import com.kjh.mynote.ui.features.place.search.result.dialog.PlaceNoteSearchFilterBSDialog
import com.kjh.mynote.utils.SpacingItemDecoration
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.setBackgroundRes
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.setTextColorRes
import com.kjh.mynote.utils.extensions.setTint
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 18..
 * Description: 장소노트 검색 결과 화면
 */

@AndroidEntryPoint
class PlaceNoteSearchResultFragment
    : BaseFragment<FragmentPlaceNoteSearchResultBinding>({
    FragmentPlaceNoteSearchResultBinding.inflate(it)
}), SortBSDialog.SortBSDialogClickListener {

    private val viewModel: SearchPlaceNoteResultViewModel by viewModels()

    private val listAdapter: PlaceNoteSearchResultOuterListAdapter by lazy {
        PlaceNoteSearchResultOuterListAdapter(placeNoteItemClickAction)
    }

    override fun onInitView() {
        with (binding) {
            rvSearchResults.apply {
                adapter = listAdapter
                addItemDecoration(SpacingItemDecoration(top = 20))
            }

            layoutLoading.root.setBackgroundColor(
                ContextCompat.getColor(requireContext(),R.color.black_50))

            layoutEmpty.root.setBackgroundColor(
                ContextCompat.getColor(requireContext(),R.color.black_50))

            clFilterSort.setOnThrottleClickListener(sortFilterClickListener)
            clFilterConditions.setOnThrottleClickListener(conditionFilterClickListener)
        }
    }

    override fun onInitData() {
        viewModel.getFilteredPlaceNotes()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.isLoading }
                        .distinctUntilChanged()
                        .collect {
                            binding.layoutLoading.root.isVisible = it
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.isEmpty() }
                        .distinctUntilChanged()
                        .collect {
                            binding.layoutEmpty.root.isVisible = it
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.isError }
                        .distinctUntilChanged()
                        .collect { errorMsg ->
                            errorMsg?.let {
                                showToast(it)
                                viewModel.shownError()
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.hasAppliedFilters() }
                        .distinctUntilChanged()
                        .collect { hasFilters ->
                            val textColorRes = if (hasFilters) {
                                R.color.purple
                            } else {
                                R.color.black_700
                            }

                            val backgroundRes = if (hasFilters) {
                                R.drawable.ripple_shape_s_white_c_4_l_purple
                            } else {
                                R.drawable.ripple_shape_s_white_c_4_l_black_600
                            }

                            with (binding) {
                                ivNoti.isVisible = hasFilters
                                tvFilter.setTextColorRes(textColorRes)
                                ivFilter.setTint(textColorRes)
                                clFilterConditions.setBackgroundRes(backgroundRes)
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.isDescending }
                        .distinctUntilChanged()
                        .collect {
                            binding.tvSort.text = if (it) {
                                getString(R.string.sort_descending)
                            } else {
                                getString(R.string.sort_ascending)
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.resultItems }
                        .distinctUntilChanged()
                        .collect { resultItems ->
                            listAdapter.submitList(resultItems)
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.resultTotalCount }
                        .distinctUntilChanged()
                        .collect { totalCount ->
                            binding.tvResultsCount.text = getString(R.string.format_total_count, totalCount)
                        }
                }
            }
        }
    }

    private val placeNoteDetailResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.getFilteredPlaceNotes()
        }
    }

    private val placeNoteItemClickAction: (PlaceNoteUiModel) -> Unit = { placeNoteItem ->
        Intent(requireContext(), PlaceNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_NOTE_ID, placeNoteItem.id)
            placeNoteDetailResultLauncher.launch(this)
        }
    }

    private val sortFilterClickListener = OnClickListener {
        SortBSDialog.newInstance(
            currentSort = viewModel.uiState.value.isDescending
        ).show(childFragmentManager, SortBSDialog.TAG)
    }

    private val conditionFilterClickListener = OnClickListener {
        PlaceNoteSearchFilterBSDialog.newInstance()
            .show(childFragmentManager, PlaceNoteSearchFilterBSDialog.TAG)
    }

    override fun onClickSort(isDescending: Boolean) {
        viewModel.applySortFilter(isDescending)
    }

    companion object {
        const val TAG = "PlaceNoteSearchResultFragment"
    }
}