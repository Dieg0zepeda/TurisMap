package com.turismap.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Identidad de marca TurisMap (constantes base)
val VerdePrimario = Color(0xFF1B4D3E)
val VerdeBosqueMarca = Color(0xFF1B4934)
val VerdeSalviaOscuro = Color(0xFF52B788)
val VerdeEsmeraldaOscuro = Color(0xFF6FCF97)
val TerracotaAcento = Color(0xFFD96B52)

// Escala Tonal Dark: Bosque-Obsidiana (60-30-10)
val ObsidianaFondo = Color(0xFF0D1310)
val ObsidianaBarraInferior = Color(0xFF111A15)
val PinoSuperficie = Color(0xFF141D18)
val PinoContenedor = Color(0xFF1B2620)
val PinoContenedorAlto = Color(0xFF212F27)
val PinoBorde = Color(0xFF26362E)
val BosqueContenedorPrimario = Color(0xFF163829)
val BosqueSobreContenedorPrimario = Color(0xFFB7F0CE)
val BosqueSobrePrimario = Color(0xFF082114)
val BosqueNeblinaSuperior = Color(0xFF163024)
val TextoClaroBosque = Color(0xFFEDF3EF)
val TextoSecundarioBosque = Color(0xFF9FB0A6)
val TextoInactivoBarraOscuro = Color(0xFF8A9C91)

// Tokens adaptables a modo claro y oscuro
val GrisFondo: Color
    @Composable get() = if (isSystemInDarkTheme()) ObsidianaFondo else Color(0xFFF7F9F8)

val FondoBlancoAdaptable: Color
    @Composable get() = if (isSystemInDarkTheme()) PinoSuperficie else Color.White

val SuperficieContenedorAdaptable: Color
    @Composable get() = if (isSystemInDarkTheme()) PinoContenedor else Color.White

val SuperficieAltaAdaptable: Color
    @Composable get() = if (isSystemInDarkTheme()) PinoContenedorAlto else Color(0xFFF0F4F1)

val SuperficieBarraInferiorAdaptable: Color
    @Composable get() = if (isSystemInDarkTheme()) ObsidianaBarraInferior else Color.White

val GrisTextoSecundario: Color
    @Composable get() = if (isSystemInDarkTheme()) TextoSecundarioBosque else Color(0xFF718096)

val GrisBorde: Color
    @Composable get() = if (isSystemInDarkTheme()) PinoBorde else Color(0xFFE2E8F0)

val TextoPrincipalOscuro: Color
    @Composable get() = if (isSystemInDarkTheme()) TextoClaroBosque else Color(0xFF1A202C)

val VerdeTextoAdaptable: Color
    @Composable get() = if (isSystemInDarkTheme()) VerdeEsmeraldaOscuro else VerdeBosqueMarca

val BordeVerdeActivoAdaptable: Color
    @Composable get() = if (isSystemInDarkTheme()) VerdeSalviaOscuro else VerdeBosqueMarca

val TerracotaTextoAdaptable: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFE58A74) else TerracotaAcento
