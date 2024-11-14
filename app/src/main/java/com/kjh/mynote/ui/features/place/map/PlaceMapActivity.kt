package com.kjh.mynote.ui.features.place.map

import android.content.Intent
import android.net.Uri
import android.view.View.OnClickListener
import androidx.core.content.ContextCompat
import com.example.domain.model.PlaceInfo
import com.kakao.sdk.navi.Constants
import com.kakao.sdk.navi.NaviClient
import com.kakao.sdk.navi.model.CoordType
import com.kakao.sdk.navi.model.Location
import com.kakao.sdk.navi.model.NaviOption
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityPlaceMapBinding
import com.kjh.mynote.model.PlaceInfoUiModel
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.ui.base.BaseNaverMapActivity
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.constants.AppConstants.DEFAULT_ZOOM_LEVEL
import com.kjh.mynote.utils.extensions.ifNullOrEmpty
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
    private var placeInfoItem: PlaceInfoUiModel? = null

    override fun onInitView() {
        binding.btnFindRoad.setOnThrottleClickListener(findRoadClickListener)
    }

    override fun onInitUiData() {
        placeInfoItem = intent.parcelable<PlaceInfoUiModel>(AppConstants.INTENT_PLACE_INFO_ITEM) ?: run {
            finish()
            return
        }

        placeInfoItem?.let {
            binding.tvAddress.text = it.roadAddress.ifNullOrEmpty(it.address)
            binding.btnFindRoad.isEnable = true

            moveToCamera(it)
        }
    }

    private fun moveToCamera(placeItem: PlaceInfoUiModel) {
        val targetLatLng = LatLng(placeItem.y.toDouble(), placeItem.x.toDouble())
        val cameraUpdate = CameraUpdate.scrollAndZoomTo(targetLatLng, DEFAULT_ZOOM_LEVEL)
            .animate(CameraAnimation.None)

        marker = getMarker(placeItem, targetLatLng)
        naverMap.moveCamera(cameraUpdate)
    }

    private fun getMarker(
        placeItem: PlaceInfoUiModel,
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
        placeInfoItem?.let { place ->
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