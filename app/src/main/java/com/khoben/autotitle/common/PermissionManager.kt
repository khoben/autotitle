package com.khoben.autotitle.common

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.khoben.autotitle.extension.activityresult.permission.permissionsDSL
import java.lang.ref.WeakReference

typealias Permission = ActivityResultLauncher<Array<String>>

data class PInfo(
    var permission: Permission,
    var requestedPermissions: Array<String>,
    var onGranted: (() -> Unit)? = null
)

class PermissionManager(
    private val context: WeakReference<ComponentActivity>
) {

    private val permissions = HashMap<String, PInfo>()

    fun register(
        token: String,
        permission: Array<String>,
        onDenied: () -> Unit,
        onExplained: () -> Unit
    ) {
        context.get()?.let { context ->
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
}