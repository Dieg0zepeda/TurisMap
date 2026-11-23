package com.turismap.app.screens.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.auth_badge_pueblos_vivos
import turismap.shared.generated.resources.auth_bienvenida_descripcion
import turismap.shared.generated.resources.auth_bienvenida_titulo
import turismap.shared.generated.resources.auth_btn_google
import turismap.shared.generated.resources.auth_cd_logo
import turismap.shared.generated.resources.auth_subtitulo_ruta
import turismap.shared.generated.resources.logo_turismap

internal data class LoginPaleta(
    val esOscuro: Boolean,
    val colorFondoArriba: Color,
    val colorFondoAbajo: Color,
    val verdeOscuro: Color,
    val terracota: Color,
    val grisTexto: Color,
    val fondoCampos: Color,
    val colorFondoSheet: Color
)

@Composable
internal fun rememberLoginPaleta(): LoginPaleta {
    val esOscuro = isSystemInDarkTheme()
    return remember(esOscuro) {
        LoginPaleta(
            esOscuro = esOscuro,
            colorFondoArriba = if (esOscuro) Color(0xFF12221A) else Color(0xFFE8F0EC),
            colorFondoAbajo = if (esOscuro) Color(0xFF0F1A14) else Color(0xFFF7FAF8),
            verdeOscuro = if (esOscuro) Color(0xFF4CAF50) else Color(0xFF1B4934),
            terracota = if (esOscuro) Color(0xFFFF8A65) else Color(0xFFD96B52),
            grisTexto = if (esOscuro) Color(0xFFB0BEC5) else Color(0xFF555555),
            fondoCampos = if (esOscuro) Color(0xFF1E1E1E) else Color.White,
            colorFondoSheet = if (esOscuro) Color(0xFF1E1E1E) else Color(0xFFF7FAF8)
        )
    }
}

@Composable
internal fun CabeceraLogin(paleta: LoginPaleta) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(paleta.fondoCampos)
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.logo_turismap),
                contentDescription = stringResource(Res.string.auth_cd_logo),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = paleta.fondoCampos,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = paleta.terracota,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    stringResource(Res.string.auth_badge_pueblos_vivos),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = paleta.terracota
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.auth_subtitulo_ruta),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.auth_bienvenida_titulo),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = paleta.verdeOscuro
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.auth_bienvenida_descripcion),
            fontSize = 14.sp,
            color = paleta.grisTexto,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
internal fun BotonGoogleSignIn(
    cargandoGoogle: Boolean,
    habilitado: Boolean,
    paleta: LoginPaleta,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (paleta.esOscuro) Color(0xFF333333) else Color(0xFFDDDDDD)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = paleta.fondoCampos),
        enabled = habilitado
    ) {
        if (cargandoGoogle) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = paleta.verdeOscuro)
        } else {
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(24.dp),
                shadowElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("G", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF4285F4))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.auth_btn_google),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (paleta.esOscuro) Color.White else Color(0xFF333333)
            )
        }
    }
}

@Composable
internal fun ChipInteresLogin(
    texto: String,
    seleccionado: Boolean,
    paleta: LoginPaleta,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colorBorde = if (paleta.esOscuro) Color(0xFF333333) else Color.LightGray
    val colorTexto = if (paleta.esOscuro) Color(0xFFB0BEC5) else Color.Gray

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (seleccionado) paleta.verdeOscuro.copy(alpha = 0.1f) else paleta.fondoCampos,
        border = BorderStroke(1.dp, if (seleccionado) paleta.verdeOscuro else colorBorde),
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            color = if (seleccionado) paleta.verdeOscuro else colorTexto,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}
