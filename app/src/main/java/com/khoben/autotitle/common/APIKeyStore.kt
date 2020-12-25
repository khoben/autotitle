package com.khoben.autotitle.common

object APIKeyStore {
    init {
        System.loadLibrary("api-key")
    }
    external fun HuaweiMLKit(): String
}