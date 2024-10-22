package com.kjh.mynote.utils.extensions

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
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

fun ComponentActivity.setDarkStatusBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val controller = window.insetsController
        controller?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS // 현재 상태에서 설정할 플래그
        )
    } else
    // 상태 바의 텍스트 색상을 검정색으로 설정
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
}

fun ComponentActivity.setLightStatusBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val controller = window.insetsController
        controller?.setSystemBarsAppearance(
            0, // 변경하지 않음
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )
    } else
        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
}