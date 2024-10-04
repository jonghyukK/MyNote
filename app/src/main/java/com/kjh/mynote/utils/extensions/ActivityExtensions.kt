package com.kjh.mynote.utils.extensions

import android.app.Activity
import android.widget.Toast

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 27..
 * Description:
 */


fun Activity.showToast(msg: String) =
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()