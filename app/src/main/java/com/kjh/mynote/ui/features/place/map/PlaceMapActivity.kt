package com.kjh.mynote.ui.features.place.map

import android.content.Intent
import android.net.Uri
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import com.kakao.sdk.navi.Constants
import com.kakao.sdk.navi.NaviClient
import com.kakao.sdk.navi.model.CoordType
import com.kakao.sdk.navi.model.Location
import com.kakao.sdk.navi.model.NaviOption
import com.kjh.data.model.KakaoPlaceModel
import com.kjh.data.model.PlaceNoteModel
import com.kjh.data.model.mapToKakaoPlaceModel
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityPlaceMapBinding
import com.kjh.mynote.ui.base.BaseNaverMapActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.constants.AppConstants.DEFAULT_ZOOM_LEVEL
import com.kjh.mynote.utils.extensions.parcelable
import com.kjh.mynote.utils.extensions.setOnThrottleClickListener
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 23..
 * Description:
 */

@AndroidEntryPoint
class PlaceMapActivity: BaseNaverMapActivity<ActivityPlaceMapBinding>({ ActivityPlaceMapBinding.inflate(it) }) {

    private var marker: Marker? = null
    private var placeNoteItem: PlaceNoteModel? = null

    override fun onInitView() {
        binding.btnFindRoad.setOnThrottleClickListener(findRoadClickListener)
    }

    override fun onInitUiData() {
        placeNoteItem = intent.parcelable<PlaceNoteModel>(AppConstants.INTENT_PLACE_NOTE_ITEM) ?: run {
            finish()
            return
        }

        placeNoteItem?.let {
            binding.tvAddress.text = it.placeAddress
            binding.btnFindRoad.isEnable = true

            moveToCamera(it.mapToKakaoPlaceModel())
        }
    }

    private fun moveToCamera(placeItem: KakaoPlaceModel) {
        val targetLatLng = LatLng(placeItem.y.toDouble(), placeItem.x.toDouble())
        val cameraUpdate = CameraUpdate.scrollAndZoomTo(targetLatLng, DEFAULT_ZOOM_LEVEL)
            .animate(CameraAnimation.None)

        marker = getMarker(placeItem, targetLatLng)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun getMarker(
        placeItem: KakaoPlaceModel,
        latLng: LatLng
    ): Marker = Marker().apply {
        position = latLng
        width = 80
        height = 100
        icon = MarkerIcons.BLACK
        iconTintColor = ContextCompat.getColor(this@PlaceMapActivity, R.color.purple)
        captionText = placeItem.placeName
        captionColor = ContextCompat.getColor(this@PlaceMapActivity, R.color.purple)
        map = naverMap
    }

    override fun onCameraChange(p0: Int, p1: Boolean) {}

    private fun goToKakaoNaviApp() {
        placeNoteItem?.let { place ->
            startActivity(
                NaviClient.instance.shareDestinationIntent(
                    Location(place.placeName, place.x, place.y),
                    NaviOption(coordType = CoordType.WGS84)
                )
            )
        }
    }

    private fun goToPlayStoreForKakaoNaviInstall() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(Constants.WEB_NAVI_INSTALL)
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private val findRoadClickListener = OnClickListener {
        if (NaviClient.instance.isKakaoNaviInstalled(this)) {
            goToKakaoNaviApp()
        } else {
            goToPlayStoreForKakaoNaviInstall()
        }
    }
}