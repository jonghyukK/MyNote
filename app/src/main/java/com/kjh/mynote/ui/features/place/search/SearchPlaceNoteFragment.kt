package com.kjh.mynote.ui.features.place.search

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View.OnClickListener
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.FragmentSearchPlaceNoteBinding
import com.kjh.mynote.ui.base.BaseDialogFragment
import com.kjh.mynote.ui.base.DialogType
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailActivity
import com.kjh.mynote.ui.features.place.search.adapter.SearchPlaceNoteListAdapter
import com.kjh.mynote.ui.features.search.SearchPlaceNotesWithFiltersActivity
import com.kjh.mynote.utils.SpacingItemDecoration
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.hideKeyboard
import com.kjh.mynote.utils.extensions.highlightText
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showKeyboard
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
class SearchPlaceNoteFragment: BaseDialogFragment<FragmentSearchPlaceNoteBinding>(
    { FragmentSearchPlaceNoteBinding.inflate(it) },
    DialogType.FULL_SCREEN
) {

    private val viewModel: SearchPlaceNoteViewModel by viewModels()

    private val listAdapter: SearchPlaceNoteListAdapter by lazy {
        SearchPlaceNoteListAdapter(searchResultListItemClickAction)
    }

    override fun onInitView() = with (binding) {
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
            setOnTouchListener { _, _ ->
                hideKeyboard()
                false
            }
        }

        etSearch.setOnEditorActionListener(searchEditorActionListener)
        etSearch.addTextChangedListener(searchTextWatcher)

        ivBack.setOnThrottleClickListener(backButtonClickListener)
        ivClear.setOnThrottleClickListener(textClearButtonClickListener)
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    binding.etSearch.showKeyboard()
                }

                launch {
                    viewModel.uiState.collect { uiState ->
                        updateVisibilityBy(uiState)

                        when (uiState) {
                            is SearchUiState.Init,
                            is SearchUiState.Empty,
                            is SearchUiState.Loading -> {
                                listAdapter.submitList(null)
                            }
                            is SearchUiState.Error -> {
                                listAdapter.submitList(null)
                                uiState.msg?.let { showToast(it) }
                            }
                            is SearchUiState.Success -> {
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

    override fun onStart() {
        super.onStart()
        binding.etSearch.requestFocus()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    private fun hideKeyboard() {
        binding.etSearch.hideKeyboard()
    }

    private fun updateVisibilityBy(uiState: SearchUiState) = with (binding) {
        clInitContainer.isVisible = uiState is SearchUiState.Init
        layoutEmptyResults.root.isVisible = uiState is SearchUiState.Empty
        layoutLoading.root.isVisible = uiState is SearchUiState.Loading
    }

    private fun handleSearchAction() {
        val query = binding.etSearch.text.toString()
        if (query.isBlank()) {
            showToast(getString(R.string.input_search_text))
            return
        }

        Intent(requireContext(), SearchPlaceNotesWithFiltersActivity::class.java).apply {
            putExtra(AppConstants.INTENT_QUERY_TEXT, viewModel.searchQuery.value)
            startActivity(this)
        }
    }

    private val searchTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setSearchQuery(s.toString())
        }
    }

    private val searchEditorActionListener = OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            handleSearchAction()
            true
        } else {
            false
        }
    }

    private val searchResultListItemClickAction: (SearchResultItem) -> Unit = { resultItem ->
        hideKeyboard()
        Intent(requireContext(), PlaceNoteDetailActivity::class.java).apply {
            putExtra(AppConstants.INTENT_NOTE_ID, resultItem.item.id)
            startActivity(this)
        }
    }

    private val backButtonClickListener = OnClickListener {
        hideKeyboard()
        dismiss()
    }

    private val textClearButtonClickListener = OnClickListener {
        binding.etSearch.text?.clear()
        viewModel.clearSearchQuery()
    }

    companion object {
        const val TAG = "SearchPlaceNoteFragment"

        fun newInstance() = SearchPlaceNoteFragment()
    }
}