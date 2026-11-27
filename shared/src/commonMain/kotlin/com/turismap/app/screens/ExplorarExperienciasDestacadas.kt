package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.repository.Comercio
import com.turismap.app.screens.guia.BadgePlanComercio
import com.turismap.app.screens.guia.GuiaPaleta
import com.turismap.app.screens.guia.PlaceholderImagenCategoria
import com.turismap.app.screens.guia.formatearSubtituloComercio
import com.turismap.app.screens.guia.rememberGuiaPaleta
import com.turismap.app.screens.guia.shimmerEffect
import com.turismap.app.theme.ObsidianaFondo
import kotlin.math.absoluteValue
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.auth_ruta_flores

private const val PROPORCION_ANCHO_TARJETA = 0.76f
private val ANCHO_MAXIMO_TARJETA = 330.dp
private val ALTO_TARJETA_DESTACADA = 152.dp
private val RADIO_TARJETA_DESTACADA = 16.dp
private val ESPACIADO_PAGINAS_DESTACADAS = 12.dp
private val PADDING_SUPERIOR_CARRUSEL = 6.dp
private val PADDING_INFERIOR_CARRUSEL = 20.dp
private val DESPLAZAMIENTO_VERTICAL_LATERAL = 12.dp
private val ALTO_CARRUSEL_DESTACADAS =
    ALTO_TARJETA_DESTACADA + PADDING_SUPERIOR_CARRUSEL + PADDING_INFERIOR_CARRUSEL

