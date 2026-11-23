package com.turismap.app.screens.guia

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.FondoBlancoAdaptable
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_buscar
import turismap.shared.generated.resources.categoria_lugar_todos
import turismap.shared.generated.resources.guia_buscar_placeholder
import turismap.shared.generated.resources.guia_filtro_todos_pueblos
import turismap.shared.generated.resources.guia_selector_pueblo_titulo
import turismap.shared.generated.resources.mapa_cd_limpiar_busqueda

@Composable
internal fun CabeceraBusquedaYPueblo(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    municipioSeleccionado: String?,
    onAbrirSelectorPueblo: () -> Unit,
    paleta: GuiaPaleta
) {
    val puebloActivo = municipioSeleccionado != null
    val etiquetaPueblo = municipioSeleccionado ?: stringResource(Res.string.categoria_lugar_todos)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Campo de búsqueda compacto
        Row(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(FondoBlancoAdaptable)
                .border(0.8.dp, paleta.airbnbBorder, RoundedCornerShape(22.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(Res.string.btn_buscar),
                tint = paleta.airbnbGray,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.guia_buscar_placeholder),
                        color = paleta.airbnbGray,
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = paleta.airbnbBlack,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(paleta.filtroActivoContenido),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (searchQuery.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(Res.string.mapa_cd_limpiar_busqueda),
                    tint = paleta.airbnbGray,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onSearchQueryChange("") }
                )
            }
        }

        // Chip selector compacto de pueblo
        Row(
            modifier = Modifier
                .height(44.dp)
                .widthIn(max = 146.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    if (puebloActivo) paleta.filtroActivoFondo.copy(alpha = 0.12f)
                    else FondoBlancoAdaptable
                )
                .border(
                    width = if (puebloActivo) 1.dp else 0.8.dp,
                    color = if (puebloActivo) paleta.filtroActivoContenido.copy(alpha = 0.5f) else paleta.airbnbBorder,
                    shape = RoundedCornerShape(22.dp)
                )
                .clickable(onClick = onAbrirSelectorPueblo)
                .padding(horizontal = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = if (puebloActivo) paleta.filtroActivoContenido else paleta.airbnbGray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = etiquetaPueblo,
                color = if (puebloActivo) paleta.filtroActivoContenido else paleta.airbnbBlack,
                fontSize = 13.sp,
                fontWeight = if (puebloActivo) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = stringResource(Res.string.guia_selector_pueblo_titulo),
                tint = if (puebloActivo) paleta.filtroActivoContenido else paleta.airbnbGray,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectorPuebloBottomSheet(
    municipiosDisponibles: List<String?>,
    municipioSeleccionado: String?,
    paleta: GuiaPaleta,
    onMunicipioSeleccionado: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FondoBlancoAdaptable,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(Res.string.guia_selector_pueblo_titulo),
                color = paleta.airbnbBlack,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            HorizontalDivider(
                color = paleta.airbnbBorder,
                thickness = 0.8.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            municipiosDisponibles.forEach { muni ->
                val activo = if (muni == null) {
                    municipioSeleccionado == null
                } else {
                    muni.equals(municipioSeleccionado, ignoreCase = true)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (activo) paleta.filtroActivoFondo.copy(alpha = 0.1f)
                            else Color.Transparent
                        )
                        .clickable { onMunicipioSeleccionado(muni) }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (muni == null) Icons.Rounded.Map else Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = if (activo) paleta.filtroActivoContenido else paleta.airbnbGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = muni ?: stringResource(Res.string.guia_filtro_todos_pueblos),
                        color = if (activo) paleta.filtroActivoContenido else paleta.airbnbBlack,
                        fontSize = 15.sp,
                        fontWeight = if (activo) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (activo) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = paleta.filtroActivoContenido,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
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
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categoriasFiltros, key = { it.id }) { cat ->
            val activa = cat.id.equals(categoriaSeleccionada, ignoreCase = true)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (activa) paleta.filtroActivoFondo else FondoBlancoAdaptable)
                    .then(
                        if (activa) Modifier
                        else Modifier.border(0.8.dp, paleta.airbnbBorder, RoundedCornerShape(18.dp))
                    )
                    .clickable { onCategoriaSeleccionada(cat.id) }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(cat.etiquetaRes),
                    color = if (activa) Color.White else paleta.airbnbBlack,
                    fontSize = 13.sp,
                    fontWeight = if (activa) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}
