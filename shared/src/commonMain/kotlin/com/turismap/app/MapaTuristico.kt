package com.turismap.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.turismap.app.data.models.Lugar // Importación del nuevo modelo

@Composable
expect fun MapaTuristico(
    lugares: List<Lugar>, // Cambiado de LugarTuristico a Lugar
    onLugarClick: (Lugar) -> Unit, // Cambiado de LugarTuristico a Lugar
    modifier: Modifier = Modifier
)