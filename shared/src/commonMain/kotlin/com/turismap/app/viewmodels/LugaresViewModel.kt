package com.turismap.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.LugaresRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LugaresViewModel : ViewModel() {

    private val _lugares = MutableStateFlow<List<Lugar>>(emptyList())
    val lugares: StateFlow<List<Lugar>> = _lugares.asStateFlow()
    private val _error = MutableStateFlow<Throwable?>(null)
    val error: StateFlow<Throwable?> = _error.asStateFlow()

    init {
        cargarLugares()
    }

    private fun cargarLugares() {
        viewModelScope.launch {
            try {
                _lugares.value = LugaresRepository.obtenerTodos()
                _error.value = null
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _error.value = error
            }
        }
    }
}