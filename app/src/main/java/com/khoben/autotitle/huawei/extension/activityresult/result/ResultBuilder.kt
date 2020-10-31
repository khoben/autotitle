package com.khoben.autotitle.huawei.extension.activityresult.result

class ResultBuilder {
    var success: (result: Any) -> Unit = {}
    var error: (error: Throwable) -> Unit = {}
}