package com.arz.rates.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arz.rates.data.PreferencesRepository
import com.arz.rates.data.RateItem
import com.arz.rates.data.RatesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AppState(
    val rates: List<RateItem> = emptyList(),
    val selected: List<String> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val lastUpdated: Long? = null
)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val ratesRepo = RatesRepository()
    private val prefs = PreferencesRepository(app)

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.selected.collect { selected ->
                _state.value = _state.value.copy(selected = selected)
            }
        }
        refresh()
    }

    fun refresh() {
        if (_state.value.loading) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { ratesRepo.fetch() }
                .onSuccess { rates ->
                    _state.value = _state.value.copy(
                        rates = rates,
                        loading = false,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = e.message ?: "Unable to update rates."
                    )
                }
        }
    }

    fun toggle(key: String) {
        viewModelScope.launch {
            val current = _state.value.selected.toMutableList()
            if (key in current) current.remove(key) else current.add(key)
            prefs.saveSelected(current)
        }
    }

    fun remove(key: String) = toggle(key)

    fun reorder(from: Int, to: Int) {
        viewModelScope.launch {
            val list = _state.value.selected.toMutableList()
            if (from !in list.indices || to !in list.indices) return@launch
            val item = list.removeAt(from)
            list.add(to, item)
            prefs.saveSelected(list)
        }
    }

    fun setSelected(keys: List<String>) {
        viewModelScope.launch { prefs.saveSelected(keys.distinct()) }
    }
}
