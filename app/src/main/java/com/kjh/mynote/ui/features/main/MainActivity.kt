package com.kjh.mynote.ui.features.main

import androidx.fragment.app.commit
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import com.kjh.mynote.R
import com.kjh.mynote.databinding.ActivityMainBinding
import com.kjh.mynote.ui.base.BaseActivity
import com.kjh.mynote.ui.features.home.HomeFragment
import com.kjh.mynote.ui.features.place.home.PlaceNoteHomeFragment
import com.kjh.mynote.ui.features.purchase.home.PurchaseHomeFragment
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
            bnvMain.selectedItemId = R.id.nav_home
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
        HomeFragment.TAG -> HomeFragment.newInstance()
        PlaceNoteHomeFragment.TAG -> PlaceNoteHomeFragment.newInstance()
        PurchaseHomeFragment.TAG -> PurchaseHomeFragment.newInstance()
        else -> throw Exception("Wrong Fragment Tag")
    }

    private val bnvOnItemSelectedListener = OnItemSelectedListener { item ->
        val tag = when (item.itemId) {
            R.id.nav_home -> MainFragments.HOME_FRAGMENT.tag
            R.id.nav_place -> MainFragments.PLACE_NOTE_HOME_FRAGMENT.tag
            R.id.nav_purchase -> MainFragments.PURCHASE_NOTE_HOME_FRAGMENT.tag
            else -> throw Exception("Wrong MenuItem Id")
        }

        changeFragmentBy(tag)
        true
    }

    companion object {
        enum class MainFragments(val tag: String) {
            HOME_FRAGMENT(HomeFragment.TAG),
            PLACE_NOTE_HOME_FRAGMENT(PlaceNoteHomeFragment.TAG),
            PURCHASE_NOTE_HOME_FRAGMENT(PurchaseHomeFragment.TAG)
        }
    }
}