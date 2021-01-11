package com.khoben.autotitle.extension.activityresult.permission

class MultiPermissionBuilder {
    var allGranted: () -> Unit = {}
    var denied: (List<String>) -> Unit = {}
    var explained: (List<String>) -> Unit = {}
}