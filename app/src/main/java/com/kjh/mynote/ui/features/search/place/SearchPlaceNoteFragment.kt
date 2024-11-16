package com.kjh.mynote.ui.features.search.place

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.FragmentSearchPlaceNoteBinding
import com.kjh.mynote.ui.base.BaseDialogFragment
import com.kjh.mynote.ui.base.DialogType
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailActivity
import com.kjh.mynote.ui.features.search.place.adapter.SearchResultListAdapter
import com.kjh.mynote.utils.SpacingItemDecoration
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.hideKeyboard
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 6..
 * Description:
 */

@AndroidEntryPoint
class SearchPlaceNoteFragment: BaseDialogFragment<FragmentSearchPlaceNoteBinding>(
    { FragmentSearchPlaceNoteBinding.inflate(it) },
    DialogType.FULL_SCREEN
) {

    private val viewModel: SearchPlaceNoteViewModel by viewModels()

    private val listAdapter: SearchResultListAdapter by lazy {
        SearchResultListAdapter(searchResultClickAction)
    }

    override fun onInitView() {
        with (binding) {
            rvSearchResults.apply {
                itemAnimator = null
                addItemDecoration(SpacingItemDecoration(bottom = 15, exceptFirstItem = false))
                adapter = listAdapter
            }

            etSearch.addTextChangedListener(searchTextWatcher)

            ivBack.setOnThrottleClickListener(backButtonClickListener)
            ivClear.setOnThrottleClickListener(textClearButtonClickListener)
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            is SearchUiState.Empty -> {
                                setResultEmptyUi()
                            }
                            is SearchUiState.Error -> {
                                setResultEmptyUi()
                                Toast.makeText(requireContext(), uiState.msg, Toast.LENGTH_SHORT).show()
                            }
                            is SearchUiState.Loading -> {
                                setLoadingUi()
                            }
                            is SearchUiState.Results -> {
                                setExistResultsUi()
                                listAdapter.submitList(uiState.data)
                            }
                        }
                    }
                }

                launch {
                    viewModel.searchQuery.collectLatest { queryText ->
                        binding.ivClear.isVisible = queryText.isNotEmpty()
                    }
                }
            }
        }
    }

    private fun setResultEmptyUi() = with (binding) {
        tvEmptyResults.isVisible = true
        layoutLoading.clLoading.isVisible = false
        listAdapter.submitList(null)
    }

    private fun setLoadingUi() = with (binding) {
        tvEmptyResults.isVisible = false
        layoutLoading.clLoading.isVisible = true
    }

    private fun setExistResultsUi() = with (binding) {
        tvEmptyResults.isVisible = false
        layoutLoading.clLoading.isVisible = false
    }

    private val searchTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setSearchQuery(s.toString())
        }
    }

    private val searchResultClickAction: (SearchResultItem) -> Unit = { resultItem ->
        binding.etSearch.hideKeyboard()
        Intent(requireContext(), PlaceNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_NOTE_ID, resultItem.item.id)
            startActivity(this)
        }
    }

    private val backButtonClickListener = OnClickListener {
        binding.etSearch.hideKeyboard()
        dismiss()
    }

    private val textClearButtonClickListener = OnClickListener {
        viewModel.clearSearchQuery()
        binding.etSearch.text?.clear()
        setResultEmptyUi()
    }

    companion object {
        const val TAG = "SearchFragment"

        fun newInstance() = SearchPlaceNoteFragment()
    }
}