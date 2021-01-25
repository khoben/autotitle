package com.khoben.autotitle.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.khoben.autotitle.repository.LanguageRepository

class LanguageRepositoryViewModelFactory(
    private val repository: LanguageRepository
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        LanguageSelectorViewModel(repository) as T
}