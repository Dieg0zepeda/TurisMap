package com.turismap.app.components

import androidx.compose.runtime.Composable

// Stub temporal en iOS: no invoca onImagenSeleccionada("") para no borrar la foto guardada
@Composable
actual fun rememberSelectorImagen(onImagenSeleccionada: (String) -> Unit): () -> Unit {
    return { /* Pendiente selector nativo PHPicker en iOS */ }
}
