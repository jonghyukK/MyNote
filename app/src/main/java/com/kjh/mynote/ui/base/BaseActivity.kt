package com.kjh.mynote.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.kjh.mynote.utils.extensions.showToast
import kotlinx.coroutines.launch

abstract class BaseActivity<B: ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> B
): AppCompatActivity() {

    private var _binding: B? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        _binding = bindingFactory(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        onInitView()
        onInitUiData()

        setupGlobalErrorHandler()
    }

    open fun getViewModel(): BaseViewModel? {
        return null
    }

    private fun setupGlobalErrorHandler() {
        getViewModel()?.let { viewModel ->
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.errorEvent.collect { error ->
                        error?.let { showToast(it) }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    abstract fun onInitView()
    abstract fun onInitUiData()
}