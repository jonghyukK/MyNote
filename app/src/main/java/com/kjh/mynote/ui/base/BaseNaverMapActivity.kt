package com.kjh.mynote.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.kjh.mynote.R
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMap.OnCameraChangeListener
import com.naver.maps.map.OnMapReadyCallback

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 8..
 * Description:
 */

abstract class BaseNaverMapActivity<B: ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> B
): AppCompatActivity(), OnMapReadyCallback, OnCameraChangeListener {

    private var _binding: B? = null
    val binding get() = _binding!!

    private lateinit var _naverMap: NaverMap
    val naverMap get() = _naverMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = bindingFactory(layoutInflater)
        setContentView(binding.root)

        onInitNaverMap()
    }

    private fun onInitNaverMap() {
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.fcv_map_container) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.fcv_map_container, it).commit()
            }

        mapFragment.getMapAsync(this@BaseNaverMapActivity)
    }

    override fun onMapReady(naverMap: NaverMap) {
        _naverMap = naverMap
        _naverMap.addOnCameraChangeListener(this)

        onInitView()
        onInitUiData()
    }

    override fun onDestroy() {
        super.onDestroy()
        _naverMap.removeOnCameraChangeListener(this)
        _binding = null
    }

    abstract fun onInitView()
    abstract fun onInitUiData()
}