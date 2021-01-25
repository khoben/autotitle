package com.khoben.autotitle.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.khoben.autotitle.model.LanguageItem
import com.khoben.autotitle.repository.LanguageRepository

class LanguageSelectorViewModel(private val languageRepository: LanguageRepository) : ViewModel() {
    private val selectedItem = MutableLiveData<Int>()
    private val items by lazy {
        MutableLiveData<List<LanguageItem>>().also { loadLanguages(it) }
    }

    private fun loadLanguages(items: MutableLiveData<List<LanguageItem>>) {
        items.postValue(languageRepository.load())
    }

    fun getItems(): LiveData<List<LanguageItem>> {
        return items
    }

    fun getSelectedItem(): LiveData<Int> {
        return selectedItem
    }


    fun setSelected(idx: Int) {
        selectedItem.value = idx
    }
}