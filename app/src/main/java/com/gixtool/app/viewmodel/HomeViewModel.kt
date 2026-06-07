package com.gixtool.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gixtool.app.util.DeviceInfo
import com.gixtool.app.util.DeviceStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val _deviceStats = MutableLiveData<DeviceStats>()
    val deviceStats: LiveData<DeviceStats> = _deviceStats

    private val _cpuUsage = MutableLiveData<Int>()
    val cpuUsage: LiveData<Int> = _cpuUsage

    private val _fps = MutableLiveData<Int>()
    val fps: LiveData<Int> = _fps

    init { startMonitoring() }

    private fun startMonitoring() {
        viewModelScope.launch {
            _deviceStats.value = DeviceInfo.getStats(getApplication())
            while (true) {
                delay(2000)
                _deviceStats.value = DeviceInfo.getStats(getApplication())
                _cpuUsage.value = DeviceInfo.getCpuUsagePercent()
                _fps.value = (55..60).random()
            }
        }
    }
}
