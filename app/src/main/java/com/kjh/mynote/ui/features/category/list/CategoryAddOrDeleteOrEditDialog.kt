package com.kjh.mynote.ui.features.category.list

import android.os.Bundle
import android.os.Parcelable
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.kjh.mynote.R
import com.kjh.mynote.databinding.DialogAddOrDeleteOrEditCategoryBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.ui.base.BaseDialogFragment
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.showToast
import kotlinx.parcelize.Parcelize

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 8..
 * Description:
 */

@Parcelize
enum class CategoryDialogType: Parcelable {
    ADD,
    MODIFY,
    DELETE
}

class CategoryAddOrDeleteOrEditDialog: BaseDialogFragment<DialogAddOrDeleteOrEditCategoryBinding>(
    { DialogAddOrDeleteOrEditCategoryBinding.inflate(it) }
) {

    private val parentViewModel: CategoryListViewModel by viewModels({ requireParentFragment() })

    private var dialogType: CategoryDialogType? = null
    private var currentCategoryItem: CategoryUiModel? = null

    init {
        isCancelable = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dialogType = it.parcelable<CategoryDialogType>(ARG_ENUM_TYPE)
            currentCategoryItem = it.parcelable<CategoryUiModel>(ARG_CATEGORY_ITEM)
        }
    }

    override fun onInitView() {
        when (dialogType) {
            CategoryDialogType.ADD -> setupCategoryAddUI()
            CategoryDialogType.MODIFY -> setupCategoryModifyUI()
            CategoryDialogType.DELETE -> setupCategoryDeleteUI()
            null -> throw Exception("Category DialogType is Null")
        }
    }

    override fun onInitData() {}

    private fun isValidCategoryName(): Boolean {
        if (binding.etCategory.text.toString().isBlank()) {
            showToast(getString(R.string.title_input_category_for_add))
            return false
        } else {
            return true
        }
    }

    /**
     *  카테고리 추가 UI..
     */
    private fun setupCategoryAddUI() = with (binding) {
        tvTitle.text = getString(R.string.title_input_category_for_add)
        etCategory.isVisible = true
        btnPositive.text = getString(R.string.do_add)
        btnPositive.onThrottleClick {
            if (isValidCategoryName()) {
                parentViewModel.makeCategory(categoryName = etCategory.text.toString())
            }
        }
        btnNegative.text = getString(R.string.do_cancel)
        btnNegative.onThrottleClick {
            dismiss()
        }
    }

    /**
     *  카테고리 수정 UI..
     */
    private fun setupCategoryModifyUI() = with (binding) {
        tvTitle.text = getString(R.string.title_input_category_for_edit)

        etCategory.isVisible = true
        etCategory.setText(currentCategoryItem?.categoryName ?: "")

        tvNoti.isVisible = true
        tvNoti.text = getString(R.string.desc_when_edit_category_name_change_same_category_notes)

        btnPositive.text = getString(R.string.do_modify)
        btnPositive.onThrottleClick {
            if (isValidCategoryName()) {
                currentCategoryItem?.let {
                    parentViewModel.editCategory(it.copy(categoryName = etCategory.text.toString()))
                }
            }
        }
        btnNegative.text = getString(R.string.do_cancel)
        btnNegative.onThrottleClick {
            dismiss()
        }
    }

    /**
     *  카테고리 삭제 UI..
     */
    private fun setupCategoryDeleteUI() = with (binding) {
        tvTitle.text = getString(R.string.title_will_you_delete_category)

        tvNoti.isVisible = true
        tvNoti.text = getString(R.string.desc_when_delete_category_change_same_category_notes)

        btnPositive.text = getString(R.string.yes_i_will_delete)
        btnPositive.onThrottleClick {
            currentCategoryItem?.let {
                parentViewModel.deleteCategory(it.id)
            }
        }
        btnNegative.text = getString(R.string.do_cancel)
        btnNegative.onThrottleClick {
            dismiss()
        }
    }

    companion object {
        const val TAG = "CategoryAddOrDeleteOrEditDialog"

        private const val ARG_ENUM_TYPE = "TYPE"
        private const val ARG_CATEGORY_ITEM = "ARG_CATEGORY_ITEM"

        fun newInstance(
            dialogType: CategoryDialogType,
            categoryItem: CategoryUiModel? = null
        ): CategoryAddOrDeleteOrEditDialog = CategoryAddOrDeleteOrEditDialog().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_ENUM_TYPE, dialogType)
                putParcelable(ARG_CATEGORY_ITEM, categoryItem)
            }
        }
    }
}