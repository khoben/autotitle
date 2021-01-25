package com.khoben.autotitle.repository

import com.khoben.autotitle.model.LanguageItem

abstract class LanguageRepository protected constructor(){
    abstract fun load(): List<LanguageItem>
}
