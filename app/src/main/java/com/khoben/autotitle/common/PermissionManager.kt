package com.khoben.autotitle.common

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.khoben.autotitle.extension.activityresult.permission.permissionsDSL

typealias Permission = ActivityResultLauncher<Array<String>>

class PermissionManager(
    private val activity: ComponentActivity
) {

    data class PInfo(
        var permission: Permission,
        var requestedPermissions: Array<String>,
        var onGranted: (() -> Unit)? = null
    )

    private val permissions = HashMap<String, PInfo>()

    /**
     * Register set of permissions for [activity] with specified [token]
     *
     * @param token Permissions token
     * @param permission Array of permissions
     * @param onDenied OnDenied Callback
     * @param onExplained OnExplained Callback
     */
    fun register(
        token: String,
        permission: Array<String>,
        onDenied: () -> Unit,
        onExplained: () -> Unit
    ) {
        activity.let { context ->
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

    /**
     * Run permissions with [token] and single-shot [onGranted] callback
     *
     * @param token Permissions token
     * @param onGranted OnGranted Permissions Callback
     */
    @Synchronized
    fun runWithPermission(token: String, onGranted: () -> Unit) {
        permissions[token]?.let { (r, p) ->
            permissions[token]!!.onGranted = onGranted
            r.launch(p)
        }
    }

    /**
     * Releases all registered permissions
     */
    fun release() {
        permissions.forEach { (_, value) ->
            value.permission.unregister()
        }
        permissions.clear()
    }
}