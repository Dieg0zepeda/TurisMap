package com.turismap.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.PlanComercio
import kotlinx.coroutines.CancellationException

@Composable
fun PlanesComercioScreen(
    onBack: () -> Unit,
    onSolicitarPlan: (PlanComercio) -> Unit
) {
    var planes by remember { mutableStateOf<List<PlanComercio>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var errorCarga by remember { mutableStateOf<String?>(null) }
    var intentoCarga by remember { mutableStateOf(0) }

    LaunchedEffect(intentoCarga) {
        cargando = true
        errorCarga = null
        try {
            planes = ComercioRepository.obtenerPlanesDisponibles()
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (error: Exception) {
            val mensaje = error.message.orEmpty()
            errorCarga = if (
                mensaje.contains("PERMISSION_DENIED", ignoreCase = true) ||
                mensaje.contains("missing or insufficient permissions", ignoreCase = true)
            ) {
                "Firestore no permite leer los planes. Publica las reglas de planes_comercio y verifica que los documentos tengan activo: true."
            } else {
                mensaje.ifBlank { "No se pudieron cargar los planes." }
            }
        } finally {
            cargando = false
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(
                onClick = onBack,
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Text("Volver al perfil")
            }

            Text(
                text = "Planes para tu negocio",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Conoce las opciones disponibles y solicita información del plan que te interesa. Las condiciones y detalles se confirmarán contigo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when {
                cargando -> CircularProgressIndicator()
                errorCarga != null -> Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        errorCarga.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { intentoCarga++ }) {
                        Text("Reintentar")
                    }
                }
                planes.isEmpty() -> Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Aún no hay planes activos en planes_comercio. Agrega documentos con nombre, descripcion, beneficios, orden y activo: true.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = { intentoCarga++ }) {
                        Text("Volver a cargar")
                    }
                }
            }

            planes.forEach { plan ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = null,
                                    modifier = Modifier.padding(10.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                            ) {
                                Text(
                                    text = plan.nombre,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = plan.descripcion.ifBlank {
                                "El equipo de TurisMap confirmará los detalles de este plan."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        plan.beneficios.forEach { beneficio ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = beneficio,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Button(
                            onClick = { onSolicitarPlan(plan) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !cargando && errorCarga == null && plan.id.isNotBlank()
                        ) {
                            Text("Solicitar este plan")
                        }
                    }
                }
            }
        }
    }
}