@Composable
internal fun CarruselExperienciasDestacadas(
    comerciosDestacados: List<Comercio>,
    resolverUbicacion: (Comercio) -> String,
    resolverCategoria: (String) -> String,
    onComercioClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (comerciosDestacados.isEmpty()) return

    val esOscuro = isSystemInDarkTheme()
    val paleta = rememberGuiaPaleta(esOscuro)
    val municipioPredeterminado = stringResource(Res.string.auth_ruta_flores)
    val pagerState = rememberPagerState(pageCount = { comerciosDestacados.size })

    LaunchedEffect(comerciosDestacados.size) {
        val ultimoIndice = (comerciosDestacados.size - 1).coerceAtLeast(0)
        if (pagerState.currentPage > ultimoIndice) {
            pagerState.scrollToPage(ultimoIndice)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(ALTO_CARRUSEL_DESTACADAS)
    ) {
        val anchoTarjeta = (maxWidth * PROPORCION_ANCHO_TARJETA)
            .coerceAtMost(ANCHO_MAXIMO_TARJETA)
        val paddingHorizontal = ((maxWidth - anchoTarjeta) / 2f)
            .coerceAtLeast(0.dp)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(ALTO_CARRUSEL_DESTACADAS),
            verticalAlignment = Alignment.Top,
            contentPadding = PaddingValues(
                start = paddingHorizontal,
                top = PADDING_SUPERIOR_CARRUSEL,
                end = paddingHorizontal,
                bottom = PADDING_INFERIOR_CARRUSEL
            ),
            pageSize = PageSize.Fixed(anchoTarjeta),
            pageSpacing = ESPACIADO_PAGINAS_DESTACADAS,
            beyondViewportPageCount = if (comerciosDestacados.size > 1) 1 else 0,
            key = { indice ->
                comerciosDestacados.getOrNull(indice)?.let { "dest_${it.id}" } ?: "dest_idx_$indice"
            }
        ) { pagina ->
            val destacado = comerciosDestacados.getOrNull(pagina) ?: return@HorizontalPager
            val pageOffset = (
                (pagerState.currentPage - pagina) + pagerState.currentPageOffsetFraction
            ).absoluteValue.coerceIn(0f, 1f)
            val factorFoco = 1f - pageOffset

            val muniNombre = resolverUbicacion(destacado).ifBlank { municipioPredeterminado }
            val categoriaNombre = resolverCategoria(destacado.categoria)

            TarjetaExperienciaDestacada(
                comercio = destacado,
                municipio = muniNombre,
                categoriaEtiqueta = categoriaNombre,
                paleta = paleta,
                factorFoco = factorFoco,
                onClick = { onComercioClick(destacado.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ALTO_TARJETA_DESTACADA)
                    .zIndex(factorFoco)
                    .graphicsLayer {
                        val escala = lerp(start = 0.90f, stop = 1f, fraction = factorFoco)
                        scaleX = escala
                        scaleY = escala
                        alpha = lerp(start = 0.65f, stop = 1f, fraction = factorFoco)
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
internal fun CarruselExperienciasDestacadasEsqueleto(
    modifier: Modifier = Modifier
) {
    val esOscuro = isSystemInDarkTheme()
    val forma = RoundedCornerShape(RADIO_TARJETA_DESTACADA)
    val pagerState = rememberPagerState(pageCount = { 2 })

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(ALTO_CARRUSEL_DESTACADAS)
    ) {
        val anchoTarjeta = (maxWidth * PROPORCION_ANCHO_TARJETA)
            .coerceAtMost(ANCHO_MAXIMO_TARJETA)
        val paddingHorizontal = ((maxWidth - anchoTarjeta) / 2f)
            .coerceAtLeast(0.dp)

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(ALTO_CARRUSEL_DESTACADAS),
            verticalAlignment = Alignment.Top,
            contentPadding = PaddingValues(
                start = paddingHorizontal,
                top = PADDING_SUPERIOR_CARRUSEL,
                end = paddingHorizontal,
                bottom = PADDING_INFERIOR_CARRUSEL
            ),
            pageSize = PageSize.Fixed(anchoTarjeta),
            pageSpacing = ESPACIADO_PAGINAS_DESTACADAS,
            beyondViewportPageCount = 1
        ) { pagina ->
            val factorFoco = if (pagina == 0) 1f else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ALTO_TARJETA_DESTACADA)
                    .zIndex(factorFoco)
                    .graphicsLayer {
                        val escala = lerp(start = 0.90f, stop = 1f, fraction = factorFoco)
                        scaleX = escala
                        scaleY = escala
                        alpha = lerp(start = 0.65f, stop = 1f, fraction = factorFoco)
                        translationY = lerp(
                            start = DESPLAZAMIENTO_VERTICAL_LATERAL.toPx(),
                            stop = 0f,
                            fraction = factorFoco
                        )
                    }
                    .clip(forma)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
                    .shimmerEffect(esOscuro)
            )
        }
    }
}

@Composable
private fun TarjetaExperienciaDestacada(
    comercio: Comercio,
    municipio: String,
    categoriaEtiqueta: String,
    paleta: GuiaPaleta,
    factorFoco: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val urlPortada = comercio.imagenUrls.firstOrNull {
        it.startsWith("http://") || it.startsWith("https://")
    }
    val subtitulo = formatearSubtituloComercio(municipio, categoriaEtiqueta)

    val bordeDestacada = when (comercio.planInt) {
        3 -> paleta.ambarAcento.copy(alpha = if (paleta.esOscuro) 0.38f else 0.32f)
        2 -> paleta.bordeEnfocado.copy(alpha = if (paleta.esOscuro) 0.40f else 0.28f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val elevacion = lerp(
        start = if (paleta.esOscuro) 1f else 2f,
        stop = if (paleta.esOscuro) 3f else 6f,
        fraction = factorFoco
    ).dp

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(RADIO_TARJETA_DESTACADA),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, bordeDestacada),
        elevation = CardDefaults.cardElevation(defaultElevation = elevacion),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (urlPortada != null) {
                RemoteImage(
                    url = urlPortada,
                    contentDescription = comercio.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                PlaceholderImagenCategoria(
                    categoria = comercio.categoria,
                    paleta = paleta,
                    modifier = Modifier.fillMaxSize(),
                    iconoSize = 30.dp,
                    desplazamientoInferior = 26.dp
                )
            }

            // Gradiente inferior oscuro para legibilidad consistente sobre imagen o placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to ObsidianaFondo.copy(alpha = 0.12f),
                                0.42f to Color.Transparent,
                                1.0f to ObsidianaFondo.copy(alpha = if (urlPortada != null) 0.86f else 0.74f)
                            )
                        )
                    )
            )

            BadgePlanComercio(
                planInt = comercio.planInt,
                paleta = paleta,
                compacto = true,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = comercio.nombre,
                    color = Color.White,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitulo.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitulo,
                        color = Color.White.copy(alpha = 0.88f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
