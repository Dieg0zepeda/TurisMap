package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.BotonNotificaciones
import com.turismap.app.components.RemoteImage
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.EstadoPublicados
import com.turismap.app.data.repository.LugaresRepository
import com.turismap.app.screens.guia.rememberGuiaPaleta
import com.turismap.app.theme.*
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.app_name
import turismap.shared.generated.resources.btn_reintentar
import turismap.shared.generated.resources.explorar_badge_destino_destacado
import turismap.shared.generated.resources.explorar_cd_fondo_destino
import turismap.shared.generated.resources.explorar_descubre_lugar
import turismap.shared.generated.resources.explorar_error_cargar_destinos
import turismap.shared.generated.resources.explorar_experiencias_vacio
import turismap.shared.generated.resources.explorar_seccion_experiencias
import turismap.shared.generated.resources.explorar_seccion_pueblos_ruta
import turismap.shared.generated.resources.explorar_titulo

@Composable
fun ExplorarScreen(
    cantidadNotificacionesNoLeidas: Int = 0,
    onAbrirNotificaciones: () -> Unit = {},
    onLugarClick: (Lugar) -> Unit = {},
    onComercioClick: (String) -> Unit = {}
) {
    val lugaresIniciales = remember { LugaresRepository.lugaresEnCache }

    var lugares by remember { mutableStateOf(lugaresIniciales.orEmpty()) }
    var lugarDestacado by remember {
        mutableStateOf(
            lugaresIniciales?.let { lista ->
                lista.firstOrNull { it.id == "ataco" } ?: lista.firstOrNull()
            }
        )
    }
    var cargando by remember { mutableStateOf(lugaresIniciales == null) }
    var errorCarga by remember { mutableStateOf<String?>(null) }
    var intentoCarga by remember { mutableIntStateOf(0) }
    var mapaCategorias by remember {
        mutableStateOf(
            ComercioRepository.categoriasEnCache
                ?.associate { it.id.trim() to it.etiqueta.trim() }
                .orEmpty()
        )
    }
    var comerciosRespaldo by remember { mutableStateOf<List<Comercio>?>(null) }
    val listState = rememberLazyListState()

    val estadoPublicados by ComercioRepository.publicadosStateFlow.collectAsState()
    val comerciosPublicados = when (val estado = estadoPublicados) {
        is EstadoPublicados.Exito -> estado.comercios
        is EstadoPublicados.Cargando -> ComercioRepository.publicadosEnCache ?: comerciosRespaldo
        is EstadoPublicados.Error -> estado.comerciosEnCache ?: ComercioRepository.publicadosEnCache ?: comerciosRespaldo
    }
    val cargandoExperiencias = estadoPublicados is EstadoPublicados.Cargando && comerciosPublicados == null
    val experienciasDestacadas = remember(comerciosPublicados) {
        comerciosPublicados?.let(ComercioRepository::filtrarExperienciasDestacadas).orEmpty()
    }

    LaunchedEffect(intentoCarga) {
        val esReintentoManual = intentoCarga > 0
        if (esReintentoManual) {
            ComercioRepository.reintentarObservacionPublicados()
        } else {
            val cacheLugares = LugaresRepository.lugaresEnCache
            if (cacheLugares != null) {
                lugares = cacheLugares
                lugarDestacado = cacheLugares.firstOrNull { it.id == "ataco" } ?: cacheLugares.firstOrNull()
                errorCarga = null
                cargando = false
                return@LaunchedEffect
            }
        }

        val tieneDatosVisibles = lugares.isNotEmpty()
        if (!tieneDatosVisibles) {
            cargando = true
        }
        errorCarga = null
        try {
            val lugaresCargados = LugaresRepository.obtenerTodos(forzarRecarga = esReintentoManual)
            lugares = lugaresCargados
            lugarDestacado = lugaresCargados.firstOrNull { it.id == "ataco" } ?: lugaresCargados.firstOrNull()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            if (!tieneDatosVisibles) {
                errorCarga = error.message ?: getString(Res.string.explorar_error_cargar_destinos)
            }
        } finally {
            cargando = false
        }
    }

    // Respaldo puntual solo si el listener de publicados falla sin datos previos en caché
    LaunchedEffect(estadoPublicados) {
        if (estadoPublicados is EstadoPublicados.Error && comerciosPublicados == null) {
            try {
                comerciosRespaldo = ComercioRepository.obtenerPublicados(forzarRecarga = false)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // Mantiene estado vacío sin interrumpir la vista principal de destinos
            }
        }
    }

    // Catálogo auxiliar de categorías desde caché o en segundo plano sin bloquear el primer frame
    LaunchedEffect(Unit) {
        if (mapaCategorias.isEmpty()) {
            try {
                mapaCategorias = ComercioRepository.obtenerCategorias(forzarRecarga = false)
                    .associate { it.id.trim() to it.etiqueta.trim() }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // Usa el identificador de categoría como respaldo seguro
            }
        }
    }

    // Filtra la lista para no duplicar el destino que ya está en el banner superior
    val destinosSinDestacado = remember(lugares, lugarDestacado) {
        lugares.filter { it.id != lugarDestacado?.id }
    }
    val mapaMunicipios = remember(lugares) {
        lugares.associate { it.id.trim() to it.nombre.trim() }
    }
    val resolverUbicacionComercio: (Comercio) -> String = remember(mapaMunicipios) {
        { item ->
            mapaMunicipios[item.lugarId.trim()]
                ?: mapaMunicipios[item.lugarId]
                ?: item.municipio.trim().ifBlank { item.direccion.trim() }
        }
    }
    val resolverEtiquetaCategoria: (String) -> String = remember(mapaCategorias) {
        { categoriaId ->
            mapaCategorias[categoriaId.trim()]?.takeIf(String::isNotBlank) ?: categoriaId.trim()
        }
    }

    val esModoOscuro = isSystemInDarkTheme()
    val paletaGuia = rememberGuiaPaleta(esModoOscuro)

    if (cargando) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else if (errorCarga != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(errorCarga ?: stringResource(Res.string.explorar_error_cargar_destinos), color = TextoPrincipalOscuro)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { intentoCarga++ }) {
                Text(stringResource(Res.string.btn_reintentar))
            }
        }
    } else {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 28.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Cabecera con título y acceso a notificaciones
            item(key = "cabecera") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(Res.string.app_name).uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerdeTextoAdaptable,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = stringResource(Res.string.explorar_titulo),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextoPrincipalOscuro
                            )
                        }

                        BotonNotificaciones(
                            cantidadNoLeidas = cantidadNotificacionesNoLeidas,
                            onClick = onAbrirNotificaciones
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Banner superior: Destino Destacado
            lugarDestacado?.let { destacado ->
                item(key = "hero_destino") {
                    Card(
                        onClick = { onLugarClick(destacado) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(280.dp)
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        border = if (esModoOscuro) BorderStroke(1.dp, GrisBorde) else null,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val urlImagenDestacado = destacado.imagenPortadaUrl.ifBlank {
                                destacado.atractivos.firstOrNull { it.imagenUrl.isNotBlank() }?.imagenUrl.orEmpty()
                            }
                            if (urlImagenDestacado.isNotEmpty()) {
                                RemoteImage(
                                    url = urlImagenDestacado,
                                    contentDescription = stringResource(Res.string.explorar_cd_fondo_destino, destacado.nombre),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colorStops = arrayOf(
                                                0.0f to Color.Transparent,
                                                0.38f to ObsidianaFondo.copy(alpha = 0.14f),
                                                0.72f to ObsidianaFondo.copy(alpha = 0.66f),
                                                1.0f to ObsidianaFondo.copy(alpha = 0.90f)
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(20.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = TerracotaAcento
                                ) {
                                    Text(
                                        text = stringResource(Res.string.explorar_badge_destino_destacado),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp),
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val nombreCortoDestacado = destacado.nombre
                                    .trim()
                                    .split(" ")
                                    .lastOrNull { it.isNotBlank() }
                                    ?: destacado.nombre.trim()
                                Text(
                                    text = stringResource(Res.string.explorar_descubre_lugar, nombreCortoDestacado),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = destacado.nombre,
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.92f)
                                )
                            }
                        }
                    }
                }
            }

            item(key = "pueblos_ruta") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(Res.string.explorar_seccion_pueblos_ruta),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoPrincipalOscuro,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Carrusel horizontal alineado a la izquierda sin el elemento repetido
                    CarruselExploraRuta(
                        destinos = destinosSinDestacado,
                        onLugarClick = onLugarClick
                    )
                }
            }

            item(key = "experiencias_destacadas") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = paletaGuia.ambarAcento,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(Res.string.explorar_seccion_experiencias),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextoPrincipalOscuro
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (cargandoExperiencias) {
                        CarruselExperienciasDestacadasEsqueleto()
                    } else if (experienciasDestacadas.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.explorar_experiencias_vacio),
                            color = GrisTextoSecundario,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        CarruselExperienciasDestacadas(
                            comerciosDestacados = experienciasDestacadas,
                            resolverUbicacion = resolverUbicacionComercio,
                            resolverCategoria = resolverEtiquetaCategoria,
                            onComercioClick = onComercioClick
                        )
                    }
                }
            }
        }
    }
}
