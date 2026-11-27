package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.models.Lugar
import com.turismap.app.theme.FondoBlancoAdaptable
import com.turismap.app.theme.GrisBorde
import com.turismap.app.theme.GrisTextoSecundario
import com.turismap.app.theme.TextoPrincipalOscuro
import com.turismap.app.theme.VerdeTextoAdaptable
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.explorar_etiqueta_destino_predeterminada

private const val PROPORCION_ANCHO_TARJETA = 0.80f
private val ANCHO_MAXIMO_TARJETA = 340.dp
private val MARGEN_HORIZONTAL_CARRUSEL = 16.dp
private val ESPACIADO_TARJETAS = 12.dp
private val PADDING_SUPERIOR_CARRUSEL = 8.dp
private val PADDING_INFERIOR_CARRUSEL = 16.dp
private val ALTURA_TARJETA_RUTA = 260.dp
private val ALTURA_CARRUSEL_RUTA =
    ALTURA_TARJETA_RUTA + PADDING_SUPERIOR_CARRUSEL + PADDING_INFERIOR_CARRUSEL
private val ALTURA_IMAGEN_TARJETA = 140.dp
private val RADIO_TARJETA_RUTA = 20.dp
private val ELEVACION_TARJETA_RUTA = 4.dp

@Composable
internal fun CarruselExploraRuta(
    destinos: List<Lugar>,
    onLugarClick: (Lugar) -> Unit,
    modifier: Modifier = Modifier
) {
    if (destinos.isEmpty()) return

    val listState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(
        lazyListState = listState,
        snapPosition = SnapPosition.Start
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(ALTURA_CARRUSEL_RUTA)
    ) {
        val anchoDisponible = (maxWidth - MARGEN_HORIZONTAL_CARRUSEL * 2).coerceAtLeast(0.dp)
        val anchoTarjeta = (anchoDisponible * PROPORCION_ANCHO_TARJETA)
            .coerceAtMost(ANCHO_MAXIMO_TARJETA)

        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(ALTURA_CARRUSEL_RUTA),
            contentPadding = PaddingValues(
                start = MARGEN_HORIZONTAL_CARRUSEL,
                top = PADDING_SUPERIOR_CARRUSEL,
                end = MARGEN_HORIZONTAL_CARRUSEL,
                bottom = PADDING_INFERIOR_CARRUSEL
            ),
            horizontalArrangement = Arrangement.spacedBy(ESPACIADO_TARJETAS),
            flingBehavior = snapFlingBehavior
        ) {
            items(
                items = destinos,
                key = { lugar -> lugar.id }
            ) { lugar ->
                TarjetaDestinoCarrusel(
                    lugar = lugar,
                    onClick = { onLugarClick(lugar) },
                    modifier = Modifier
                        .width(anchoTarjeta)
                        .height(ALTURA_TARJETA_RUTA)
                )
            }
        }
    }
}

@Composable
private fun TarjetaDestinoCarrusel(
    lugar: Lugar,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esModoOscuro = isSystemInDarkTheme()
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(RADIO_TARJETA_RUTA),
        colors = CardDefaults.cardColors(containerColor = FondoBlancoAdaptable),
        elevation = CardDefaults.cardElevation(defaultElevation = if (esModoOscuro) 2.dp else ELEVACION_TARJETA_RUTA),
        border = BorderStroke(
            width = 1.dp,
            color = if (esModoOscuro) GrisBorde else GrisBorde.copy(alpha = 0.70f)
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ALTURA_IMAGEN_TARJETA)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val urlImagen = lugar.imagenPortadaUrl.ifBlank {
                    lugar.atractivos.firstOrNull { it.imagenUrl.isNotBlank() }?.imagenUrl.orEmpty()
                }
                if (urlImagen.isNotEmpty()) {
                    RemoteImage(
                        url = urlImagen,
                        contentDescription = lugar.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Surface(
                    modifier = Modifier.padding(10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = FondoBlancoAdaptable.copy(alpha = 0.92f),
                    border = BorderStroke(0.8.dp, GrisBorde.copy(alpha = 0.75f))
                ) {
                    val etiqueta = lugar.atractivos.firstOrNull()?.etiqueta
                        ?: stringResource(Res.string.explorar_etiqueta_destino_predeterminada)
                    Text(
                        text = etiqueta,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerdeTextoAdaptable,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    text = lugar.nombre,
                    fontSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrincipalOscuro,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = lugar.subtitulo,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = GrisTextoSecundario,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
