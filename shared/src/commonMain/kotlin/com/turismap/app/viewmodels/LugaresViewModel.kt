package com.turismap.app.viewmodels

import androidx.lifecycle.ViewModel
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.LugaresRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LugaresViewModel : ViewModel() {

    // Estado reactivo que la UI (Screens) observará
    private val _lugares = MutableStateFlow<List<Lugar>>(emptyList())
    val lugares: StateFlow<List<Lugar>> = _lugares.asStateFlow()

    init {
        cargarLugares()
    }

    private fun cargarLugares() {
        // Por ahora usa el Mock local.
        // En PT2026-47 esto se cambiará por una llamada a Firestore
        _lugares.value = LugaresRepository.obtenerTodos()
    }
}