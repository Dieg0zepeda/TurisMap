package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.PlanComercio
import kotlinx.coroutines.CancellationException

// Paleta Elegante y Sutil
private val GoldPremium = Color(0xFFD4AF37) // VIP
private val GreenSocio = Color(0xFF1B5E20)  // Socio
private val SlateBasic = Color(0xFF64748B)  // BÁSICO: Gris azulado elegante
private val SoftGrayBorder = Color(0xFFE0E0E0)

@OptIn(ExperimentalMaterial3Api::class)
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
                "Error de permisos en Firestore. Verifica las reglas."
            } else {
                mensaje.ifBlank { "No se pudieron cargar los planes." }
            }
        } finally {
            cargando = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Planes de negocio", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            // Distribución dinámica del espacio vertical
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Cabecera Elegante
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Impulsa tu negocio",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Elige la membresía que mejor se adapte a ti para atraer turistas en la Ruta de las Flores.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }

            when {
                cargando -> Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                errorCarga != null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(errorCarga.orEmpty(), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    Button(onClick = { intentoCarga++ }) { Text("Reintentar") }
                }
                planes.isEmpty() -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Aún no hay planes configurados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Lista de Tarjetas
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                planes.sortedBy { it.precio }.forEach { plan ->
                    TarjetaElegante(
                        plan = plan,
                        onSolicitar = { onSolicitarPlan(plan) },
                        habilitado = !cargando && errorCarga == null
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TarjetaElegante(
    plan: PlanComercio,
    onSolicitar: () -> Unit,
    habilitado: Boolean
) {
    val planNombre = plan.nombre.lowercase()
    val esPremium = planNombre.contains("premium") || planNombre.contains("25")
    val esSocio = !esPremium && (planNombre.contains("socio") || planNombre.contains("15"))

    // Definición sutil de colores (Básico ahora usa SlateBasic)
    val colorAcento = when {
        esPremium -> GoldPremium
        esSocio -> GreenSocio
        else -> SlateBasic
    }

    val colorFondoTarjeta = MaterialTheme.colorScheme.surface
    val colorBorde = if (esPremium || esSocio) colorAcento.copy(alpha = 0.5f) else SoftGrayBorder

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorFondoTarjeta),
        border = BorderStroke(1.dp, colorBorde),
        elevation = CardDefaults.cardElevation(defaultElevation = if (esPremium) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Encabezado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = plan.precioFormateado() ?: "Consultar",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorAcento
                    )
                }

                // Indicador visual sutil
                if (esPremium || esSocio) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colorAcento.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (esPremium) "Recomendado" else "Destacado",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorAcento
                        )
                    }
                }
            }

            Text(
                text = plan.descripcion.ifBlank { "El equipo confirmará los detalles contigo." },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            // Beneficios
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                plan.beneficios.forEach { beneficio ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = colorAcento,
                            modifier = Modifier.size(18.dp).offset(y = 2.dp)
                        )
                        Text(
                            text = beneficio,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Botón elegante estándar
            Button(
                onClick = onSolicitar,
                enabled = habilitado && plan.documentId.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAcento)
            ) {
                Text("Solicitar plan", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}