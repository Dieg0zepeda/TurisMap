package com.turismap.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.turismap.app.components.MapSelector
import com.turismap.app.data.models.Coordenada

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionarUbicacionComercioScreen(
    coordenadaInicial: Coordenada,
    coordenadaGuardada: Coordenada?,
    onBack: () -> Unit,
    onGuardar: (Coordenada) -> Unit
) {
    var latitudTexto by remember(coordenadaGuardada) {
        mutableStateOf(coordenadaGuardada?.latitud?.toString().orEmpty())
    }
    var longitudTexto by remember(coordenadaGuardada) {
        mutableStateOf(coordenadaGuardada?.longitud?.toString().orEmpty())
    }
    val coordenadaSeleccionada = remember(latitudTexto, longitudTexto) {
        val latitud = latitudTexto.trim().replace(',', '.').toDoubleOrNull()
        val longitud = longitudTexto.trim().replace(',', '.').toDoubleOrNull()
        if (
            latitud != null && latitud.isFinite() && latitud in -90.0..90.0 &&
            longitud != null && longitud.isFinite() && longitud in -180.0..180.0
        ) {
            Coordenada(latitud = latitud, longitud = longitud)
        } else {
            null
        }
    }

    val camposConTexto = latitudTexto.isNotBlank() || longitudTexto.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ubicar mi negocio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al formulario"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Toca el mapa en la ubicación exacta de tu negocio. Luego guarda para regresar al formulario.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MapSelector(
                coordenadaInicial = coordenadaInicial,
                coordenadaSeleccionada = coordenadaSeleccionada,
                onCoordenadaSeleccionada = { coordenada ->
                    latitudTexto = coordenada.latitud.toString()
                    longitudTexto = coordenada.longitud.toString()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = latitudTexto,
                    onValueChange = { latitudTexto = it.replace(',', '.') },
                    label = { Text("Latitud") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = camposConTexto && coordenadaSeleccionada == null
                )
                OutlinedTextField(
                    value = longitudTexto,
                    onValueChange = { longitudTexto = it.replace(',', '.') },
                    label = { Text("Longitud") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = camposConTexto && coordenadaSeleccionada == null
                )
            }
            if (camposConTexto && coordenadaSeleccionada == null) {
                Text(
                    "Coordenadas inválidas: latitud debe estar entre -90 y 90, y longitud entre -180 y 180.",
                    color = MaterialTheme.colorScheme.error
                )
            } else if (!camposConTexto) {
                Text(
                    "Aún no has marcado un punto en el mapa.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { coordenadaSeleccionada?.let(onGuardar) },
                enabled = coordenadaSeleccionada != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar ubicación y volver")
            }
        }
    }
}