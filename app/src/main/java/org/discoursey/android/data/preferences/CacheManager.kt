package org.discoursey.android.data.preferences

import android.content.Context
import coil.Coil
import java.io.File

class CacheManager(private val context: Context) {

    fun getCacheSizeBytes(): Long {
        return getFolderSize(context.cacheDir) +
                (context.externalCacheDir?.let { getFolderSize(it) } ?: 0L)
    }

    fun getFormattedCacheSize(): String {
        val bytes = getCacheSizeBytes()
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb < 1.0) {
            val kb = bytes / 1024.0
            String.format("%.1f KB", kb)
        } else {
            String.format("%.1f MB", mb)
        }
    }

    fun clearAllCache(): Boolean {
        return try {
            // Clear Coil memory & disk
            Coil.imageLoader(context).memoryCache?.clear()
            Coil.imageLoader(context).diskCache?.clear()

            // Delete internal cache files
            deleteDir(context.cacheDir)
            context.externalCacheDir?.let { deleteDir(it) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getFolderSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var total = 0L
        val files = dir.listFiles() ?: return 0L
        for (f in files) {
            total += if (f.isDirectory) getFolderSize(f) else f.length()
        }
        return total
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list() ?: return true
            for (child in children) {
                val success = deleteDir(File(dir, child))
                if (!success) return false
            }
            return dir.delete()
        } else if (dir != null && dir.isFile) {
            return dir.delete()
        }
        return false
    }
}
