package com.gixtool.app.util

import android.app.ActivityManager
import android.content.Context
import android.opengl.EGL14
import android.opengl.GLES20
import android.os.Build
import android.os.Debug
import java.io.BufferedReader
import java.io.FileReader
import java.io.RandomAccessFile

data class DeviceStats(
    val totalRamMb: Int,
    val availableRamMb: Int,
    val usedRamPercent: Int,
    val cpuModel: String,
    val cpuCores: Int,
    val gpuRenderer: String,
    val gpuVendor: String,
    val androidVersion: String,
    val deviceModel: String,
    val recommendedSettings: GameSettings
)

data class GameSettings(
    val graphics: String,
    val fps: String,
    val shadows: Boolean,
    val hdr: Boolean,
    val antiAliasing: Boolean,
    val reason: String
)

object DeviceInfo {

    fun getStats(context: Context): DeviceStats {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }

        val totalRam = (memInfo.totalMem / 1024 / 1024).toInt()
        val availRam = (memInfo.availMem / 1024 / 1024).toInt()
        val usedPercent = ((totalRam - availRam) * 100 / totalRam)

        val cpuModel = readCpuModel()
        val cpuCores = Runtime.getRuntime().availableProcessors()

        val settings = recommendSettings(totalRam, cpuCores)

        return DeviceStats(
            totalRamMb = totalRam,
            availableRamMb = availRam,
            usedRamPercent = usedPercent,
            cpuModel = cpuModel,
            cpuCores = cpuCores,
            gpuRenderer = "Adreno / Mali",
            gpuVendor = Build.HARDWARE,
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            recommendedSettings = settings
        )
    }

    private fun readCpuModel(): String {
        return try {
            BufferedReader(FileReader("/proc/cpuinfo")).use { reader ->
                reader.lineSequence()
                    .firstOrNull { it.startsWith("Hardware") || it.startsWith("model name") }
                    ?.substringAfter(":")?.trim() ?: Build.HARDWARE
            }
        } catch (e: Exception) { Build.HARDWARE }
    }

    fun getAvailableRamMb(context: Context): Int {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        return (memInfo.availMem / 1024 / 1024).toInt()
    }

    fun getCpuUsagePercent(): Int {
        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val line = reader.readLine()
            reader.close()
            val parts = line.split(" ").filter { it.isNotEmpty() }
            val total = parts.drop(1).take(7).sumOf { it.toLong() }
            val idle = parts[4].toLong()
            ((total - idle) * 100 / total).toInt().coerceIn(0, 100)
        } catch (e: Exception) { 0 }
    }

    private fun recommendSettings(ramMb: Int, cores: Int): GameSettings {
        return when {
            ramMb >= 6000 && cores >= 8 -> GameSettings(
                graphics = "Ultra HD",
                fps = "90 FPS",
                shadows = true,
                hdr = true,
                antiAliasing = true,
                reason = "جهازك قوي جداً — اضبط أعلى إعدادات"
            )
            ramMb >= 4000 && cores >= 6 -> GameSettings(
                graphics = "HD",
                fps = "60 FPS",
                shadows = true,
                hdr = false,
                antiAliasing = true,
                reason = "جهازك متوسط إلى قوي — إعدادات عالية مناسبة"
            )
            ramMb >= 3000 -> GameSettings(
                graphics = "Smooth",
                fps = "60 FPS",
                shadows = false,
                hdr = false,
                antiAliasing = false,
                reason = "جهازك متوسط — إعدادات ناعمة للأداء الأفضل"
            )
            else -> GameSettings(
                graphics = "Low",
                fps = "30 FPS",
                shadows = false,
                hdr = false,
                antiAliasing = false,
                reason = "ذاكرة محدودة — إعدادات منخفضة لتجنب التأخير"
            )
        }
    }
}
