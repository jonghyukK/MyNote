package com.kjh.mynote.ui.features.main

import androidx.fragment.app.commit
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityMainBinding
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.features.place.calendar.CalendarWithPlacesFragment
import com.kjh.mynote.ui.features.main.product.MyProductFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 11..
 * Description:
 */

@AndroidEntryPoint
class MainActivity: BaseActivity<ActivityMainBinding>({ ActivityMainBinding.inflate(it) }) {

    override fun onInitView() {
        with (binding) {
            bnvMain.setOnItemSelectedListener(bnvOnItemSelectedListener)
            bnvMain.selectedItemId = R.id.nav_place
        }
    }

    override fun onInitUiData() {}

    private fun changeFragmentBy(tag: String) {
        var targetFragment = supportFragmentManager.findFragmentByTag(tag)

        supportFragmentManager.commit {
            if (targetFragment == null) {
                targetFragment = getFragmentBy(tag)
                add(R.id.fcv_container, targetFragment!!, tag)
            }

            targetFragment?.let {
                show(it)
            }

            MainFragments.entries
                .filterNot { it.tag == tag }
                .forEach { type ->
                    supportFragmentManager.findFragmentByTag(type.tag)?.let { hide(it) }
                }
        }
    }

    private fun getFragmentBy(tag: String) = when (tag) {
        CalendarWithPlacesFragment.TAG -> CalendarWithPlacesFragment.newInstance()
        MyProductFragment.TAG -> MyProductFragment.newInstance()
        else -> throw Exception("Wrong Fragment Tag")
    }

    private val bnvOnItemSelectedListener = OnItemSelectedListener { item ->
        val tag = when (item.itemId) {
            R.id.nav_place -> MainFragments.CALENDAR_WITH_PLACES_FRAGMENT.tag
            R.id.nav_product -> MainFragments.MY_PRODUCT_FRAGMENT.tag
            else -> throw Exception("Wrong MenuItem Id")
        }

        changeFragmentBy(tag)
        true
    }

    companion object {
        enum class MainFragments(val tag: String) {
            CALENDAR_WITH_PLACES_FRAGMENT(CalendarWithPlacesFragment.TAG),
            MY_PRODUCT_FRAGMENT(MyProductFragment.TAG)
        }
    }
}