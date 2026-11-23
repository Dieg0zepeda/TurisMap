package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.data.repository.ComercioRepository
import com.turismap.app.data.repository.PlanComercio
import com.turismap.app.theme.FondoBlancoAdaptable
import com.turismap.app.theme.GrisBorde
import com.turismap.app.theme.GrisTextoSecundario
import com.turismap.app.theme.TextoPrincipalOscuro
import kotlinx.coroutines.CancellationException
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_reintentar
import turismap.shared.generated.resources.planes_badge_mas_popular
import turismap.shared.generated.resources.planes_btn_solicitar
import turismap.shared.generated.resources.planes_cd_volver
import turismap.shared.generated.resources.planes_encabezado_subtitulo
import turismap.shared.generated.resources.planes_encabezado_titulo
import turismap.shared.generated.resources.planes_error_cargar
import turismap.shared.generated.resources.planes_error_permisos
import turismap.shared.generated.resources.planes_estado_vacio
import turismap.shared.generated.resources.planes_nota_aclaratoria
import turismap.shared.generated.resources.planes_precio_consultar
import turismap.shared.generated.resources.planes_titulo_barra

private val VerdeBosqueClaro = Color(0xFF1B4934)
private val VerdeSalviaOscuro = Color(0xFF6FCF97)
private val TerracotaCta = Color(0xFFD96B52)
private val AmbarPremiumOscuro = Color(0xFFE9C46A)
private val AmbarPremiumClaro = Color(0xFF9A6700)
private val SuperficieSocioOscuro = Color(0xFF18241F)
private val SuperficieSocioClaro = Color(0xFFF3FAF6)

private enum class TipoPlanVisual(val prioridadVisual: Int) { SOCIO(0), PREMIUM(1), BASICO(2), OTRO(3) }
private data class PrecioVisualPlan(val montoPrincipal: String, val sufijo: String?)

private fun clasificarPlanVisual(plan: PlanComercio): TipoPlanVisual {
    val id = plan.documentId.trim().lowercase()
    val nombre = plan.nombre.trim().lowercase()
    return when {
        id == "socio_turismap" -> TipoPlanVisual.SOCIO
        id == "socio_premium" -> TipoPlanVisual.PREMIUM
        id == "presencia_basica" -> TipoPlanVisual.BASICO
        nombre.contains("premium") || plan.precio == 25.0 -> TipoPlanVisual.PREMIUM
        nombre.contains("socio") || plan.precio == 15.0 -> TipoPlanVisual.SOCIO
        nombre.contains("básic") || nombre.contains("basic") ||
            nombre.contains("presencia") || plan.precio == 5.0 -> TipoPlanVisual.BASICO
        else -> TipoPlanVisual.OTRO
    }
}

private fun formatearPrecioVisual(plan: PlanComercio): PrecioVisualPlan? {
    val valor = plan.precio?.takeIf { it.isFinite() && it >= 0.0 } ?: return null
    val valorTexto = valor.toString()
    val numero = if (valorTexto.endsWith(".0")) valorTexto.dropLast(2) else valorTexto
    val monedaLimpia = plan.moneda.trim()
    val periodicidadLimpia = plan.periodicidad.trim().lowercase()
    val esDolar = monedaLimpia.equals("USD", ignoreCase = true) || monedaLimpia == "$"
    val montoPrincipal = if (esDolar) "$$numero" else numero
    val sufijo = buildList {
        if (monedaLimpia.isNotBlank() && monedaLimpia != "$") add(monedaLimpia.uppercase())
        if (periodicidadLimpia.isNotBlank()) add("/ $periodicidadLimpia")
    }.joinToString(" ").ifBlank { null }
    return PrecioVisualPlan(montoPrincipal = montoPrincipal, sufijo = sufijo)
}

