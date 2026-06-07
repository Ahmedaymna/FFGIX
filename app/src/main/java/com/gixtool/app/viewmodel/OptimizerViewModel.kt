package com.gixtool.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gixtool.app.util.DeviceInfo
import com.gixtool.app.util.GameSettings
import kotlinx.coroutines.launch

sealed class OptimizerState {
    object Idle : OptimizerState()
    object Loading : OptimizerState()
    data class Done(val settings: GameSettings) : OptimizerState()
    data class Error(val message: String) : OptimizerState()
}

class OptimizerViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableLiveData<OptimizerState>(OptimizerState.Idle)
    val state: LiveData<OptimizerState> = _state

    fun analyze() {
        viewModelScope.launch {
            _state.value = OptimizerState.Loading
            val stats = DeviceInfo.getStats(getApplication())
            _state.value = OptimizerState.Done(stats.recommendedSettings)
        }
    }
}
