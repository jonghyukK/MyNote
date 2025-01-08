package com.kjh.mynote.ui.features.place.search.autocomplete

import android.content.Intent
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.FragmentPlaceNoteSearchAutoCompleteBinding
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailActivity
import com.kjh.mynote.ui.features.place.search.PlaceNoteSearchViewModel
import com.kjh.mynote.ui.features.place.search.autocomplete.adapter.PlaceNoteSearchAutoCompleteListAdapter
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.highlightText
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 6..
 * Description:
 */

@AndroidEntryPoint
class PlaceNoteSearchAutoCompleteFragment
    : BaseFragment<FragmentPlaceNoteSearchAutoCompleteBinding>({ FragmentPlaceNoteSearchAutoCompleteBinding.inflate(it) }) {

    private val parentViewModel: PlaceNoteSearchViewModel by activityViewModels()
    private val viewModel: PlaceNoteSearchAutoCompleteViewModel by viewModels()

    private val listAdapter: PlaceNoteSearchAutoCompleteListAdapter by lazy {
        PlaceNoteSearchAutoCompleteListAdapter(autoCompleteItemClickAction)
    }

    override fun onInitView() {
        with(binding) {
            tvSearchInfo1.highlightText(
                fullText = tvSearchInfo1.text.toString(),
                wordToHighlight = "특정 단어"
            )

            tvSearchInfo2.highlightText(
                fullText = binding.tvSearchInfo2.text.toString(),
                wordToHighlight = "마트, 공원, 호텔"
            )

            tvSearchInfo3.highlightText(
                fullText = binding.tvSearchInfo3.text.toString(),
                wordToHighlight = "서울, 경기, 평창, 경주"
            )

            rvSearchResults.apply {
                itemAnimator = null
                addItemDecoration(SpacingItemDecoration(bottom = 15, exceptFirstItem = false))
                adapter = listAdapter
            }
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    parentViewModel.searchQuery.collectLatest {
                        viewModel.setSearchQuery(it)
                    }
                }

                launch {
                    viewModel.searchAutoCompleteUiState.collect { uiState ->
                        updateVisibilityBy(uiState)

                        when (uiState) {
                            is SearchAutoCompleteUiState.Init,
                            is SearchAutoCompleteUiState.Empty,
                            is SearchAutoCompleteUiState.Loading -> {
                                listAdapter.submitList(null)
                            }
                            is SearchAutoCompleteUiState.Error -> {
                                listAdapter.submitList(null)

                                uiState.msg?.let {
                                    showToast(it)
                                }
                            }
                            is SearchAutoCompleteUiState.Success -> {
                                listAdapter.submitList(uiState.data)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateVisibilityBy(uiState: SearchAutoCompleteUiState) = with (binding) {
        clInitContainer.isVisible = uiState is SearchAutoCompleteUiState.Init
        layoutEmptyResults.root.isVisible = uiState is SearchAutoCompleteUiState.Empty
        layoutLoading.root.isVisible = uiState is SearchAutoCompleteUiState.Loading
    }

    private val autoCompleteItemClickAction: (PlaceNoteSearchAutoCompleteItem) -> Unit = { resultItem ->
        Intent(requireContext(), PlaceNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_NOTE_ID, resultItem.item.id)
            startActivity(this)
        }
    }

    companion object {
        const val TAG = "SearchPlaceNoteAutoCompleteFragment"
    }
}