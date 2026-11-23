package com.turismap.app.data.repository

import com.turismap.app.data.models.Notificacion
import com.turismap.app.data.models.contarNotificacionesNoLeidas
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.notificaciones_error_marcar_leida
import turismap.shared.generated.resources.notificaciones_error_marcar_todas
import turismap.shared.generated.resources.notificaciones_error_sesion
import dev.gitlive.firebase.firestore.orderBy

object NotificacionesRepository {
    private val firestore by lazy { Firebase.firestore }
    private const val LIMITE_NOTIFICACIONES = 50

    fun observarNotificaciones(userId: String): Flow<List<Notificacion>> {
        val uidLimpio = userId.trim()
        if (uidLimpio.isEmpty()) return flowOf(emptyList())

        return firestore.collection("usuarios")
            .document(uidLimpio)
            .collection("notificaciones")
            .orderBy("creadaEn", Direction.DESCENDING)
            .limit(LIMITE_NOTIFICACIONES)
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { documento ->
                    try {
                        documento.data(Notificacion.serializer()).copy(id = documento.id)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        null
                    }
                }
            }
    }

    fun observarCantidadNoLeidas(userId: String): Flow<Int> =
        observarNotificaciones(userId)
            .map(::contarNotificacionesNoLeidas)
            .catch { emit(0) }

    suspend fun marcarComoLeida(notificacionId: String) {
        val idLimpio = notificacionId.trim()
        if (idLimpio.isEmpty()) return

        val uid = Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException(getString(Res.string.notificaciones_error_sesion))

        try {
            firestore.collection("usuarios")
                .document(uid)
                .collection("notificaciones")
                .document(idLimpio)
                .update("leida" to true)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw AutenticacionRepository.traducirErrorAuth(
                error = error,
                porDefectoRes = Res.string.notificaciones_error_marcar_leida
            )
        }
    }

    suspend fun marcarTodasComoLeidas(idsNoLeidas: List<String>) {
        val idsLimpios = idsNoLeidas
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
        if (idsLimpios.isEmpty()) return

        val uid = Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException(getString(Res.string.notificaciones_error_sesion))

        try {
            val coleccion = firestore.collection("usuarios")
                .document(uid)
                .collection("notificaciones")
            idsLimpios.forEach { id ->
                coleccion.document(id).update("leida" to true)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw AutenticacionRepository.traducirErrorAuth(
                error = error,
                porDefectoRes = Res.string.notificaciones_error_marcar_todas
            )
        }
    }
}
