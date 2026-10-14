package com.turismap.app.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.turismap.app.data.models.Lugar
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual object LugaresRepository {

    actual suspend fun obtenerTodos(): List<Lugar> = suspendCancellableCoroutine { continuation ->
        Firebase.firestore.collection("lugares")
            .get()
            .addOnSuccessListener { snapshot ->
                if (continuation.isActive) {
                    try {
                        continuation.resume(
                            snapshot.documents.mapNotNull { document ->
                                document.toObject(Lugar::class.java)?.copy(id = document.id)
                            }
                        )
                    } catch (error: Exception) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(error)
                        }
                    }
                }
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
    }

    actual suspend fun obtenerPorId(id: String): Lugar? = suspendCancellableCoroutine { continuation ->
        Firebase.firestore.collection("lugares").document(id)
            .get()
            .addOnSuccessListener { document ->
                if (continuation.isActive) {
                    try {
                        continuation.resume(document.toObject(Lugar::class.java)?.copy(id = document.id))
                    } catch (error: Exception) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(error)
                        }
                    }
                }
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
    }
}
