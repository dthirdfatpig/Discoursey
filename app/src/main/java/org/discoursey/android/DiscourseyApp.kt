package org.discoursey.android

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import org.discoursey.android.data.api.DiscourseClient
import org.discoursey.android.data.preferences.AppPreferences
import org.discoursey.android.data.preferences.CacheManager
import org.discoursey.android.data.repository.DiscourseRepository

class DiscourseyApp : Application(), ImageLoaderFactory {

    lateinit var appPreferences: AppPreferences
        private set
    lateinit var cacheManager: CacheManager
        private set
    lateinit var discourseClient: DiscourseClient
        private set
    lateinit var discourseRepository: DiscourseRepository
        private set

    override fun onCreate() {
        super.onCreate()
        appPreferences = AppPreferences(this)
        cacheManager = CacheManager(this)
        discourseClient = DiscourseClient(this)
        discourseRepository = DiscourseRepository(discourseClient, appPreferences)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
