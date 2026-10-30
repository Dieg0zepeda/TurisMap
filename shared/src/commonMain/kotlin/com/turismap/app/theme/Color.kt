package com.turismap.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val VerdePrimario = Color(0xFF1B4D3E)
val TerracotaAcento = Color(0xFFC25E3E)

val GrisFondo: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF121212) else Color(0xFFF7F9F8)

val GrisTextoSecundario: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFA0AEC0) else Color(0xFF718096)

val GrisBorde: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF2D3748) else Color(0xFFE2E8F0)

val FondoBlancoAdaptable: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF1E1E1E) else Color.White

val TextoPrincipalOscuro: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFF7FAFC) else Color(0xFF1A202C)

val VerdeTextoAdaptable: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF81C784) else VerdePrimario

val TerracotaTextoAdaptable: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFFE28B73) else TerracotaAcento