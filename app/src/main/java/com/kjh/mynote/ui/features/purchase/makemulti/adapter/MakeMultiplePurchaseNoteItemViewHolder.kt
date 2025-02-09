package com.kjh.mynote.ui.features.purchase.makemulti.adapter

import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.isVisible
import com.kjh.mynote.R
import com.kjh.mynote.databinding.VhMakeMultiplePurchaseNoteItemBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.ui.base.BaseViewHolder
import com.kjh.mynote.ui.features.place.make.adapter.TempImageListAdapter
import com.kjh.mynote.ui.features.purchase.makeedit.adapter.RecentRegisteredPurchaseNameListAdapter
import com.kjh.mynote.ui.features.purchase.makemulti.TempPurchaseNoteUiState
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.decorations.SpacingItemDecoration
import com.kjh.mynote.utils.extensions.onThrottleClick
import com.kjh.mynote.utils.extensions.toComma
import com.kjh.mynote.utils.extensions.toStringWithFormat

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 3..
 * Description:
 */
class MakeMultiplePurchaseNoteItemViewHolder(
    private val binding: VhMakeMultiplePurchaseNoteItemBinding,
    private val addNextItemAction: () -> Unit,
    private val deleteItemAction: (Int) -> Unit,
    private val categoryClickAction: (Int, CategoryUiModel?) -> Unit,
    private val purchaseNameInputAction: (Int, String) -> Unit,
    private val recentPurchaseNameClickAction: (Int, String) -> Unit,
    private val purchasePriceInputAction: (Int, Long) -> Unit,
    private val purchaseDateClickAction: (Int, Long?) -> Unit,
    private val paymentMethodClickAction: (Int, PaymentMethodUiModel?) -> Unit,
    private val placeClickAction: (Int, PlaceInfoUiModel?) -> Unit,
    private val attachImageClickAction: (Int) -> Unit,
    private val deleteImageClickAction: (Int, String) -> Unit
): BaseViewHolder<TempPurchaseNoteUiState>(binding.root) {

    private var purchaseNameTextWatcher: TextWatcher? = null
    private var priceTextWatcher: TextWatcher? = null

    private var tempImageListAdapter: TempImageListAdapter? = null
    private var recentPurchaseNameListAdapter: RecentRegisteredPurchaseNameListAdapter? = null

    init {
        binding.tvAdd.onThrottleClick {
            bindItem?.let { addNextItemAction() }
        }

        binding.ivDelete.onThrottleClick {
            bindItem?.let { item -> deleteItemAction(item.tempId) }
        }

        binding.tvCategory.onThrottleClick {
            bindItem?.let { item ->
                categoryClickAction(item.tempId, item.categoryItem)
            }
        }

        binding.tvPurchaseDate.onThrottleClick {
            bindItem?.let { item ->
                purchaseDateClickAction(item.tempId, item.purchaseDate)
            }
        }

        binding.tvPaymentMethod.onThrottleClick {
            bindItem?.let { item ->
                paymentMethodClickAction(item.tempId, item.paymentMethodItem)
            }
        }

        binding.tvPurchasePlace.onThrottleClick {
            bindItem?.let { item ->
                placeClickAction(item.tempId, item.tempPlaceItem)
            }
        }

        binding.clAttachImages.onThrottleClick {
            bindItem?.let { item -> attachImageClickAction(item.tempId) }
        }

        binding.rvTempImages.apply {
            itemAnimator = null
            tempImageListAdapter = TempImageListAdapter { selectedUrl ->
                bindItem?.let { item -> deleteImageClickAction(item.tempId, selectedUrl) }
            }
            adapter = tempImageListAdapter
        }

        binding.rvRecentPurchaseNames.apply {
            itemAnimator = null
            if (itemDecorationCount == 0) {
                addItemDecoration(SpacingItemDecoration(right = 6, exceptFirstItem = false))
            }
            recentPurchaseNameListAdapter = RecentRegisteredPurchaseNameListAdapter { selectedName ->
                bindItem?.let { item -> recentPurchaseNameClickAction(item.tempId, selectedName) }
            }
            adapter = recentPurchaseNameListAdapter
        }
    }

    override fun bind(item: TempPurchaseNoteUiState) {
        super.bind(item)

        // 타이틀
        bindTitle(item.tempIndex)
        // 삭제 버튼
        bindDeleteBtnVisibility(item.showDeleteButton)
        // 추가 버튼
        bindAddBtnVisibility(item.isLastItem)
        // 카테고리
        bindCategory(item.categoryItem)
        // 구매명
        bindPurchaseName(item.tempId, item.purchaseName)
        // 최근 구매명 목록
        bindRecentPurchaseNames(item.recentPurchaseNameItems)
        // 가격
        bindPrice(item.tempId, item.purchasePrice)
        // 날짜
        bindDate(item.purchaseDate)
        // 결제수단
        bindPaymentMethod(item.paymentMethodItem)
        // 장소
        bindPlace(item.tempPlaceItem)
        // 이미지
        bindTempImages(item.tempImageUrls)
    }

    private fun bindTitle(tempIndex: Int) = with(binding.tvTitle) {
        text = context.getString(R.string.format_purchase_note_with_number, tempIndex)
    }

    private fun bindDeleteBtnVisibility(isVisible: Boolean) = with (binding.ivDelete) {
        this.isVisible = isVisible
    }

    private fun bindAddBtnVisibility(isVisible: Boolean) = with (binding.tvAdd) {
        this.isVisible = isVisible
    }

    private fun bindCategory(categoryItem: CategoryUiModel?) = with(binding.tvCategory) {
        text = categoryItem?.categoryName ?: context.getString(R.string.select_category)
        textColor = if (categoryItem == null) hintColorRes else normalColorRes
    }

    private fun bindPurchaseName(tempId: Int, purchaseName: String) = with(binding.etPurchaseName) {
        removeMyTextWatcher()

        if (text != purchaseName) {
            text = purchaseName
            setSelection(purchaseName.length)
        }

        purchaseNameTextWatcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                purchaseNameInputAction(tempId, s.toString())
            }
        }

        addMyTextWatcher(purchaseNameTextWatcher!!)
    }

    private fun bindRecentPurchaseNames(recentPurchaseNameItems: List<String>) = with (binding) {
        rvRecentPurchaseNames.isVisible = recentPurchaseNameItems.isNotEmpty()
        recentPurchaseNameListAdapter?.submitList(recentPurchaseNameItems)
    }

    private fun bindPrice(tempId: Int, price: Long) = with (binding.etPurchasePrice) {
        removeMyTextWatcher()

        val priceComma = if (price == 0L) "" else price.toComma()
        if (text != priceComma) {
            text = priceComma
            setSelection(priceComma.length)
        }

        priceTextWatcher = object: TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    removeMyTextWatcher()

                    val parsedPrice = s.toString()
                        .replace(",", "")
                        .toLongOrNull() ?: 0
                    purchasePriceInputAction(tempId, parsedPrice)

                    val formatted = parsedPrice.toComma()
                    current = formatted
                    text = if (parsedPrice == 0L) "" else formatted
                    setSelection(formatted.length)

                    addMyTextWatcher(this)
                }
            }
        }

        addMyTextWatcher(priceTextWatcher!!)
    }

    private fun bindDate(purchaseDate: Long?) = with(binding.tvPurchaseDate) {
        text = purchaseDate?.toStringWithFormat(DATE_FORMAT) ?: context.getString(R.string.select_purchase_date)
        textColor = if (purchaseDate == null) hintColorRes else normalColorRes
    }

    private fun bindPaymentMethod(paymentMethodItem: PaymentMethodUiModel?) = with (binding.tvPaymentMethod) {
        text = paymentMethodItem?.paymentMethodName ?: context.getString(R.string.select_payment_method)
        textColor = if (paymentMethodItem == null) hintColorRes else normalColorRes
    }

    private fun bindPlace(tempPlaceItem: PlaceInfoUiModel?) = with (binding.tvPurchasePlace) {
        text = tempPlaceItem?.placeName ?: context.getString(R.string.search_purchase_place)
        textColor = if (tempPlaceItem == null) hintColorRes else normalColorRes
    }

    private fun bindTempImages(tempImages: List<String>) = with (binding) {
        tvImageCount.text = context.getString(
            R.string.format_slash,
            tempImages.size,
            AppConstants.MAX_SELECTABLE_IMAGE_COUNT
        )
        tempImageListAdapter?.submitList(tempImages)
    }

    fun updateCategoryItem(categoryItem: CategoryUiModel?) {
        updateBindItem { it.copy(categoryItem = categoryItem) }
        bindCategory(categoryItem)
    }

    fun updatePurchaseDate(date: Long) {
        updateBindItem { it.copy(purchaseDate = date) }
        bindDate(date)
    }

    fun updatePaymentMethod(paymentMethodItem: PaymentMethodUiModel?) {
        updateBindItem { it.copy(paymentMethodItem = paymentMethodItem)}
        bindPaymentMethod(paymentMethodItem)
    }

    fun updatePlace(placeItem: PlaceInfoUiModel?) {
        updateBindItem { it.copy(tempPlaceItem = placeItem) }
        bindPlace(placeItem)
    }

    fun updateTempImages(tempImages: List<String>) {
        updateBindItem { it.copy(tempImageUrls = tempImages)}
        bindTempImages(tempImages)
    }

    fun updateIsLastItem(isLast: Boolean) {
        updateBindItem { it.copy(isLastItem = isLast) }
        bindAddBtnVisibility(isLast)
    }

    fun updateRecentPurchaseNames(recentPurchaseNameItems: List<String>) {
        updateBindItem { it.copy(recentPurchaseNameItems = recentPurchaseNameItems) }
        bindRecentPurchaseNames(recentPurchaseNameItems)
    }

    fun updateRecentPurchaseName(recentPurchaseName: String) {
        updateBindItem { it.copy(purchaseName = recentPurchaseName) }
        bindPurchaseName(bindItem?.tempId ?: -1, recentPurchaseName)
    }

    fun updateDeleteBtnVisibility(isVisible: Boolean) {
        updateBindItem { it.copy(showDeleteButton = isVisible)}
        bindDeleteBtnVisibility(isVisible)
    }

    private fun updateBindItem(update: (TempPurchaseNoteUiState) -> TempPurchaseNoteUiState) {
        bindItem?.let {
            super.bind(update(it))
        }
    }

    private companion object {
        private const val DATE_FORMAT = "yyyy년 M월 d일 (E)"

        private val hintColorRes = R.color.black_400
        private val normalColorRes = R.color.black_800
    }
}