private fun descripcionPropiaDelPlan(plan: PlanComercio, descripcionRepetida: String?): String? {
    val limpia = plan.descripcion.trim()
    if (limpia.isBlank()) return null
    val norm = limpia.lowercase()
    val esNotaLegal = norm.contains("propuesta inicial") ||
        norm.contains("se confirmarán antes de activar") ||
        norm.contains("se confirmaran antes de activar") ||
        norm.contains("initial reference proposal") ||
        (descripcionRepetida != null && limpia.equals(descripcionRepetida, ignoreCase = true))
    return if (esNotaLegal) null else limpia
}

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
                getString(Res.string.planes_error_permisos)
            } else {
                mensaje.ifBlank { getString(Res.string.planes_error_cargar) }
            }
        } finally {
            cargando = false
        }
    }

    val planesOrdenados = remember(planes) {
        planes.sortedWith(compareBy<PlanComercio>({ clasificarPlanVisual(it).prioridadVisual }, { it.orden }))
    }
    val descripcionRepetida = remember(planes) {
        val descripciones = planes.map { it.descripcion.trim() }.filter(String::isNotBlank)
        if (descripciones.size > 1 && descripciones.distinct().size == 1) descripciones.first() else null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.planes_titulo_barra), fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.planes_cd_volver))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = TextoPrincipalOscuro,
                    navigationIconContentColor = TextoPrincipalOscuro
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(Res.string.planes_encabezado_titulo), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextoPrincipalOscuro)
                Text(stringResource(Res.string.planes_encabezado_subtitulo), fontSize = 14.5.sp, color = GrisTextoSecundario, lineHeight = 21.sp)
            }

            when {
                cargando -> Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = if (isSystemInDarkTheme()) VerdeSalviaOscuro else VerdeBosqueClaro)
                }

                errorCarga != null -> Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = FondoBlancoAdaptable,
                    border = BorderStroke(1.dp, GrisBorde)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(errorCarga.orEmpty(), color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, fontSize = 14.sp)
                        Button(
                            onClick = { intentoCarga++ },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracotaCta, contentColor = Color.White)
                        ) { Text(stringResource(Res.string.btn_reintentar), fontWeight = FontWeight.SemiBold) }
                    }
                }

                planesOrdenados.isEmpty() -> Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.planes_estado_vacio), color = GrisTextoSecundario, fontSize = 14.sp)
                }

                else -> {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        planesOrdenados.forEach { plan ->
                            TarjetaPlanComercio(
                                plan = plan,
                                tipo = clasificarPlanVisual(plan),
                                descripcionPropia = descripcionPropiaDelPlan(plan, descripcionRepetida),
                                onSolicitar = { onSolicitarPlan(plan) },
                                habilitado = !cargando && errorCarga == null
                            )
                        }
                    }
                    NotaAclaratoriaPlanes()
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TarjetaPlanComercio(
    plan: PlanComercio,
    tipo: TipoPlanVisual,
    descripcionPropia: String?,
    onSolicitar: () -> Unit,
    habilitado: Boolean
) {
    val esModoOscuro = isSystemInDarkTheme()
    val esSocio = tipo == TipoPlanVisual.SOCIO
    val esPremium = tipo == TipoPlanVisual.PREMIUM
    val precioVisual = remember(plan.precio, plan.moneda, plan.periodicidad) { formatearPrecioVisual(plan) }

    val colorAcento = when {
        esSocio -> if (esModoOscuro) VerdeSalviaOscuro else VerdeBosqueClaro
        esPremium -> if (esModoOscuro) AmbarPremiumOscuro else AmbarPremiumClaro
        else -> GrisTextoSecundario
    }
    val colorPrecio = if (esSocio || esPremium) colorAcento else TextoPrincipalOscuro
    val colorFondo = if (esSocio) (if (esModoOscuro) SuperficieSocioOscuro else SuperficieSocioClaro) else FondoBlancoAdaptable
    val bordeTarjeta = when {
        esSocio -> BorderStroke(1.5.dp, colorAcento.copy(alpha = if (esModoOscuro) 0.55f else 0.45f))
        esPremium -> BorderStroke(1.dp, colorAcento.copy(alpha = if (esModoOscuro) 0.42f else 0.35f))
        else -> BorderStroke(1.dp, GrisBorde)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colorFondo,
        border = bordeTarjeta,
        shadowElevation = if (esSocio) 3.dp else if (esPremium) 1.dp else 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(plan.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoPrincipalOscuro, modifier = Modifier.weight(1f))
                if (esSocio) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colorAcento.copy(alpha = if (esModoOscuro) 0.18f else 0.12f),
                        border = BorderStroke(1.dp, colorAcento.copy(alpha = if (esModoOscuro) 0.45f else 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = colorAcento, modifier = Modifier.size(13.dp))
                            Text(stringResource(Res.string.planes_badge_mas_popular), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = colorAcento)
                        }
                    }
                }
            }

            if (precioVisual != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                    Text(precioVisual.montoPrincipal, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = colorPrecio, modifier = Modifier.alignByBaseline())
                    precioVisual.sufijo?.let { sufijo ->
                        Text(sufijo, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = GrisTextoSecundario, modifier = Modifier.alignByBaseline())
                    }
                }
            } else {
                Text(stringResource(Res.string.planes_precio_consultar), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorPrecio)
            }

            if (!descripcionPropia.isNullOrBlank()) {
                Text(descripcionPropia, fontSize = 13.5.sp, color = GrisTextoSecundario, lineHeight = 19.sp)
            }

            HorizontalDivider(
                color = if (esSocio) colorAcento.copy(alpha = if (esModoOscuro) 0.22f else 0.18f) else GrisBorde,
                thickness = 1.dp
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                plan.beneficios.forEach { beneficio ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colorAcento, modifier = Modifier.size(18.dp).offset(y = 1.dp))
                        Text(beneficio, fontSize = 13.5.sp, color = TextoPrincipalOscuro, lineHeight = 19.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            BotonPlanComercio(
                tipo = tipo,
                colorAcento = colorAcento,
                esModoOscuro = esModoOscuro,
                habilitado = habilitado && plan.documentId.isNotBlank(),
                onSolicitar = onSolicitar
            )
        }
    }
}

@Composable
private fun BotonPlanComercio(
    tipo: TipoPlanVisual,
    colorAcento: Color,
    esModoOscuro: Boolean,
    habilitado: Boolean,
    onSolicitar: () -> Unit
) {
    val texto = stringResource(Res.string.planes_btn_solicitar)
    when (tipo) {
        TipoPlanVisual.SOCIO -> Button(
            onClick = onSolicitar,
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerracotaCta, contentColor = Color.White)
        ) { Text(texto, fontSize = 15.sp, fontWeight = FontWeight.Bold) }

        TipoPlanVisual.PREMIUM -> OutlinedButton(
            onClick = onSolicitar,
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.2.dp, colorAcento.copy(alpha = if (esModoOscuro) 0.65f else 0.50f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colorAcento.copy(alpha = if (esModoOscuro) 0.16f else 0.10f),
                contentColor = colorAcento
            )
        ) { Text(texto, fontSize = 15.sp, fontWeight = FontWeight.Bold) }

        else -> OutlinedButton(
            onClick = onSolicitar,
            enabled = habilitado,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, GrisBorde),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoPrincipalOscuro)
        ) { Text(texto, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun NotaAclaratoriaPlanes() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Outlined.Info, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(16.dp).offset(y = 2.dp))
        Text(stringResource(Res.string.planes_nota_aclaratoria), fontSize = 12.sp, color = GrisTextoSecundario, lineHeight = 17.sp)
    }
}
