package com.gixtool.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gixtool.app.util.CleanResult
import com.gixtool.app.util.CleanerEngine
import kotlinx.coroutines.launch

sealed class CleanerState {
    object Idle : CleanerState()
    object Cleaning : CleanerState()
    data class Done(val result: CleanResult) : CleanerState()
    data class Error(val msg: String) : CleanerState()
}

class CleanerViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableLiveData<CleanerState>(CleanerState.Idle)
    val state: LiveData<CleanerState> = _state

    fun cleanRam() = clean { CleanerEngine.cleanRam(getApplication()) }
    fun cleanCache() = clean { CleanerEngine.cleanCache(getApplication()) }
    fun cleanStorage() = clean { CleanerEngine.cleanStorage(getApplication()) }

    private fun clean(block: suspend () -> CleanResult) {
        viewModelScope.launch {
            _state.value = CleanerState.Cleaning
            try {
                _state.value = CleanerState.Done(block())
            } catch (e: Exception) {
                _state.value = CleanerState.Error(e.message ?: "خطأ")
            }
        }
    }
}
