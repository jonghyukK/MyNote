package com.kjh.mynote.ui.features.search

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.FragmentSearchBinding
import com.kjh.mynote.ui.base.BaseFragment
import com.kjh.mynote.ui.features.place.detail.PlaceNoteDetailActivity
import com.kjh.mynote.ui.features.search.adapter.SearchResultListAdapter
import com.kjh.mynote.utils.SpacingItemDecoration
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 6..
 * Description:
 */

@AndroidEntryPoint
class SearchFragment: BaseFragment<FragmentSearchBinding>({ FragmentSearchBinding.inflate(it) }) {

    private val viewModel: SearchViewModel by viewModels()

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
        }
    }

    override fun onInitData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            is SearchUiState.Empty -> {
                                binding.tvEmptyResults.isVisible = true
                                binding.layoutLoading.clLoading.isVisible = false
                                listAdapter.submitList(null)
                            }
                            is SearchUiState.Error -> {
                                binding.tvEmptyResults.isVisible = true
                                binding.layoutLoading.clLoading.isVisible = false
                                listAdapter.submitList(null)
                                Toast.makeText(requireContext(), uiState.msg, Toast.LENGTH_SHORT).show()
                            }
                            is SearchUiState.Loading -> {
                                binding.tvEmptyResults.isVisible = false
                                binding.layoutLoading.clLoading.isVisible = true
                            }
                            is SearchUiState.Results -> {
                                binding.tvEmptyResults.isVisible = false
                                binding.layoutLoading.clLoading.isVisible = false
                                listAdapter.submitList(uiState.data)
                            }
                        }
                    }
                }
            }
        }
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

    companion object {
        const val TAG = "SearchFragment"

        fun newInstance() = SearchFragment()
    }
}