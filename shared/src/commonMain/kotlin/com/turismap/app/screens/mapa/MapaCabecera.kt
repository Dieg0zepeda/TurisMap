package com.turismap.app.screens.mapa

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.data.models.CategoriaAtractivoPublico
import com.turismap.app.theme.FondoBlancoAdaptable
import com.turismap.app.theme.SuperficieContenedorAdaptable
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_buscar
import turismap.shared.generated.resources.mapa_buscar_placeholder_completo
import turismap.shared.generated.resources.mapa_busqueda_sin_resultados
import turismap.shared.generated.resources.mapa_busqueda_sin_resultados_ayuda
import turismap.shared.generated.resources.mapa_cd_limpiar_busqueda
import turismap.shared.generated.resources.mapa_cd_opciones_capas
import turismap.shared.generated.resources.mapa_tipo_comercio
import turismap.shared.generated.resources.mapa_tipo_destino
import turismap.shared.generated.resources.mapa_tipo_destino_turistico

@Composable
internal fun CabeceraMapa(
    textoBusqueda: String,
    resultadosBusqueda: List<MarcadorMapa>,
    mostrarSugerencias: Boolean,
    filtroTipoMarcadores: FiltroMarcadoresMapa,
    categoriaAtractivoSeleccionada: CategoriaAtractivoPublico?,
    cantidadLugaresEnMapa: Int,
    paleta: MapaPaleta,
    onBusquedaChange: (String) -> Unit,
    onFocoBusqueda: () -> Unit,
    onSeleccionarResultado: (MarcadorMapa) -> Unit,
    onBuscarAccionTeclado: () -> Unit,
    onLimpiarBusqueda: () -> Unit,
    onAbrirMenuCapas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val mostrarListaSugerencias = mostrarSugerencias && textoBusqueda.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        // Barra de búsqueda superior a todo lo ancho en una sola línea
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = SuperficieContenedorAdaptable,
            shadowElevation = 5.dp,
            border = BorderStroke(1.dp, paleta.airbnbBorde),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(Res.string.btn_buscar),
                    tint = paleta.airbnbOscuro,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = textoBusqueda,
                        onValueChange = onBusquedaChange,
                        singleLine = true,
                        maxLines = 1,
                        textStyle = TextStyle(
                            color = paleta.airbnbOscuro,
                            fontSize = 14.5.sp
                        ),
                        cursorBrush = SolidColor(paleta.turismapPrimario),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                onBuscarAccionTeclado()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { estadoFoco ->
                                if (estadoFoco.isFocused) {
                                    onFocoBusqueda()
                                }
                            },
                        decorationBox = { innerTextField ->
                            if (textoBusqueda.isEmpty()) {
                                Text(
                                    text = stringResource(Res.string.mapa_buscar_placeholder_completo),
                                    color = paleta.airbnbGris,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                    )
                }
                if (textoBusqueda.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            onLimpiarBusqueda()
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(Res.string.mapa_cd_limpiar_busqueda),
                            tint = paleta.airbnbGris,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (mostrarListaSugerencias) {
            Spacer(modifier = Modifier.height(8.dp))
            ListaSugerenciasMapa(
                textoBusqueda = textoBusqueda,
                resultados = resultadosBusqueda,
                paleta = paleta,
                onSeleccionar = { marcador ->
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onSeleccionarResultado(marcador)
                }
            )
        } else {
            // Fila de controles bajo la barra: espacio izquierdo completamente libre
            // (sin fondo ni interceptores de toque) para la brújula nativa de Google Maps.
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 72.dp, end = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (filtroTipoMarcadores != FiltroMarcadoresMapa.TODOS) {
                    val categoriaActiva = categoriaAtractivoSeleccionada
                        ?.takeIf { filtroTipoMarcadores == FiltroMarcadoresMapa.ATRACTIVOS }
                    val etiquetaFiltroRes = categoriaActiva?.etiquetaPluralRes
                        ?: filtroTipoMarcadores.etiquetaRes
                    val colorChip = categoriaActiva?.let(::colorCategoriaAtractivo)
                        ?: paleta.turismapPrimario
                    val colorTextoChip = if (categoriaActiva != null) {
                        Color.White
                    } else {
                        paleta.sobrePrimario
                    }
                    Surface(
                        onClick = onAbrirMenuCapas,
                        shape = RoundedCornerShape(20.dp),
                        color = colorChip,
                        shadowElevation = 3.dp
                    ) {
                        Text(
                            text = "${stringResource(etiquetaFiltroRes)} ($cantidadLugaresEnMapa) ▾",
                            color = colorTextoChip,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Surface(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onAbrirMenuCapas()
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = SuperficieContenedorAdaptable,
                    shadowElevation = 5.dp,
                    border = BorderStroke(1.dp, paleta.airbnbBorde),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(Res.string.mapa_cd_opciones_capas),
                            tint = paleta.airbnbOscuro,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListaSugerenciasMapa(
    textoBusqueda: String,
    resultados: List<MarcadorMapa>,
    paleta: MapaPaleta,
    onSeleccionar: (MarcadorMapa) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = FondoBlancoAdaptable,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, paleta.airbnbBorde),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        if (resultados.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(Res.string.mapa_busqueda_sin_resultados, textoBusqueda.trim()),
                    color = paleta.airbnbOscuro,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(Res.string.mapa_busqueda_sin_resultados_ayuda),
                    color = paleta.airbnbGris,
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 264.dp)
            ) {
                itemsIndexed(
                    items = resultados,
                    key = { _, marcador -> marcador.idUnico }
                ) { indice, marcador ->
                    val etiquetaComercio = stringResource(Res.string.mapa_tipo_comercio)
                    val etiquetaDestinoTuristico = stringResource(Res.string.mapa_tipo_destino_turistico)

                    val icono = when (marcador) {
                        is MarcadorMapa.Comercio -> Icons.Default.Store
                        is MarcadorMapa.Destino -> Icons.Default.Place
                        is MarcadorMapa.AtractivoPublico -> iconoCategoriaAtractivo(marcador.categoria)
                    }
                    val colorAcento = when (marcador) {
                        is MarcadorMapa.Comercio -> paleta.turismapAcento
                        is MarcadorMapa.Destino -> paleta.turismapPrimario
                        is MarcadorMapa.AtractivoPublico -> colorCategoriaAtractivo(marcador.categoria)
                    }
                    val descripcionIcono = when (marcador) {
                        is MarcadorMapa.Comercio -> stringResource(Res.string.mapa_tipo_comercio)
                        is MarcadorMapa.Destino -> stringResource(Res.string.mapa_tipo_destino)
                        is MarcadorMapa.AtractivoPublico -> stringResource(marcador.categoria.etiquetaRes)
                    }
                    val detalleSecundario = when (marcador) {
                        is MarcadorMapa.Comercio -> listOf(
                            marcador.etiquetaCategoria.ifBlank { etiquetaComercio },
                            marcador.nombreMunicipio.ifBlank { marcador.comercio.direccion }
                        ).filter(String::isNotBlank).joinToString(" · ")

                        is MarcadorMapa.Destino -> listOf(
                            etiquetaDestinoTuristico,
                            marcador.lugar.ubicacionCorta
                        ).filter(String::isNotBlank).joinToString(" · ")

                        is MarcadorMapa.AtractivoPublico -> listOf(
                            stringResource(marcador.categoria.etiquetaRes),
                            marcador.atractivo.municipio
                        ).filter(String::isNotBlank).joinToString(" · ")
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSeleccionar(marcador) }
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    color = colorAcento.copy(alpha = 0.15f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icono,
                                contentDescription = descripcionIcono,
                                tint = colorAcento,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = marcador.titulo,
                                color = paleta.airbnbOscuro,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (detalleSecundario.isNotBlank()) {
                                Text(
                                    text = detalleSecundario,
                                    color = paleta.airbnbGris,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    if (indice < resultados.lastIndex) {
                        HorizontalDivider(
                            color = paleta.airbnbBorde,
                            thickness = 0.7.dp,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                    }
                }
            }
        }
    }
}
