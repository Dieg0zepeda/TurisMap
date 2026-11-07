package com.turismap.app.components

import androidx.compose.runtime.Composable

// Implementación vacía para iOS de momento
@Composable
actual fun rememberSelectorImagen(onImagenSeleccionada: (String) -> Unit): () -> Unit {
    return { onImagenSeleccionada("") }
}