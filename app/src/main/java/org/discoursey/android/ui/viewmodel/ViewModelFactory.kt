package org.discoursey.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.discoursey.android.data.preferences.AppPreferences
import org.discoursey.android.data.preferences.CacheManager
import org.discoursey.android.data.repository.DiscourseRepository

class ViewModelFactory(
    private val repository: DiscourseRepository,
    private val prefs: AppPreferences,
    private val cacheManager: CacheManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, prefs, cacheManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
