package com.turismap.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.turismap.app.components.FigmaBottomBar
import com.turismap.app.screens.ExplorarScreen
import com.turismap.app.screens.DetalleAtacoScreen
import com.turismap.app.screens.DetalleApanecaScreen
import com.turismap.app.screens.DetalleJuayuaScreen
import com.turismap.app.screens.DetalleSalcoatitanScreen
import com.turismap.app.screens.DetalleNahuizalcoScreen
import com.turismap.app.screens.GuiaScreen
import com.turismap.app.screens.MapaScreen
import com.turismap.app.screens.PerfilScreen

@Composable
fun App() {
    var selectedIndex by remember { mutableStateOf(0) }
    var puebloSeleccionado by remember { mutableStateOf<String?>(null) } // Solo guardamos el nombre

    MaterialTheme {
        Scaffold(
            bottomBar = {
                if (puebloSeleccionado == null) {
                    FigmaBottomBar(
                        selectedIndex = selectedIndex,
                        onTabSelected = { selectedIndex = it }
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {

                // Enrutador manual (Abre la pantalla quemada según el nombre)
                when (puebloSeleccionado) {
                    "Concepción de Ataco", "Ataco" -> DetalleAtacoScreen { puebloSeleccionado = null }
                    "Apaneca" -> DetalleApanecaScreen { puebloSeleccionado = null }
                    "Juayúa" -> DetalleJuayuaScreen { puebloSeleccionado = null }
                    "Salcoatitán" -> DetalleSalcoatitanScreen { puebloSeleccionado = null }
                    "Nahuizalco" -> DetalleNahuizalcoScreen { puebloSeleccionado = null }

                    null -> {
                        // Vista Principal
                        when (selectedIndex) {
                            0 -> ExplorarScreen(
                                onPuebloClick = { pueblo -> puebloSeleccionado = pueblo.nombre }
                            )

                            1 -> MapaScreen(onAbrirDetalle = {})
                            2 -> GuiaScreen()
                            3 -> PerfilScreen()
                        }
                    }
                }
            }
        }
    }
}