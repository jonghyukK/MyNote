package com.kjh.mynote.utils.extensions

import android.app.Activity
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 27..
 * Description:
 */


fun Activity.showToast(msg: String) =
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun ComponentActivity.registerStartActivityResultLauncher(
    resultOkBlock: (ActivityResult) -> Unit,
    resultCancelBlock: ((ActivityResult) -> Unit)? = {},
) = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    when (result.resultCode) {
        Activity.RESULT_OK -> resultOkBlock.invoke(result)
        Activity.RESULT_CANCELED -> resultCancelBlock?.invoke(result)
    }
}