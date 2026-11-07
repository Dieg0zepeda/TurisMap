package com.turismap.app.components

import dev.gitlive.firebase.storage.File

// Función para convertir la ruta local en un archivo compatible con GitLive Storage
expect fun getFirebaseFile(uri: String): File?