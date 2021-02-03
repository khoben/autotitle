package com.khoben.autotitle.extension.activityresult.permission

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

const val DENIED = "DENIED"
const val EXPLAINED = "EXPLAINED"


inline fun ComponentActivity.requestMultiplePermissions(
    crossinline denied: (List<String>) -> Unit = {},
    crossinline explained: (List<String>) -> Unit = {},
    crossinline allGranted: () -> Unit = {}
): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: MutableMap<String, Boolean> ->
        val deniedList = result.filter { !it.value }.map { it.key }
        when {
            deniedList.isNotEmpty() -> {
                val map = deniedList.groupBy { permission ->
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            permission
                        )
                    ) DENIED else EXPLAINED
                }
                map[DENIED]?.let { denied.invoke(it) }
                map[EXPLAINED]?.let { explained.invoke(it) }
            }
            else -> allGranted.invoke()
        }
    }
}