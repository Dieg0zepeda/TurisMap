package com.turismap.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.LugaresRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LugaresViewModel : ViewModel() {

    private val _lugares = MutableStateFlow<List<Lugar>>(emptyList())
    val lugares: StateFlow<List<Lugar>> = _lugares.asStateFlow()

    init {
        cargarLugares()
    }

    private fun cargarLugares() {
        // Envolvemos la petición asíncrona (suspend) en el scope del ViewModel
        viewModelScope.launch {
            _lugares.value = LugaresRepository.obtenerTodos()
        }
    }
}