package com.turismap.app.components

import androidx.compose.runtime.Composable

@Composable
expect fun rememberCompartirTexto(): (texto: String, asunto: String) -> Unit
