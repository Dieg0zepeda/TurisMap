package com.turismap.app.data.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.turismap.app.data.models.Lugar
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// 'actual' inyecta este código nativo de Android para cumplir el contrato de commonMain
actual object LugaresRepository {

    actual suspend fun obtenerTodos(): List<Lugar> = suspendCancellableCoroutine { continuation ->
        Firebase.firestore.collection("lugares")
            .get()
            .addOnSuccessListener { snapshot ->
                val lugares = snapshot.toObjects(Lugar::class.java)
                continuation.resume(lugares)
            }
            .addOnFailureListener {
                continuation.resume(emptyList())
            }
    }

    actual suspend fun obtenerPorId(id: String): Lugar? = suspendCancellableCoroutine { continuation ->
        Firebase.firestore.collection("lugares").document(id)
            .get()
            .addOnSuccessListener { document ->
                val lugar = document.toObject(Lugar::class.java)
                continuation.resume(lugar)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }
}