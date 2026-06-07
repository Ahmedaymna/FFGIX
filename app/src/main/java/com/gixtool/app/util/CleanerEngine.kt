package com.gixtool.app.util

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class CleanResult(val freedMb: Double, val filesDeleted: Int, val message: String)

object CleanerEngine {

    suspend fun cleanRam(context: Context): CleanResult = withContext(Dispatchers.IO) {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val beforeMb = DeviceInfo.getAvailableRamMb(context)
        try {
            am.killBackgroundProcesses(context.packageName)
            val runningApps = am.runningAppProcesses ?: emptyList()
            var killed = 0
            for (proc in runningApps) {
                if (proc.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    try { am.killBackgroundProcesses(proc.processName); killed++ } catch (e: Exception) {}
                }
            }
            val afterMb = DeviceInfo.getAvailableRamMb(context)
            val freed = (afterMb - beforeMb).coerceAtLeast(0)
            CleanResult(freed.toDouble(), killed, "تم تحرير $freed MB من الذاكرة وإيقاف $killed عملية")
        } catch (e: Exception) {
            CleanResult(0.0, 0, "تنظيف RAM: ${e.message ?: "تم التنظيف"}")
        }
    }

    suspend fun cleanCache(context: Context): CleanResult = withContext(Dispatchers.IO) {
        var totalFreed = 0L
        var filesDeleted = 0

        val dirs = listOf(
            context.cacheDir,
            context.externalCacheDir,
            context.codeCacheDir,
            File(context.filesDir, "temp"),
            File(context.filesDir, "cache")
        )

        for (dir in dirs) {
            if (dir != null && dir.exists()) {
                val result = deleteRecursive(dir)
                totalFreed += result.first
                filesDeleted += result.second
            }
        }

        val freedMb = totalFreed / 1024.0 / 1024.0
        CleanResult(freedMb, filesDeleted, "تم حذف $filesDeleted ملف مؤقت وتوفير ${String.format("%.1f", freedMb)} MB")
    }

    suspend fun cleanStorage(context: Context): CleanResult = withContext(Dispatchers.IO) {
        var totalFreed = 0L
        var filesDeleted = 0

        val tempDirs = mutableListOf<File>()

        // Internal temp
        tempDirs.add(File(context.filesDir, ".temp"))
        tempDirs.add(context.cacheDir)

        // External storage temp folders
        val extStorage = Environment.getExternalStorageDirectory()
        listOf("Android/data/${context.packageName}/cache",
               ".thumbnails", "DCIM/.thumbnails", "WhatsApp/Media/.Statuses").forEach { path ->
            tempDirs.add(File(extStorage, path))
        }

        for (dir in tempDirs) {
            if (dir.exists()) {
                val result = deleteRecursive(dir)
                totalFreed += result.first
                filesDeleted += result.second
            }
        }

        val freedMb = totalFreed / 1024.0 / 1024.0
        CleanResult(freedMb, filesDeleted, "تم تنظيف التخزين: حذف $filesDeleted ملف وتوفير ${String.format("%.1f", freedMb)} MB")
    }

    private fun deleteRecursive(file: File): Pair<Long, Int> {
        var size = 0L
        var count = 0
        if (file.isDirectory) {
            file.listFiles()?.forEach { child ->
                val r = deleteRecursive(child)
                size += r.first; count += r.second
            }
        } else {
            size = file.length()
            if (file.delete()) count = 1
        }
        return Pair(size, count)
    }
}
