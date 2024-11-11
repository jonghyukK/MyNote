package com.kjh.mynote.utils.extensions

import android.widget.Toast
import androidx.fragment.app.Fragment

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 9..
 * Description:
 */


fun Fragment.showToast(msg: String) {
    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}