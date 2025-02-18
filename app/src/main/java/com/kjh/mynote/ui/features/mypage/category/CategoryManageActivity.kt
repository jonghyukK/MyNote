package com.kjh.mynote.ui.features.mypage.category

import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.databinding.ActivityCategoryManageBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.UiState
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.common.dialog.categorymanage.CategoryAddOrDeleteOrEditDialog
import com.kjh.mynote.ui.common.dialog.categorymanage.CategoryDialogType
import com.kjh.mynote.ui.features.mypage.category.adapter.MyCategoryListAdapter
import com.kjh.mynote.utils.decorations.UnderLineDecoration
import com.kjh.mynote.utils.extensions.makeGone
import com.kjh.mynote.utils.extensions.makeVisible
import com.kjh.mynote.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 30..
 * Description:
 */

@AndroidEntryPoint
class CategoryManageActivity :
    BaseActivity<ActivityCategoryManageBinding>({ ActivityCategoryManageBinding.inflate(it) }) {

    private val viewModel: CategoryManageViewModel by viewModels()

    private val listAdapter: MyCategoryListAdapter by lazy {
        MyCategoryListAdapter(
            editClickAction = editClickAction,
            deleteClickAction = deleteClickAction
        )
    }

    override fun onInitView() {
        with (binding) {
            rvCategories.apply {
                itemAnimator = null
                adapter = listAdapter
                addItemDecoration(UnderLineDecoration(this@CategoryManageActivity, height = 1))
            }

            tbToolbar.setRightFirstButtonClickListener(addCategoryClickListener)
        }
    }

    override fun onInitUiData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.errorMessage.collectLatest(::showToast)
                }

                launch {
                    viewModel.uiState.collect { uiState ->
                        when (uiState) {
                            UiState.Loading -> {
                                binding.layoutLoading.root.makeVisible()
                            }
                            UiState.Error -> {
                                binding.layoutLoading.root.makeGone()
                            }
                            is UiState.Success -> {
                                binding.layoutLoading.root.makeGone()
                                listAdapter.submitList(uiState.data)
                            }
                        }
                    }
                }
            }
        }
    }

    private val editClickAction: (CategoryUiModel) -> Unit = { categoryItem ->
        CategoryAddOrDeleteOrEditDialog.newInstance(
            dialogType = CategoryDialogType.MODIFY,
            categoryItem = categoryItem
        ).show(supportFragmentManager, CategoryAddOrDeleteOrEditDialog.TAG)
    }

    private val deleteClickAction: (CategoryUiModel) -> Unit = { categoryItem ->
        CategoryAddOrDeleteOrEditDialog.newInstance(
            dialogType = CategoryDialogType.DELETE,
            categoryItem = categoryItem
        ).show(supportFragmentManager, CategoryAddOrDeleteOrEditDialog.TAG)
    }

    private val addCategoryClickListener = View.OnClickListener {
        CategoryAddOrDeleteOrEditDialog.newInstance(
            dialogType = CategoryDialogType.ADD
        ).show(supportFragmentManager, CategoryAddOrDeleteOrEditDialog.TAG)
    }
}