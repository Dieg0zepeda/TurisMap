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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turismap.app.data.repository.Comercio
import com.turismap.app.screens.guia.BarraBusquedaGuia
import com.turismap.app.screens.guia.EncabezadoFeedGuia
import com.turismap.app.screens.guia.EstadoErrorGuia
import com.turismap.app.screens.guia.EstadoVacioGuia
import com.turismap.app.screens.guia.FilaFiltrosCategoria
import com.turismap.app.screens.guia.FilaFiltrosMunicipio
import com.turismap.app.screens.guia.SeccionExperienciasDestacadas
import com.turismap.app.screens.guia.TarjetaComercioInstagram
import com.turismap.app.screens.guia.TarjetaEsqueletoFeed
import com.turismap.app.screens.guia.rememberGuiaPaleta
import com.turismap.app.screens.guia.rememberGuiaState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuiaScreen(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    comerciosExternos: List<Comercio>? = null,
    onLlamarComercio: (String) -> Unit = {},
    onComercioClick: (String) -> Unit = {},
    onIniciarRuta: (Comercio) -> Unit = {}
) {
    val paleta = rememberGuiaPaleta(isSystemInDarkTheme())
    val (state, favoritosUsuario) = rememberGuiaState(comerciosExternos)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        // 1. Buscador superior
        BarraBusquedaGuia(
            searchQuery = state.searchQuery,
            onSearchQueryChange = { state.searchQuery = it },
            paleta = paleta
        )

        // 2. Fila 1: Filtros de Categoría
        FilaFiltrosCategoria(
            categoriaSeleccionada = state.categoriaSeleccionada,
            onCategoriaSeleccionada = { state.categoriaSeleccionada = it },
            paleta = paleta
        )

        // 3. Fila 2: Filtros de Pueblos / Municipios
        FilaFiltrosMunicipio(
            municipioSeleccionado = state.municipioSeleccionado,
            onMunicipioSeleccionado = { state.municipioSeleccionado = it },
            paleta = paleta
        )

        HorizontalDivider(color = paleta.airbnbBorder, thickness = 0.8.dp)

        // 4. Feed de Comercios
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.cargando) {
                LazyColumn(
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(3) { TarjetaEsqueletoFeed() }
                }
            } else if (state.errorCarga != null) {
                EstadoErrorGuia(
                    errorCarga = state.errorCarga,
                    paleta = paleta,
                    onReintentar = state::reintentarCarga,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.comerciosFiltrados.isEmpty()) {
                EstadoVacioGuia(paleta = paleta)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Carrusel superior de Destacados
                    if (state.comerciosDestacados.isNotEmpty() && state.searchQuery.isBlank()) {
                        item {
                            SeccionExperienciasDestacadas(
                                comerciosDestacados = state.comerciosDestacados,
                                mapaMunicipios = state.mapaMunicipios,
                                paleta = paleta,
                                onComercioClick = onComercioClick
                            )
                        }

                        item {
                            HorizontalDivider(
                                color = paleta.airbnbBorder.copy(alpha = 0.5f),
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Encabezado del feed con total
                    item {
                        EncabezadoFeedGuia(
                            municipioSeleccionado = state.municipioSeleccionado,
                            totalLugares = state.comerciosFiltrados.size,
                            paleta = paleta
                        )
                    }

                    // Feed vertical continuo
                    items(state.comerciosFiltrados, key = { it.id }) { comercio ->
                        val muniNombre = state.mapaMunicipios[comercio.lugarId] ?: "Ruta de las Flores"
                        val esFavorito = favoritosUsuario.contains(comercio.id) ||
                            favoritosUsuario.contains("comercio:${comercio.id}")

                        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                            TarjetaComercioInstagram(
                                comercio = comercio,
                                municipio = muniNombre,
                                esFavorito = esFavorito,
                                paleta = paleta,
                                onToggleFavorito = { state.toggleFavorito(comercio.id, !esFavorito) },
                                onClick = { onComercioClick(comercio.id) },
                                onAccionContacto = {
                                    val tel = comercio.telefono.filter { it.isDigit() }
                                    if (tel.isNotBlank()) onLlamarComercio(comercio.telefono)
                                }
                            )
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = state.snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
            )
        }
    }
}
