package com.turismap.app.screens.guia

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.FondoBlancoAdaptable

@Composable
internal fun BarraBusquedaGuia(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    paleta: GuiaPaleta
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.12f)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(FondoBlancoAdaptable)
                .border(0.8.dp, paleta.airbnbBorder, RoundedCornerShape(28.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = paleta.verdeTurisMap,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        "Buscar en la Ruta de las Flores...",
                        color = paleta.airbnbGray,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun FilaFiltrosCategoria(
    categoriaSeleccionada: String,
    onCategoriaSeleccionada: (String) -> Unit,
    paleta: GuiaPaleta
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
    ) {
        items(categoriasFiltros) { cat ->
            val activa = cat.id.equals(categoriaSeleccionada, ignoreCase = true)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (activa) paleta.verdeTurisMap else FondoBlancoAdaptable)
                    .border(
                        width = if (activa) 1.5.dp else 1.dp,
                        color = if (activa) paleta.verdeTurisMap else paleta.airbnbBorder,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onCategoriaSeleccionada(cat.id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = cat.icono, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = cat.etiqueta,
                    color = if (activa) Color.White else paleta.airbnbBlack,
                    fontSize = 12.5.sp,
                    fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
internal fun FilaFiltrosMunicipio(
    municipioSeleccionado: String,
    onMunicipioSeleccionado: (String) -> Unit,
    paleta: GuiaPaleta
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
    ) {
        items(municipiosRuta) { muni ->
            val activo = muni.equals(municipioSeleccionado, ignoreCase = true)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (activo) paleta.verdeOscuroTurisMap.copy(alpha = 0.15f) else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (activo) paleta.verdeTurisMap else paleta.airbnbBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onMunicipioSeleccionado(muni) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📍", fontSize = 11.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (muni == "Todos") "Todos los Pueblos" else muni,
                    color = if (activo) paleta.verdeTurisMap else paleta.airbnbGray,
                    fontSize = 12.sp,
                    fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
