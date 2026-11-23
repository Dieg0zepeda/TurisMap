package com.turismap.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.data.repository.AutenticacionRepository
import com.turismap.app.screens.login.BotonGoogleSignIn
import com.turismap.app.screens.login.CabeceraLogin
import com.turismap.app.screens.login.VentanaRegistroSheet
import com.turismap.app.screens.login.rememberLoginPaleta
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.auth_btn_ingresar_ruta
import turismap.shared.generated.resources.auth_btn_registrate_gratis
import turismap.shared.generated.resources.auth_cd_mostrar_contrasena
import turismap.shared.generated.resources.auth_cd_ocultar_contrasena
import turismap.shared.generated.resources.auth_contrasena_label
import turismap.shared.generated.resources.auth_correo_label
import turismap.shared.generated.resources.auth_correo_placeholder
import turismap.shared.generated.resources.auth_error_contrasena_requerida
import turismap.shared.generated.resources.auth_error_correo_invalido
import turismap.shared.generated.resources.auth_error_correo_requerido
import turismap.shared.generated.resources.auth_error_correo_restablecer
import turismap.shared.generated.resources.auth_error_google
import turismap.shared.generated.resources.auth_error_google_no_configurado
import turismap.shared.generated.resources.auth_error_iniciar_sesion
import turismap.shared.generated.resources.auth_error_restablecer
import turismap.shared.generated.resources.auth_o_continua_con
import turismap.shared.generated.resources.auth_olvide_contrasena
import turismap.shared.generated.resources.auth_restablecimiento_enviado
import turismap.shared.generated.resources.auth_sin_cuenta_pregunta

private val EMAIL_REGEX = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$".toRegex()

fun esEmailValido(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit = {},
    onSolicitarGoogleSignIn: ((onTokenRecibido: (String?) -> Unit) -> Unit)? = null
) {
    val paleta = rememberLoginPaleta()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var mostrarVentanaRegistro by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<StringResource?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var cargandoGoogle by remember { mutableStateOf(false) }
    var restablecimientoEnviado by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val errorGoogleNoConfigurado = stringResource(Res.string.auth_error_google_no_configurado)
    val errorContrasenaRequerida = stringResource(Res.string.auth_error_contrasena_requerida)

    fun validarFormulario(): Boolean {
        var isValid = true
        emailError = null

        if (email.isBlank()) {
            emailError = Res.string.auth_error_correo_requerido
            isValid = false
        } else if (!esEmailValido(email)) {
            emailError = Res.string.auth_error_correo_invalido
            isValid = false
        }

        if (password.isEmpty()) {
            mensajeError = errorContrasenaRequerida
            isValid = false
        }

        return isValid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(paleta.colorFondoArriba, paleta.colorFondoAbajo)
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        CabeceraLogin(paleta = paleta)

        Spacer(modifier = Modifier.height(32.dp))

        // Campo Correo
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.auth_correo_label), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = paleta.grisTexto)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                    mensajeError = null
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(Res.string.auth_correo_placeholder)) },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = paleta.fondoCampos,
                    focusedContainerColor = paleta.fondoCampos,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = paleta.verdeOscuro
                ),
                shape = RoundedCornerShape(12.dp)
            )
            emailError?.let { errorRes ->
                Text(text = stringResource(errorRes), color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Contraseña
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.auth_contrasena_label), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = paleta.grisTexto)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    mensajeError = null
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("••••••••••••") },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = stringResource(
                                if (passwordVisible) Res.string.auth_cd_ocultar_contrasena else Res.string.auth_cd_mostrar_contrasena
                            )
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = paleta.fondoCampos,
                    focusedContainerColor = paleta.fondoCampos,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = paleta.verdeOscuro
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        mensajeError?.let { mensaje ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(mensaje, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        if (restablecimientoEnviado) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(Res.string.auth_restablecimiento_enviado), color = paleta.verdeOscuro, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.auth_olvide_contrasena),
            fontSize = 13.sp,
            color = paleta.terracota,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.End)
                .clickable(enabled = !cargando && !cargandoGoogle) {
                    if (cargando || cargandoGoogle) return@clickable
                    if (!esEmailValido(email)) {
                        emailError = Res.string.auth_error_correo_restablecer
                    } else {
                        cargando = true
                        mensajeError = null
                        coroutineScope.launch {
                            try {
                                AutenticacionRepository.enviarRestablecimientoContrasena(email)
                                restablecimientoEnviado = true
                            } catch (error: CancellationException) {
                                throw error
                            } catch (error: Exception) {
                                mensajeError = error.message ?: getString(Res.string.auth_error_restablecer)
                            } finally {
                                cargando = false
                            }
                        }
                    }
                }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Ingresar Tradicional
        Button(
            onClick = {
                if (validarFormulario() && !cargando && !cargandoGoogle) {
                    cargando = true
                    mensajeError = null
                    coroutineScope.launch {
                        try {
                            AutenticacionRepository.iniciarSesion(email, password)
                            onLoginExitoso()
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            mensajeError = error.message ?: getString(Res.string.auth_error_iniciar_sesion)
                        } finally {
                            cargando = false
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = paleta.verdeOscuro),
            shape = RoundedCornerShape(16.dp),
            enabled = !cargando && !cargandoGoogle
        ) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text(stringResource(Res.string.auth_btn_ingresar_ruta), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Separador con texto "o continúa con"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = paleta.grisTexto.copy(alpha = 0.3f))
            Text(
                text = stringResource(Res.string.auth_o_continua_con),
                color = paleta.grisTexto,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = paleta.grisTexto.copy(alpha = 0.3f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botón de Inicio con Google
        BotonGoogleSignIn(
            cargandoGoogle = cargandoGoogle,
            habilitado = !cargando && !cargandoGoogle,
            paleta = paleta,
            onClick = {
                if (!cargando && !cargandoGoogle) {
                    mensajeError = null
                    val lanzadorGoogle = onSolicitarGoogleSignIn
                    if (lanzadorGoogle == null) {
                        mensajeError = errorGoogleNoConfigurado
                    } else {
                        cargandoGoogle = true
                        try {
                            lanzadorGoogle { idToken ->
                                if (idToken.isNullOrBlank()) {
                                    cargandoGoogle = false
                                    return@lanzadorGoogle
                                }
                                coroutineScope.launch {
                                    try {
                                        AutenticacionRepository.iniciarSesionConGoogle(idToken)
                                        onLoginExitoso()
                                    } catch (error: CancellationException) {
                                        throw error
                                    } catch (error: Exception) {
                                        mensajeError = error.message ?: getString(Res.string.auth_error_google)
                                    } finally {
                                        cargandoGoogle = false
                                    }
                                }
                            }
                        } catch (error: Exception) {
                            cargandoGoogle = false
                            mensajeError = error.message ?: errorGoogleNoConfigurado
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Enlace Registro
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.auth_sin_cuenta_pregunta), color = paleta.grisTexto, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(Res.string.auth_btn_registrate_gratis),
                color = paleta.verdeOscuro,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { mostrarVentanaRegistro = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (mostrarVentanaRegistro) {
            VentanaRegistroSheet(
                paleta = paleta,
                onDismiss = { mostrarVentanaRegistro = false },
                onRegistroExitoso = {
                    mostrarVentanaRegistro = false
                    onLoginExitoso()
                }
            )
        }
    }
}
