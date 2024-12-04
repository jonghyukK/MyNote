package com.kjh.mynote.ui.features.place.make

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kjh.mynote.R
import com.kjh.mynote.databinding.DialogFragmentAddPurchaseNoteBinding
import com.kjh.mynote.ui.base.BaseDialogFragment
import com.kjh.mynote.ui.base.DialogType
import com.kjh.mynote.ui.features.category.list.CategoryListBSDialog
import com.kjh.mynote.ui.features.place.make.adapter.TempImageListAdapter
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.kjh.mynote.utils.extensions.showToast
import com.kjh.mynote.utils.extensions.toComma
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 4..
 * Description:
 */

@AndroidEntryPoint
class AddPurchaseNoteDialogFragment : BaseDialogFragment<DialogFragmentAddPurchaseNoteBinding>({
    DialogFragmentAddPurchaseNoteBinding.inflate(it)
}, DialogType.FULL_SCREEN) {

    private val parentViewModel: MakeOrModifyPlaceNoteViewModel by activityViewModels()
    private val viewModel: AddPurchaseNoteDialogViewModel by viewModels()

    private val tempImageListAdapter: TempImageListAdapter by lazy {
        TempImageListAdapter(
            deleteImageClickAction = deleteTempImageClickAction,
            tempImageClickAction = tempImageClickAction
        )
    }

    private var addedTempPurchaseNoteId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            addedTempPurchaseNoteId = it.getLong(ARG_LONG_TEMP_PURCHASE_NOTE_ID)
        }
    }

    override fun onInitView() {
        with(binding) {
            btnBottom.btnTitle = if (addedTempPurchaseNoteId > 0) {
                getString(R.string.do_modify)
            } else {
                getString(R.string.do_add)
            }

            rvTempImages.apply {
                adapter = tempImageListAdapter
            }

            etPurchaseName.addMyTextWatcher(purchaseNameTextWatcher)
            etPurchasePrice.addMyTextWatcher(priceTextWatcher)

            tvCategory.setTextClickListener(categoryClickListener)

            clAttachImages.setOnThrottleClickListener(photoAttachClickListener)
            btnBottom.setOnThrottleClickListener(addBtnClickListener)
        }
    }

    override fun onInitData() {
        if (addedTempPurchaseNoteId > 0) {
            val tempPurchaseNoteItem = parentViewModel.getTempPurchaseNoteItemById(addedTempPurchaseNoteId)
            viewModel.setInitItem(tempPurchaseNoteItem)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .map { it.purchaseName }
                        .distinctUntilChanged()
                        .collect { purchaseName ->
                            if (binding.etPurchaseName.text != purchaseName) {
                                binding.etPurchaseName.text = purchaseName
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.purchasePrice }
                        .distinctUntilChanged()
                        .collect { price ->
                            if (price > 0 && binding.etPurchasePrice.text != price.toString()) {
                                binding.etPurchasePrice.text = price.toString()
                            } else {
                                binding.etPurchasePrice.text = ""
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.categoryItem }
                        .distinctUntilChanged()
                        .collect { categoryItem ->
                            categoryItem?.let {
                                binding.tvCategory.text = it.categoryName
                                binding.tvCategory.textColor = R.color.black_800
                            } ?: run {
                                binding.tvCategory.text = getString(R.string.select_category)
                                binding.tvCategory.textColor = R.color.black_400
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .map { it.tempImageUrls }
                        .distinctUntilChanged()
                        .collect { tempImages ->
                            binding.tvImageCount.text = getString(
                                R.string.format_slash,
                                tempImages.size,
                                AppConstants.MAX_SELECTABLE_IMAGE_COUNT
                            )
                            tempImageListAdapter.submitList(tempImages)
                        }
                }

                launch {
                    viewModel.bottomBtnValidateFlow.collectLatest { isValid ->
                        binding.btnBottom.isEnable = isValid
                    }
                }
            }
        }
    }

    private fun clearFocus() {
        binding.etPurchaseName.hideKeyboard()
        binding.etPurchasePrice.hideKeyboard()
    }

    private val purchaseNameTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            viewModel.setPurchaseName(s.toString())
        }
    }

    private val priceTextWatcher = object : TextWatcher {
        private var current = ""

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (s.toString().isEmpty()) {
                current = ""
                viewModel.clearPurchasePrice()
                return
            }

            if (s.toString() != current) {
                binding.etPurchasePrice.removeMyTextWatcher()

                val cleanString = s.toString().replace(",", "")
                if (cleanString.isNotEmpty()) {
                    viewModel.setPurchasePrice(cleanString)

                    val formatted = cleanString.toLong().toComma()
                    current = formatted
                    binding.etPurchasePrice.text = formatted
                    binding.etPurchasePrice.setSelection(formatted.length)
                } else {
                    viewModel.clearPurchasePrice()
                }

                binding.etPurchasePrice.addMyTextWatcher(this)
            }
        }
    }

    private val multiPhotoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.clipData?.let { clipData ->
                val tempImages: MutableList<String> = mutableListOf()
                for (i in 0 until clipData.itemCount) {
                    val imageUri = clipData.getItemAt(i).uri

                    requireContext().contentResolver.takePersistableUriPermission(
                        imageUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    tempImages.add(imageUri.toString())
                }

                viewModel.setTempImages(tempImages)
            }
        }

    private val deleteTempImageClickAction: (String) -> Unit = { uri ->
        clearFocus()

        viewModel.deleteTempImageByUrl(uri)
    }

    private val categoryClickListener = View.OnClickListener {
        clearFocus()

        CategoryListBSDialog.newInstance(
            selectedCategoryItem = viewModel.uiState.value.categoryItem,
            selectCategoryAction = { categoryItem ->
                viewModel.setCategory(categoryItem)
            },
            updateCategoryNameAction = { categoryItem ->
                viewModel.updateSelectedCategoryWhenChanged(categoryItem)
            },
            deleteCategoryAction = { categoryId ->
                viewModel.deleteSelectedCategoryWhenChanged(categoryId)
            }
        ).show(childFragmentManager, CategoryListBSDialog.TAG)
    }

    private val tempImageClickAction: (String) -> Unit = {
        showToast("개발 예정..")
    }

    private val photoAttachClickListener = View.OnClickListener {
        clearFocus()

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        multiPhotoPickerLauncher.launch(intent)
    }

    private val addBtnClickListener = View.OnClickListener {
        if (binding.btnBottom.isEnable) {
            clearFocus()

            parentViewModel.addOrUpdatePurchaseNoteItem(viewModel.uiState.value)
            dismiss()
        }
    }

    companion object {
        const val TAG = "AddPurchaseNoteDialogFragment"
        private const val ARG_LONG_TEMP_PURCHASE_NOTE_ID = "ARG_LONG_TEMP_PURCHASE_NOTE_ID"

        fun newInstance(
            tempPurchaseNoteId: Long? = null
        ) = AddPurchaseNoteDialogFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_LONG_TEMP_PURCHASE_NOTE_ID, tempPurchaseNoteId ?: -1)
            }
        }
    }
}