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
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.comercio_ubicacion_btn_guardar
import turismap.shared.generated.resources.comercio_ubicacion_cd_volver
import turismap.shared.generated.resources.comercio_ubicacion_error_rango
import turismap.shared.generated.resources.comercio_ubicacion_instrucciones
import turismap.shared.generated.resources.comercio_ubicacion_latitud
import turismap.shared.generated.resources.comercio_ubicacion_longitud
import turismap.shared.generated.resources.comercio_ubicacion_sin_punto
import turismap.shared.generated.resources.comercio_ubicacion_titulo

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
                title = { Text(stringResource(Res.string.comercio_ubicacion_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.comercio_ubicacion_cd_volver)
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
                stringResource(Res.string.comercio_ubicacion_instrucciones),
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
                    label = { Text(stringResource(Res.string.comercio_ubicacion_latitud)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = camposConTexto && coordenadaSeleccionada == null
                )
                OutlinedTextField(
                    value = longitudTexto,
                    onValueChange = { longitudTexto = it.replace(',', '.') },
                    label = { Text(stringResource(Res.string.comercio_ubicacion_longitud)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = camposConTexto && coordenadaSeleccionada == null
                )
            }
            if (camposConTexto && coordenadaSeleccionada == null) {
                Text(
                    stringResource(Res.string.comercio_ubicacion_error_rango),
                    color = MaterialTheme.colorScheme.error
                )
            } else if (!camposConTexto) {
                Text(
                    stringResource(Res.string.comercio_ubicacion_sin_punto),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = { coordenadaSeleccionada?.let(onGuardar) },
                enabled = coordenadaSeleccionada != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.comercio_ubicacion_btn_guardar))
            }
        }
    }
}
