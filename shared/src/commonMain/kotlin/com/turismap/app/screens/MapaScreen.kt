package com.turismap.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.CategoriaLugar
import com.turismap.app.Coordenada
import com.turismap.app.LugarTuristico
import com.turismap.app.LugaresRepository
import com.turismap.app.MapaTuristico
import com.turismap.app.components.FigmaPoiCard
import com.turismap.app.components.FigmaSearchBar
import com.turismap.app.components.FigmaTopHeader
import com.turismap.app.theme.*

@Composable
fun MapaScreen(onAbrirDetalle: () -> Unit) {
    var categoriaSeleccionada by remember { mutableStateOf(CategoriaLugar.TODOS) }
    var lugarSeleccionado by remember {
        mutableStateOf<LugarTuristico?>(
            LugarTuristico(
                id = "finca_albania",
                nombre = "Mirador & Finca Café Albania",
                categoria = CategoriaLugar.MIRADOR,
                descripcion = "Apaneca · CA-8 KM 87 · A 15 min de Juayúa",
                ubicacion = Coordenada(13.8682, -89.8021),
                calificacion = 4.9
            )
        )
    }

    val todosLosLugares = remember { LugaresRepository.obtenerLugaresAtaco() }
    val lugaresFiltrados = remember(categoriaSeleccionada) {
        if (categoriaSeleccionada == CategoriaLugar.TODOS) {
            todosLosLugares
        } else {
            todosLosLugares.filter { it.categoria == categoriaSeleccionada }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapaTuristico(
            lugares = lugaresFiltrados,
            onLugarClick = { lugar -> lugarSeleccionado = lugar },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            FigmaTopHeader()

            Spacer(modifier = Modifier.height(10.dp))

            FigmaSearchBar(modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CategoriaLugar.entries) { categoria ->
                    val isSelected = categoria == categoriaSeleccionada
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { categoriaSeleccionada = categoria },
                        color = if (isSelected) VerdePrimario else Color.White,
                        shape = RoundedCornerShape(50),
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, GrisBorde) else null,
                        shadowElevation = if (isSelected) 3.dp else 1.dp
                    ) {
                        Text(
                            text = categoria.etiqueta,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF2D3748),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }

        lugarSeleccionado?.let { lugar ->
            FigmaPoiCard(
                lugar = lugar,
                onVerMas = onAbrirDetalle,
                onClose = { lugarSeleccionado = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )
        }
    }
}