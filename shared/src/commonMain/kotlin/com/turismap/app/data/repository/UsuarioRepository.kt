package com.turismap.app.data.repository

object UsuarioRepository {

    // Simula que hay un usuario conectado para que no te dé errores el diseño
    fun obtenerIdUsuarioActual(): String? {
        // Le ponemos el ID de "Pedro Pérez" para que puedas ver el perfil con datos
        // Tu compañero cambiará esto luego por el usuario real de Firebase
        return "ThLD0tD5t82abtNQrY9J"
    }

    // Simula la acción de cerrar sesión
    suspend fun cerrarSesion() {
        // Tu compañero conectará esto con Firebase Auth
        println("Simulando cierre de sesión para el equipo de Diseño...")
    }
}