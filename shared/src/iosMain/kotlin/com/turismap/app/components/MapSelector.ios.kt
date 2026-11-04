package com.turismap.app.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.turismap.app.data.models.Coordenada

@Composable
actual fun MapSelector(
    coordenadaInicial: Coordenada,
    coordenadaSeleccionada: Coordenada?,
    onCoordenadaSeleccionada: (Coordenada) -> Unit,
    modifier: Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "En iOS, ingresa la latitud y longitud en los campos debajo del mapa.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
