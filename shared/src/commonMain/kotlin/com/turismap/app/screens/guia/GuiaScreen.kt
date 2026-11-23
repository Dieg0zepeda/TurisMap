package com.turismap.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turismap.app.data.repository.Comercio
import com.turismap.app.screens.guia.CabeceraBusquedaYPueblo
import com.turismap.app.screens.guia.EncabezadoFeedGuia
import com.turismap.app.screens.guia.EstadoErrorGuia
import com.turismap.app.screens.guia.EstadoVacioGuia
import com.turismap.app.screens.guia.FilaFiltrosCategoria
import com.turismap.app.screens.guia.SeccionExperienciasDestacadas
import com.turismap.app.screens.guia.SelectorPuebloBottomSheet
import com.turismap.app.screens.guia.TarjetaComercioGuia
import com.turismap.app.screens.guia.TarjetaEsqueletoFeed
import com.turismap.app.screens.guia.rememberGuiaPaleta
import com.turismap.app.screens.guia.rememberGuiaState
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.auth_ruta_flores

@Composable
fun GuiaScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    comerciosExternos: List<Comercio>? = null,
    onComercioClick: (String) -> Unit = {}
) {
    val esOscuro = isSystemInDarkTheme()
    val paleta = rememberGuiaPaleta(esOscuro)
    val (state, favoritosUsuario) = rememberGuiaState(comerciosExternos)
    val listState = rememberLazyListState()
    val municipioPredeterminado = stringResource(Res.string.auth_ruta_flores)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        // 1. Única fila fija superior: búsqueda compacta + selector compacto de pueblo
        CabeceraBusquedaYPueblo(
            searchQuery = state.searchQuery,
            onSearchQueryChange = { state.searchQuery = it },
            municipioSeleccionado = state.municipioSeleccionado,
            onAbrirSelectorPueblo = { state.mostrarSelectorPueblo = true },
            paleta = paleta
        )

        HorizontalDivider(
            color = paleta.airbnbBorder.copy(alpha = 0.7f),
            thickness = 0.6.dp
        )

        // 2. Contenido desplazable: filtros de categoría + destacados + listado principal
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.cargando) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(4) { TarjetaEsqueletoFeed(esOscuro = esOscuro) }
                }
            } else if (state.errorCarga != null) {
                EstadoErrorGuia(
                    errorCarga = state.errorCarga,
                    paleta = paleta,
                    onReintentar = state::reintentarCarga,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Fila única de chips de categorías dentro del scroll
                    item(key = "filtros_categoria") {
                        FilaFiltrosCategoria(
                            categoriaSeleccionada = state.categoriaSeleccionada,
                            onCategoriaSeleccionada = { state.categoriaSeleccionada = it },
                            paleta = paleta
                        )
                    }

                    if (state.comerciosFiltrados.isEmpty()) {
                        item(key = "estado_vacio") {
                            EstadoVacioGuia(
                                paleta = paleta,
                                modifier = Modifier.fillParentMaxHeight(0.65f)
                            )
                        }
                    } else {
                        // Carrusel panorámico de destacados
                        if (state.comerciosDestacados.isNotEmpty() && state.searchQuery.isBlank()) {
                            item(key = "destacados") {
                                SeccionExperienciasDestacadas(
                                    comerciosDestacados = state.comerciosDestacados,
                                    resolverUbicacion = state::resolverUbicacionComercio,
                                    resolverCategoria = state::etiquetaCategoria,
                                    paleta = paleta,
                                    onComercioClick = onComercioClick
                                )
                            }
                        }

                        // Encabezado del listado con total
                        item(key = "encabezado_feed") {
                            EncabezadoFeedGuia(
                                municipioSeleccionado = state.municipioSeleccionado,
                                totalLugares = state.comerciosFiltrados.size,
                                paleta = paleta
                            )
                        }

                        // Listado principal de comercios
                        items(state.comerciosFiltrados, key = { it.id }) { comercio ->
                            val muniNombre = state.resolverUbicacionComercio(comercio)
                                .ifBlank { municipioPredeterminado }
                            val categoriaNombre = state.etiquetaCategoria(comercio.categoria)
                            val esFavorito = favoritosUsuario.contains(comercio.id) ||
                                favoritosUsuario.contains("comercio:${comercio.id}")

                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                TarjetaComercioGuia(
                                    comercio = comercio,
                                    municipio = muniNombre,
                                    categoriaEtiqueta = categoriaNombre,
                                    esFavorito = esFavorito,
                                    favoritoEnProceso = comercio.id in state.favoritosEnProceso,
                                    paleta = paleta,
                                    onToggleFavorito = { state.toggleFavorito(comercio.id, !esFavorito) },
                                    onClick = { onComercioClick(comercio.id) }
                                )
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = state.snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }

    if (state.mostrarSelectorPueblo) {
        SelectorPuebloBottomSheet(
            municipiosDisponibles = state.opcionesMunicipios,
            municipioSeleccionado = state.municipioSeleccionado,
            paleta = paleta,
            onMunicipioSeleccionado = { muni ->
                state.municipioSeleccionado = muni
                state.mostrarSelectorPueblo = false
            },
            onDismiss = { state.mostrarSelectorPueblo = false }
        )
    }
}
