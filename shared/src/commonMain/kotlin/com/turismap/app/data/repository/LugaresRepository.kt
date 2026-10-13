package com.turismap.app.data.repository

import com.turismap.app.LugarTuristico
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.serialization.Serializable

// ==========================================
// 1. MODELOS DE DATOS
// ==========================================

// Modelo para los municipios de la Ruta de las Flores
@Serializable
data class MunicipioBD(
    val nombre: String = "",
    val elevacion: String = "",
    val clima: String = "",
    val calificacion: Double = 0.0,
    val historia: String = "",
    val mejorTemporadaDesc: String = "",
    val imagenes: List<String> = emptyList()
)

// Modelo para el Perfil de Usuario
@Serializable
data class UsuarioBD(
    val nombre: String = "",
    val correo: String = "",
    val favoritos: String = "",
    val intereses: String = "",
    val fotoPerfil: String = "",
    val fotoPortada: String = ""
)

// ==========================================
// 2. EL REPOSITORIO (Gestor de Base de Datos)
// ==========================================

object LugaresRepository {

    // Conexión a la base de datos Firestore
    private val firestore = Firebase.firestore

    // --- FUNCIONES PARA USUARIOS ---

    suspend fun obtenerUsuarioPorId(idUsuario: String): UsuarioBD? {
        return try {
            val documento = firestore.collection("usuarios").document(idUsuario).get()

            // MAPEO MANUAL: Solo extraemos los textos y evitamos chocar con la fecha_registro
            val nombreFirebase = documento.get<String>("nombre")
            val correoFirebase = documento.get<String>("correo")

            UsuarioBD(
                nombre = nombreFirebase,
                correo = correoFirebase,
                favoritos = "0", // Lo dejamos en 0 por ahora
                intereses = ""
            )
        } catch (e: Exception) {
            println("Error al obtener el usuario: ${e.message}")
            null
        }
    }

    // --- FUNCIONES PARA MUNICIPIOS ---

    // Función para ir a traer la lista de todos los municipios
    suspend fun obtenerMunicipios(): List<MunicipioBD> {
        return try {
            val respuesta = firestore.collection("lugares").get()
            respuesta.documents.map { documento ->
                documento.data<MunicipioBD>()
            }
        } catch (e: Exception) {
            println("Error al obtener lugares: ${e.message}")
            emptyList()
        }
    }

    // Función para traer la información de un solo municipio específico (ej. "ataco")
    suspend fun obtenerMunicipioPorId(idDocumento: String): MunicipioBD? {
        return try {
            val documento = firestore.collection("lugares").document(idDocumento).get()
            documento.data<MunicipioBD>()
        } catch (e: Exception) {
            println("Error al obtener el municipio específico: ${e.message}")
            null
        }
    }

    // --- FUNCIONES TEMPORALES ---

    // Función temporal para arreglar el mapa interactivo
    fun obtenerLugaresAtaco(): List<LugarTuristico> {
        return emptyList()
    }
}