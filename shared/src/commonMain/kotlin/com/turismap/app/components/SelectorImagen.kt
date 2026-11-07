package com.turismap.app.components

import androidx.compose.runtime.Composable

// Le decimos a KMP que "espere" esta función en cada plataforma
@Composable
expect fun rememberSelectorImagen(onImagenSeleccionada: (String) -> Unit): () -> Unit