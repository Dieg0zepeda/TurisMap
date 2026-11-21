package com.turismap.app.screens.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import com.turismap.app.theme.TerracotaTextoAdaptable
import com.turismap.app.theme.VerdeTextoAdaptable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModalFavoritosPerfil(
    totalFavoritos: Int,
    cantDestinos: Int,
    cantComercios: Int,
    categoriaSeleccionada: String,
    cargandoFavoritos: Boolean,
    favoritosFiltrados: List<ItemFavoritoUi>,
    paleta: PerfilPaleta,
    onDismiss: () -> Unit,
    onSeleccionarCategoria: (String) -> Unit,
    onItemClick: (ItemFavoritoUi) -> Unit,
    onQuitarFavoritoClick: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = paleta.fondoTarjeta,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = paleta.colorDivisor) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("❤️", fontSize = 22.sp)
                    Column {
                        Text(
                            "Mis Favoritos",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = paleta.colorTextoPrincipal
                        )
                        Text(
                            "$totalFavoritos guardados en total",
                            fontSize = 12.sp,
                            color = paleta.colorTextoSecundario
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = paleta.colorTextoSecundario)
                }
            }

            // Pestañas / Filtro de categorías: Todos, Destinos y Comercios
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Todos" to totalFavoritos,
                    "Destinos" to cantDestinos,
                    "Comercios" to cantComercios
                ).forEach { (tipo, count) ->
                    val activa = categoriaSeleccionada == tipo
                    FilterChip(
                        selected = activa,
                        onClick = { onSeleccionarCategoria(tipo) },
                        label = {
                            Text(
                                text = "$tipo ($count)",
                                fontSize = 12.sp,
                                fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = paleta.verdeEsmeralda,
                            selectedLabelColor = Color.White,
                            containerColor = paleta.fondoApp,
                            labelColor = paleta.colorTextoPrincipal
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = activa,
                            borderColor = if (activa) paleta.verdeEsmeralda else paleta.colorDivisor
                        )
                    )
                }
            }

            HorizontalDivider(color = paleta.colorDivisor)

            when {
                cargandoFavoritos -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = paleta.verdeEsmeralda)
                    }
                }
                favoritosFiltrados.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            if (categoriaSeleccionada == "Destinos") "📍"
                            else if (categoriaSeleccionada == "Comercios") "🏪"
                            else "🧭",
                            fontSize = 48.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = when (categoriaSeleccionada) {
                                "Destinos" -> "No tienes destinos guardados"
                                "Comercios" -> "No tienes comercios guardados"
                                else -> "Aún no tienes favoritos"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = paleta.colorTextoPrincipal
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Pulsa el corazón en ${if (categoriaSeleccionada == "Destinos") "los pueblos o destinos" else "los restaurantes y tiendas"} para agregarlos a esta lista.",
                            fontSize = 13.sp,
                            color = paleta.colorTextoSecundario,
                            lineHeight = 18.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(favoritosFiltrados, key = { it.id }) { item ->
                            TarjetaItemFavorito(
                                item = item,
                                paleta = paleta,
                                onClick = { onItemClick(item) },
                                onQuitarClick = { onQuitarFavoritoClick(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaItemFavorito(
    item: ItemFavoritoUi,
    paleta: PerfilPaleta,
    onClick: () -> Unit,
    onQuitarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = paleta.fondoApp),
        border = BorderStroke(1.dp, paleta.colorDivisor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (item.imagenUrl.isNotBlank()) {
                    RemoteImage(
                        url = item.imagenUrl,
                        contentDescription = item.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if (item.esComercio) "🏪" else "📍", fontSize = 24.sp)
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.categoria.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (item.esComercio) VerdeTextoAdaptable else TerracotaTextoAdaptable
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "★ ${item.calificacion}",
                        fontSize = 11.sp,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = item.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = paleta.colorTextoPrincipal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (item.esComercio) "Comercio verificado" else "Destino turístico",
                    fontSize = 11.5.sp,
                    color = paleta.colorTextoSecundario
                )
            }

            IconButton(onClick = onQuitarClick) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Quitar de favoritos",
                    tint = paleta.terracota,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
