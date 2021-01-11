package com.khoben.autotitle.extension.activityresult.permission

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher


inline fun ComponentActivity.permissionsDSL(
        builderPermission: MultiPermissionBuilder.() -> Unit
): ActivityResultLauncher<Array<String>> {
    val builder =
            MultiPermissionBuilder()
    builder.builderPermission()
    return requestMultiplePermissions(
            allGranted = builder.allGranted,
            denied = builder.denied,
            explained = builder.explained
    )
}