package com.turismap.app.screens.mapa

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.repository.esCoordenadaValida
import com.turismap.app.theme.FondoBlancoAdaptable
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.mapa_atractivo_acceso_publico
import turismap.shared.generated.resources.mapa_atractivo_galeria_contador
import turismap.shared.generated.resources.mapa_atractivo_sin_descripcion
import turismap.shared.generated.resources.mapa_btn_iniciar_ruta
import turismap.shared.generated.resources.mapa_cd_cerrar_detalles

private val ALTURA_GALERIA_ATRACTIVO = 172.dp

@Composable
internal fun TarjetaAtractivoPublicoMapa(
    marcador: MarcadorMapa.AtractivoPublico,
    alturaMaximaTarjeta: Dp,
    paleta: MapaPaleta,
    onCerrar: () -> Unit,
    onIniciarRuta: (latitud: Double, longitud: Double, nombreDestino: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val atractivo = marcador.atractivo
    val categoria = marcador.categoria
    val colorCategoria = colorCategoriaAtractivo(categoria)
    val iconoCategoria = iconoCategoriaAtractivo(categoria)
    val fotos = remember(atractivo.id, atractivo.imagenes) {
        atractivo.imagenes.map(String::trim).filter(String::isNotBlank)
    }
    val coordenadaDestino = remember(marcador) {
        atractivo.coordenadas.takeIf(::esCoordenadaValida) ?: marcador.coordenadas
    }
    val ubicacionTexto = listOf(
        atractivo.municipio.trim(),
        stringResource(Res.string.mapa_atractivo_acceso_publico)
    ).filter(String::isNotBlank).distinct().joinToString(" · ")
    val descripcion = atractivo.descripcion.trim().ifBlank {
        stringResource(Res.string.mapa_atractivo_sin_descripcion)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FondoBlancoAdaptable),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.dp, paleta.airbnbBorde),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = alturaMaximaTarjeta)
                .verticalScroll(rememberScrollState())
        ) {
            GaleriaAtractivoPublico(
                atractivoId = atractivo.id,
                nombreAtractivo = atractivo.nombre,
                fotos = fotos,
                etiquetaCategoria = stringResource(categoria.etiquetaRes),
                colorCategoria = colorCategoria,
                iconoCategoria = iconoCategoria,
                esOscuro = paleta.esOscuro,
                onCerrar = onCerrar
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = colorCategoria
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = iconoCategoria,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(categoria.etiquetaRes),
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = atractivo.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = paleta.airbnbOscuro,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (ubicacionTexto.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = paleta.airbnbGris,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = ubicacionTexto,
                            fontSize = 13.sp,
                            color = paleta.airbnbGris,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = descripcion,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = paleta.airbnbGris
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = paleta.airbnbBorde, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onIniciarRuta(coordenadaDestino.latitud, coordenadaDestino.longitud, atractivo.nombre)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = paleta.turismapPrimario,
                        contentColor = paleta.sobrePrimario
                    ),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = paleta.sobrePrimario,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.mapa_btn_iniciar_ruta),
                        color = paleta.sobrePrimario,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GaleriaAtractivoPublico(
    atractivoId: String,
    nombreAtractivo: String,
    fotos: List<String>,
    etiquetaCategoria: String,
    colorCategoria: Color,
    iconoCategoria: ImageVector,
    esOscuro: Boolean,
    onCerrar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ALTURA_GALERIA_ATRACTIVO)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        when {
            fotos.size > 1 -> {
                key(atractivoId) {
                    val pagerState = rememberPagerState(pageCount = { fotos.size })
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pagina ->
                        RemoteImage(
                            url = fotos[pagina],
                            contentDescription = nombreAtractivo,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.55f)
                                )
                            )
                        )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                            .background(Color.Black.copy(alpha = 0.50f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(fotos.size) { indice ->
                            val esActiva = pagerState.currentPage == indice
                            Box(
                                modifier = Modifier
                                    .size(if (esActiva) 7.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(if (esActiva) Color.White else Color.White.copy(alpha = 0.45f))
                            )
                        }
                    }
                    Surface(
                        color = Color.Black.copy(alpha = 0.58f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp)
                    ) {
                        Text(
                            text = stringResource(
                                Res.string.mapa_atractivo_galeria_contador,
                                pagerState.currentPage + 1,
                                fotos.size
                            ),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            fotos.size == 1 -> {
                RemoteImage(
                    url = fotos.first(),
                    contentDescription = nombreAtractivo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                PlaceholderGaleriaAtractivo(
                    etiquetaCategoria = etiquetaCategoria,
                    colorCategoria = colorCategoria,
                    iconoCategoria = iconoCategoria,
                    esOscuro = esOscuro
                )
            }
        }

        IconButton(
            onClick = onCerrar,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(34.dp)
                .background(Color.Black.copy(alpha = 0.50f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.mapa_cd_cerrar_detalles),
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PlaceholderGaleriaAtractivo(
    etiquetaCategoria: String,
    colorCategoria: Color,
    iconoCategoria: ImageVector,
    esOscuro: Boolean
) {
    val gradiente = if (esOscuro) {
        listOf(colorCategoria.copy(alpha = 0.32f), colorCategoria.copy(alpha = 0.54f))
    } else {
        listOf(colorCategoria.copy(alpha = 0.14f), colorCategoria.copy(alpha = 0.30f))
    }
    val colorTextoSecundario = if (esOscuro) Color.White.copy(alpha = 0.88f) else colorCategoria

    Box(
        modifier = Modifier.fillMaxSize().background(Brush.linearGradient(colors = gradiente)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(64.dp).background(colorCategoria, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconoCategoria,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = etiquetaCategoria,
                color = colorTextoSecundario,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
