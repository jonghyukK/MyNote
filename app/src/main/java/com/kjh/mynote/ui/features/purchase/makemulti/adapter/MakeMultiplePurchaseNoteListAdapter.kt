package com.kjh.mynote.ui.features.purchase.makemulti.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kjh.mynote.databinding.VhMakeMultiplePurchaseNoteItemBinding
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PaymentMethodUiModel
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.ui.features.purchase.makemulti.TempPurchaseNoteUiState
import com.kjh.mynote.utils.extensions.parcelable
import timber.log.Timber

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 3..
 * Description:
 */
class MakeMultiplePurchaseNoteListAdapter(
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
): ListAdapter<TempPurchaseNoteUiState, MakeMultiplePurchaseNoteItemViewHolder>(UI_MODEL_COMPARATOR) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MakeMultiplePurchaseNoteItemViewHolder {
        return MakeMultiplePurchaseNoteItemViewHolder(
            VhMakeMultiplePurchaseNoteItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            addNextItemAction,
            deleteItemAction,
            categoryClickAction,
            purchaseNameInputAction,
            recentPurchaseNameClickAction,
            purchasePriceInputAction,
            purchaseDateClickAction,
            paymentMethodClickAction,
            placeClickAction,
            attachImageClickAction,
            deleteImageClickAction
        )
    }

    override fun onBindViewHolder(holder: MakeMultiplePurchaseNoteItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun onBindViewHolder(
        holder: MakeMultiplePurchaseNoteItemViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val bundle = payloads[0] as Bundle
            if (bundle.containsKey(BUNDLE_KEY_CATEGORY)) {
                holder.updateCategoryItem(bundle.parcelable(BUNDLE_KEY_CATEGORY))
            }

            if (bundle.containsKey(BUNDLE_KEY_DATE)) {
                holder.updatePurchaseDate(bundle.getLong(BUNDLE_KEY_DATE))
            }

            if (bundle.containsKey(BUNDLE_KEY_PAYMENT_METHOD)) {
                holder.updatePaymentMethod(bundle.parcelable(BUNDLE_KEY_PAYMENT_METHOD))
            }

            if (bundle.containsKey(BUNDLE_KEY_PLACE)) {
                holder.updatePlace(bundle.parcelable(BUNDLE_KEY_PLACE))
            }

            if (bundle.containsKey(BUNDLE_KEY_IMAGES)) {
                holder.updateTempImages(bundle.getStringArrayList(BUNDLE_KEY_IMAGES) ?: emptyList())
            }

            if (bundle.containsKey(BUNDLE_KEY_IS_LAST)) {
                holder.updateIsLastItem(bundle.getBoolean(BUNDLE_KEY_IS_LAST))
            }

            if (bundle.containsKey(BUNDLE_KEY_RECENT_PURCHASE_NAMES)) {
                holder.updateRecentPurchaseNames(bundle.getStringArrayList(BUNDLE_KEY_RECENT_PURCHASE_NAMES) ?: emptyList())
            }

            if (bundle.containsKey(BUNDLE_KEY_RECENT_PURCHASE_NAME)) {
                holder.updateRecentPurchaseName(bundle.getString(BUNDLE_KEY_RECENT_PURCHASE_NAME) ?: "")
            }

            if (bundle.containsKey(BUNDLE_KEY_SHOW_DELETE_BTN)) {
                holder.updateDeleteBtnVisibility(bundle.getBoolean(BUNDLE_KEY_SHOW_DELETE_BTN))
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    companion object {
        private const val BUNDLE_KEY_CATEGORY = "categoryItem"
        private const val BUNDLE_KEY_DATE = "date"
        private const val BUNDLE_KEY_PAYMENT_METHOD = "paymentMethodItem"
        private const val BUNDLE_KEY_PLACE = "placeItem"
        private const val BUNDLE_KEY_IMAGES = "tempImages"
        private const val BUNDLE_KEY_IS_LAST = "isLastItem"
        private const val BUNDLE_KEY_RECENT_PURCHASE_NAMES = "recentPurchaseNames"
        private const val BUNDLE_KEY_RECENT_PURCHASE_NAME = "recentPurchaseName"
        private const val BUNDLE_KEY_SHOW_DELETE_BTN = "showDeleteBtn"

        private val UI_MODEL_COMPARATOR = object : DiffUtil.ItemCallback<TempPurchaseNoteUiState>() {
            override fun areItemsTheSame(
                oldItem: TempPurchaseNoteUiState,
                newItem: TempPurchaseNoteUiState
            ): Boolean = oldItem.tempId == newItem.tempId

            override fun areContentsTheSame(
                oldItem: TempPurchaseNoteUiState,
                newItem: TempPurchaseNoteUiState,
            ): Boolean {
                return if (newItem.isAutoPurchaseNameSetMode) {
                    // isAutoPurchaseNameSetMode가 true인 경우에는 purchaseName 변경도 반영하여 비교하고,
                    // purchasePrice는 기본적으로 비교 대상에서 제외.
                    oldItem.copy(purchasePrice = 0) == newItem.copy(purchasePrice = 0)
                } else {
                    // isAutoPurchaseNameSetMode가 false인 경우에는 purchaseName과 purchasePrice 모두 비교 대상에서 제외.
                    oldItem.copy(purchaseName = "", purchasePrice = 0) ==
                            newItem.copy(purchaseName = "", purchasePrice = 0)
                }
            }

            override fun getChangePayload(
                oldItem: TempPurchaseNoteUiState,
                newItem: TempPurchaseNoteUiState,
            ): Any? {
                val diffBundle = Bundle()

                if (oldItem.categoryItem != newItem.categoryItem) {
                    diffBundle.putParcelable(BUNDLE_KEY_CATEGORY, newItem.categoryItem)
                }

                if (oldItem.purchaseDate != newItem.purchaseDate) {
                    diffBundle.putLong(BUNDLE_KEY_DATE, newItem.purchaseDate)
                }

                if (oldItem.paymentMethodItem != newItem.paymentMethodItem) {
                    diffBundle.putParcelable(BUNDLE_KEY_PAYMENT_METHOD, newItem.paymentMethodItem)
                }

                if (oldItem.tempPlaceItem != newItem.tempPlaceItem) {
                    diffBundle.putParcelable(BUNDLE_KEY_PLACE, newItem.tempPlaceItem)
                }

                if (oldItem.tempImageUrls != newItem.tempImageUrls) {
                    diffBundle.putStringArrayList(BUNDLE_KEY_IMAGES, ArrayList(newItem.tempImageUrls))
                }

                if (oldItem.isLastItem != newItem.isLastItem) {
                    diffBundle.putBoolean(BUNDLE_KEY_IS_LAST, newItem.isLastItem)
                }

                if (oldItem.showDeleteButton != newItem.showDeleteButton) {
                    diffBundle.putBoolean(BUNDLE_KEY_SHOW_DELETE_BTN, newItem.showDeleteButton)
                }

                if (oldItem.recentPurchaseNameItems != newItem.recentPurchaseNameItems) {
                    diffBundle.putStringArrayList(BUNDLE_KEY_RECENT_PURCHASE_NAMES, ArrayList(newItem.recentPurchaseNameItems))
                }

                if (oldItem.purchaseName != newItem.purchaseName && newItem.isAutoPurchaseNameSetMode) {
                    diffBundle.putString(BUNDLE_KEY_RECENT_PURCHASE_NAME, newItem.purchaseName)
                }

                return if (diffBundle.size() == 0) null else diffBundle
            }
        }
    }
}