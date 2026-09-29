package com.turismap.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun MapaTuristico(
    lugares: List<LugarTuristico>,
    onLugarClick: (LugarTuristico) -> Unit,
    modifier: Modifier = Modifier
)