package com.turismap.app.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.turismap.app.data.models.Coordenada

@Composable
expect fun MapSelector(
    coordenadaInicial: Coordenada,
    coordenadaSeleccionada: Coordenada?,
    onCoordenadaSeleccionada: (Coordenada) -> Unit,
    modifier: Modifier = Modifier
)
