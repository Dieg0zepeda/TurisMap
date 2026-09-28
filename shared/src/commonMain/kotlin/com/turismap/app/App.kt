package com.turismap.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.turismap.app.components.FigmaBottomBar
import com.turismap.app.screens.DetalleExperienciaScreen
import com.turismap.app.screens.MapaScreenFigma
import com.turismap.app.theme.GrisFondo
import com.turismap.app.screens.PerfilScreen
import com.turismap.app.screens.GuiaScreen
import com.turismap.app.screens.ExplorarScreen

@Composable
fun App() {
    var selectedIndex by remember { mutableStateOf(1) } // Tab Mapa activo
    var verDetalleExperiencia by remember { mutableStateOf(false) }

    MaterialTheme {
        Scaffold(
            bottomBar = {
                if (!verDetalleExperiencia) {
                    FigmaBottomBar(
                        selectedIndex = selectedIndex,
                        onTabSelected = { selectedIndex = it }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(GrisFondo)
            ) {
                AnimatedContent(
                    targetState = verDetalleExperiencia,
                    transitionSpec = {
                        if (targetState) {
                            (slideInVertically(animationSpec = tween(350)) { it / 2 } + fadeIn(animationSpec = tween(350)))
                                .togetherWith(fadeOut(animationSpec = tween(200)))
                        } else {
                            fadeIn(animationSpec = tween(200))
                                .togetherWith(slideOutVertically(animationSpec = tween(350)) { it / 2 } + fadeOut(animationSpec = tween(200)))
                        }
                    },
                    label = "DetalleTransition"
                ) { enDetalle ->
                    if (enDetalle) {
                        DetalleExperienciaScreen(
                            onVolver = { verDetalleExperiencia = false }
                        )
                    } else {
                        AnimatedContent(
                            targetState = selectedIndex,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(250)).togetherWith(fadeOut(animationSpec = tween(200)))
                            },
                            label = "TabsTransition"
                        ) { targetTab ->
                            when (targetTab) {
                                0 -> ExplorarScreen()
                                1 -> MapaScreenFigma(
                                    onAbrirDetalle = { verDetalleExperiencia = true }
                                )
                                2 -> GuiaScreen()
                                3 -> PerfilScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}