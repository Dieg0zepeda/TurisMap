package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.models.Lugar
import com.turismap.app.theme.FondoBlancoAdaptable
import com.turismap.app.theme.GrisBorde
import com.turismap.app.theme.GrisTextoSecundario
import com.turismap.app.theme.TextoPrincipalOscuro
import com.turismap.app.theme.VerdePrimario
import com.turismap.app.theme.VerdeTextoAdaptable
import kotlin.math.absoluteValue
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.explorar_etiqueta_destino_predeterminada

private const val PROPORCION_ANCHO_TARJETA = 0.69f
private val ESPACIADO_PAGINAS = 12.dp
private val PADDING_SUPERIOR_CARRUSEL = 8.dp
private val PADDING_INFERIOR_CARRUSEL = 16.dp
private val ALTURA_TARJETA_RUTA = 260.dp
private val ALTURA_IMAGEN_TARJETA = 140.dp
private val RADIO_TARJETA_RUTA = 20.dp
private val DESPLAZAMIENTO_VERTICAL_LATERAL = 12.dp

@Composable
internal fun CarruselExploraRuta(
    destinos: List<Lugar>,
    onLugarClick: (Lugar) -> Unit,
    modifier: Modifier = Modifier
) {
    if (destinos.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { destinos.size })

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val anchoTarjeta = maxWidth * PROPORCION_ANCHO_TARJETA
        val paddingHorizontal = ((maxWidth - anchoTarjeta) / 2f).coerceAtLeast(0.dp)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = paddingHorizontal,
                top = PADDING_SUPERIOR_CARRUSEL,
                end = paddingHorizontal,
                bottom = PADDING_INFERIOR_CARRUSEL
            ),
            pageSize = PageSize.Fixed(anchoTarjeta),
            pageSpacing = ESPACIADO_PAGINAS,
            beyondViewportPageCount = 1,
            key = { indice -> destinos[indice].id }
        ) { pagina ->
            val lugar = destinos[pagina]
            val pageOffset = (
                (pagerState.currentPage - pagina) + pagerState.currentPageOffsetFraction
            ).absoluteValue.coerceIn(0f, 1f)
            val factorFoco = 1f - pageOffset

            TarjetaDestinoCarrusel(
                lugar = lugar,
                factorFoco = factorFoco,
                onClick = { onLugarClick(lugar) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ALTURA_TARJETA_RUTA)
                    .zIndex(factorFoco)
                    .graphicsLayer {
                        val escala = lerp(start = 0.92f, stop = 1f, fraction = factorFoco)
                        scaleX = escala
                        scaleY = escala
                        alpha = lerp(start = 0.78f, stop = 1f, fraction = factorFoco)
                        translationY = lerp(
                            start = DESPLAZAMIENTO_VERTICAL_LATERAL.toPx(),
                            stop = 0f,
                            fraction = factorFoco
                        )
                    }
            )
        }
    }
}

@Composable
private fun TarjetaDestinoCarrusel(
    lugar: Lugar,
    factorFoco: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val elevacion = lerp(start = 2f, stop = 8f, fraction = factorFoco).dp
    val anchoBorde = lerp(start = 1f, stop = 1.5f, fraction = factorFoco).dp
    val colorBorde = lerpColor(
        start = GrisBorde.copy(alpha = 0.45f),
        stop = VerdeTextoAdaptable.copy(alpha = 0.55f),
        fraction = factorFoco
    )

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(RADIO_TARJETA_RUTA),
        colors = CardDefaults.cardColors(containerColor = FondoBlancoAdaptable),
        elevation = CardDefaults.cardElevation(defaultElevation = elevacion),
        border = BorderStroke(width = anchoBorde, color = colorBorde)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ALTURA_IMAGEN_TARJETA)
                    .background(VerdePrimario.copy(alpha = 0.14f))
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
                    color = FondoBlancoAdaptable.copy(alpha = 0.92f)
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
