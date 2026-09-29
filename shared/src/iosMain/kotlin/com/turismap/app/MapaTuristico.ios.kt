package com.turismap.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun MapaTuristico(
    lugares: List<LugarTuristico>,
    onLugarClick: (LugarTuristico) -> Unit,
    modifier: Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Mapa iOS (MapKit pendiente)")
    }
}