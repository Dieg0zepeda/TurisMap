package com.turismap.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.*
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.app_name
import turismap.shared.generated.resources.auth_btn_google
import turismap.shared.generated.resources.auth_btn_iniciar_sesion
import turismap.shared.generated.resources.auth_btn_registrate
import turismap.shared.generated.resources.auth_cd_google
import turismap.shared.generated.resources.auth_cd_mostrar_contrasena
import turismap.shared.generated.resources.auth_cd_ocultar_contrasena
import turismap.shared.generated.resources.auth_contrasena_label
import turismap.shared.generated.resources.auth_correo_label
import turismap.shared.generated.resources.auth_no_tienes_cuenta
import turismap.shared.generated.resources.auth_o_continua_con
import turismap.shared.generated.resources.auth_olvide_contrasena
import turismap.shared.generated.resources.auth_ruta_flores

@Composable
fun PantallaInicioSesion(
    onLoginExitoso: () -> Unit = {},
    onNavegarARegistro: () -> Unit = {}
) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var contrasenaVisible by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    // Validación básica
    val formularioValido = correo.isNotBlank() && contrasena.length >= 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisFondo)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Logo/Brand
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = stringResource(Res.string.app_name).uppercase(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = VerdeTextoAdaptable,
            letterSpacing = 2.sp
        )
        
        Text(
            text = stringResource(Res.string.auth_ruta_flores),
            fontSize = 16.sp,
            color = GrisTextoSecundario,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Campo de correo
        val etiquetaCorreo = stringResource(Res.string.auth_correo_label)
        OutlinedTextField(
            value = correo,
            onValueChange = { 
                correo = it
                mensajeError = null
            },
            label = { Text(etiquetaCorreo) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = etiquetaCorreo,
                    tint = VerdeTextoAdaptable
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerdePrimario,
                unfocusedBorderColor = GrisBorde
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Campo de contraseña
        val etiquetaContrasena = stringResource(Res.string.auth_contrasena_label)
        OutlinedTextField(
            value = contrasena,
            onValueChange = { 
                contrasena = it
                mensajeError = null
            },
            label = { Text(etiquetaContrasena) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = etiquetaContrasena,
                    tint = VerdeTextoAdaptable
                )
            },
            trailingIcon = {
                IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                    Icon(
                        imageVector = if (contrasenaVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = stringResource(
                            if (contrasenaVisible) Res.string.auth_cd_ocultar_contrasena else Res.string.auth_cd_mostrar_contrasena
                        ),
                        tint = GrisTextoSecundario
                    )
                }
            },
            visualTransformation = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerdePrimario,
                unfocusedBorderColor = GrisBorde
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Mensaje de error
        mensajeError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Link de olvidaste contraseña
        Text(
            text = stringResource(Res.string.auth_olvide_contrasena),
            fontSize = 13.sp,
            color = VerdeTextoAdaptable,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp),
            textAlign = TextAlign.End
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Botón de inicio de sesión
        Button(
            onClick = {
                cargando = true
                // Aquí se implementará la lógica de autenticación en PT2026-28
                // Por ahora simulamos un login exitoso
                cargando = false
                onLoginExitoso()
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            enabled = formularioValido && !cargando,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdePrimario,
                disabledContainerColor = GrisBorde
            )
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = stringResource(Res.string.auth_btn_iniciar_sesion),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Link de registro
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.auth_no_tienes_cuenta),
                fontSize = 14.sp,
                color = GrisTextoSecundario
            )
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(
                onClick = onNavegarARegistro,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = stringResource(Res.string.auth_btn_registrate),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdeTextoAdaptable
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Divisor
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                color = GrisBorde,
                thickness = 1.dp
            )
            Text(
                text = stringResource(Res.string.auth_o_continua_con),
                fontSize = 12.sp,
                color = GrisTextoSecundario,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Divider(
                modifier = Modifier.weight(1f),
                color = GrisBorde,
                thickness = 1.dp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Botón de Google Sign In (placeholder)
        OutlinedButton(
            onClick = { /* TODO: Implementar Google Sign-In */ },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GrisBorde)
        ) {
            Icon(
                // TODO: Agregar icono de Google
                imageVector = Icons.Default.Email,
                contentDescription = stringResource(Res.string.auth_cd_google),
                tint = GrisTextoSecundario,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.auth_btn_google),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A202C)
            )
        }
    }
}
