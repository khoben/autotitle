package com.khoben.autotitle.common

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.khoben.autotitle.extension.activityresult.permission.permissionsDSL

typealias Permission = ActivityResultLauncher<Array<String>>

data class PInfo(
    var permission: Permission,
    var requestedPermissions: Array<String>,
    var onGranted: (() -> Unit)? = null
)

class PermissionManager(
    private val context: ComponentActivity
) {

    private val permissions = HashMap<String, PInfo>()

    fun register(
        token: String,
        permission: Array<String>,
        onDenied: () -> Unit,
        onExplained: () -> Unit
    ) {
        context.let { context ->
            context.permissionsDSL {
                allGranted = {
                    permissions[token]?.onGranted?.invoke()
                    permissions[token]!!.onGranted = null
                }
                denied = {
                    onDenied.invoke()
                }
                explained = {
                    onExplained.invoke()
                }
            }.also {
                permissions[token] = PInfo(it, permission, null)
            }
        }
    }

    @Synchronized
    fun runWithPermission(token: String, onGranted: () -> Unit) {
        permissions[token]?.let { (r, p) ->
            permissions[token]!!.onGranted = onGranted
            r.launch(p)
        }
    }

    fun release() {
        permissions.forEach { (_, value) ->
            value.permission.unregister()
        }
        permissions.clear()
    }
}