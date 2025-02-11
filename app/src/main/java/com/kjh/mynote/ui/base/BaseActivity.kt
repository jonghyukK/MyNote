package com.kjh.mynote.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<B: ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> B
): AppCompatActivity() {

    private var _binding: B? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = bindingFactory(layoutInflater)
        setContentView(binding.root)

        onInitView()
        onInitUiData()
    }

    open fun getViewModel(): BaseViewModel? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    abstract fun onInitView()
    abstract fun onInitUiData()
}