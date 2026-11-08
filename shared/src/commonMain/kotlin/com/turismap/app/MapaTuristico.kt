package com.turismap.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.turismap.app.data.models.Lugar

@Composable
expect fun MapaTuristico(
    lugares: List<Lugar>,
    latInicial: Double? = null,
    lngInicial: Double? = null,
    zoomInicial: Float? = null,
    onLugarClick: (Lugar) -> Unit,
    modifier: Modifier = Modifier
)