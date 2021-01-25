package com.khoben.autotitle.repository

import com.google.gson.reflect.TypeToken
import com.khoben.autotitle.App
import com.khoben.autotitle.R
import com.khoben.autotitle.common.FileUtils
import com.khoben.autotitle.model.LanguageItem

class LocalAssetLanguageRepository : LanguageRepository() {

    override fun load(): List<LanguageItem> {
        val assetFileName = App.appContext.getString(R.string.language_config_path)
        val jsonString = FileUtils.inputStreamToString(App.appContext.assets.open(assetFileName))
        return FileUtils.getObjectFromJson(jsonString, object : TypeToken<List<LanguageItem>>() {}.type)
    }

    companion object {
        @Volatile
        private var INSTANCE: LocalAssetLanguageRepository? = null

        fun getInstance(): LanguageRepository {
            synchronized("sync") {
                if (INSTANCE == null) INSTANCE = LocalAssetLanguageRepository()
                return INSTANCE!!
            }
        }
    }
}