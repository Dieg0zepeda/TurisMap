package com.turismap.app.data.repository

import com.turismap.app.data.models.Notificacion
import com.turismap.app.data.models.NotificacionFirestoreDto
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.orderBy
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.getString
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.notificaciones_error_marcar_leida
import turismap.shared.generated.resources.notificaciones_error_marcar_todas
import turismap.shared.generated.resources.notificaciones_error_sesion

object NotificacionesRepository {
    private val firestore by lazy { Firebase.firestore }
    private val scopeObservacion by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
    private const val LIMITE_NOTIFICACIONES = 50
    private const val TAMANO_LOTE_BATCH = 450

    /**
     * Flujo centralizado y compartido del contador de notificaciones no leídas ligado al estado
     * de Firebase Auth. Filtra explícitamente leida == false para evitar escuchar todo el historial.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val cantidadNoLeidasFlow: StateFlow<Int> by lazy {
        Firebase.auth.authStateChanged
            .onStart { emit(Firebase.auth.currentUser) }
            .map { it?.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                if (uid.isNullOrBlank()) {
                    flowOf(0)
                } else {
                    observarCantidadNoLeidas(uid)
                }
            }
            .catch { emit(0) }
            .stateIn(
                scope = scopeObservacion,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
                initialValue = 0
            )
    }

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
                        documento.data(NotificacionFirestoreDto.serializer())
                            .aNotificacion(documento.id)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (_: Exception) {
                        null
                    }
                }
            }
    }

    fun observarCantidadNoLeidas(userId: String): Flow<Int> {
        val uidLimpio = userId.trim()
        if (uidLimpio.isEmpty()) return flowOf(0)

        return firestore.collection("usuarios")
            .document(uidLimpio)
            .collection("notificaciones")
            .where { "leida" equalTo false }
            .snapshots
            .map { snapshot -> snapshot.documents.size }
            .catch { emit(0) }
    }

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
            idsLimpios.chunked(TAMANO_LOTE_BATCH).forEach { bloque ->
                val batch = firestore.batch()
                bloque.forEach { id ->
                    batch.update(coleccion.document(id), "leida" to true)
                }
                batch.commit()
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
