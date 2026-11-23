package com.turismap.app.data.models

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.Serializable

enum class TipoNotificacion {
    SOLICITUD_APROBADA,
    SOLICITUD_RECHAZADA,
    OTRO
}

enum class AccionNotificacion {
    VER_COMERCIO,
    VER_PLANES,
    NINGUNA
}

@Serializable
data class Notificacion(
    val id: String = "",
    val tipo: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val solicitudId: String = "",
    val comercioId: String = "",
    val nombreNegocio: String = "",
    val leida: Boolean = false,
    val creadaEn: Timestamp? = null
) {
    val tipoResuelto: TipoNotificacion
        get() = when (tipo.trim().lowercase()) {
            TIPO_SOLICITUD_APROBADA -> TipoNotificacion.SOLICITUD_APROBADA
            TIPO_SOLICITUD_RECHAZADA -> TipoNotificacion.SOLICITUD_RECHAZADA
            else -> TipoNotificacion.OTRO
        }

    val esAprobada: Boolean
        get() = tipoResuelto == TipoNotificacion.SOLICITUD_APROBADA

    val esRechazada: Boolean
        get() = tipoResuelto == TipoNotificacion.SOLICITUD_RECHAZADA

    val accionPrincipal: AccionNotificacion
        get() = when (tipoResuelto) {
            TipoNotificacion.SOLICITUD_APROBADA ->
                if (comercioId.isNotBlank()) AccionNotificacion.VER_COMERCIO else AccionNotificacion.NINGUNA
            TipoNotificacion.SOLICITUD_RECHAZADA -> AccionNotificacion.VER_PLANES
            TipoNotificacion.OTRO -> AccionNotificacion.NINGUNA
        }

    companion object {
        const val TIPO_SOLICITUD_APROBADA = "solicitud_aprobada"
        const val TIPO_SOLICITUD_RECHAZADA = "solicitud_rechazada"
    }
}

fun contarNotificacionesNoLeidas(notificaciones: List<Notificacion>): Int =
    notificaciones.count { !it.leida }

fun formatearBadgeNotificaciones(cantidadNoLeidas: Int): String? = when {
    cantidadNoLeidas <= 0 -> null
    cantidadNoLeidas > 99 -> "99+"
    else -> cantidadNoLeidas.toString()
}

@Serializable
internal data class NotificacionFirestoreDto(
    val id: String? = null,
    val tipo: String? = null,
    val titulo: String? = null,
    val mensaje: String? = null,
    val solicitudId: String? = null,
    val comercioId: String? = null,
    val nombreNegocio: String? = null,
    val leida: Boolean? = null,
    val creadaEn: Timestamp? = null
) {
    fun aNotificacion(documentId: String): Notificacion = Notificacion(
        id = documentId,
        tipo = tipo.orEmpty(),
        titulo = titulo.orEmpty(),
        mensaje = mensaje.orEmpty(),
        solicitudId = solicitudId.orEmpty(),
        comercioId = comercioId.orEmpty(),
        nombreNegocio = nombreNegocio.orEmpty(),
        leida = leida ?: false,
        creadaEn = creadaEn
    )
}
