package com.kjh.mynote.ui.features.place.search

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityPlaceNoteSearchBinding
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.features.place.search.autocomplete.PlaceNoteSearchAutoCompleteFragment
import com.kjh.mynote.ui.features.place.search.result.PlaceNoteSearchResultFragment
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.hideKeyboard
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PlaceNoteSearchActivity : BaseActivity<ActivityPlaceNoteSearchBinding>({ ActivityPlaceNoteSearchBinding.inflate(it) }) {

    private val viewModel: PlaceNoteSearchViewModel by viewModels()

    override fun onInitView() {
        with (binding) {
            etSearch.requestFocus()
            etSearch.setOnEditorActionListener(searchEditorActionListener)
            etSearch.addTextChangedListener(searchTextWatcher)

            ivBack.setOnThrottleClickListener(backButtonClickListener)
            ivClear.setOnThrottleClickListener(textClearButtonClickListener)
        }
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.searchQuery.collectLatest { queryText ->
                        binding.ivClear.isVisible = queryText.isNotBlank()
                    }
                }

                launch {
                    viewModel.isQueryChangedAfterSearchAction.collect { isQueryChanged ->
                        if (isQueryChanged) {
                            replaceToAutoCompleteFragment()
                            viewModel.clearActionQuery()
                        }
                    }
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus

            if (v is AppCompatEditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun handleSearchAction() {
        val query = binding.etSearch.text.toString()
        if (query.isBlank()) {
            showToast(getString(R.string.input_search_text))
            return
        }

        binding.etSearch.hideKeyboard()

        viewModel.saveActionQuery()

        replaceToResultFragment()
    }

    private fun replaceToAutoCompleteFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<PlaceNoteSearchAutoCompleteFragment>(
                containerViewId = R.id.fcv_container,
                tag = PlaceNoteSearchAutoCompleteFragment.TAG
            )
        }
    }

    private fun replaceToResultFragment() {
        val bundle = bundleOf(AppConstants.INTENT_QUERY_TEXT to viewModel.searchQuery.value)

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<PlaceNoteSearchResultFragment>(
                containerViewId = R.id.fcv_container,
                tag = PlaceNoteSearchResultFragment.TAG,
                args = bundle
            )
        }
    }

    private val searchTextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setSearchQuery(s.toString())
        }
    }

    private val searchEditorActionListener = TextView.OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            handleSearchAction()
            true
        } else {
            false
        }
    }

    private val backButtonClickListener = View.OnClickListener {
        finish()
    }

    private val textClearButtonClickListener = View.OnClickListener {
        binding.etSearch.text?.clear()
    }
}