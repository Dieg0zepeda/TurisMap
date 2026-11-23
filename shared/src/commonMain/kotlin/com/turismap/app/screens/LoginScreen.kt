package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.data.repository.AutenticacionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.auth_badge_pueblos_vivos
import turismap.shared.generated.resources.auth_bienvenida_descripcion
import turismap.shared.generated.resources.auth_bienvenida_titulo
import turismap.shared.generated.resources.auth_btn_crear_cuenta
import turismap.shared.generated.resources.auth_btn_google
import turismap.shared.generated.resources.auth_btn_ingresar_ruta
import turismap.shared.generated.resources.auth_btn_registrate_gratis
import turismap.shared.generated.resources.auth_cd_logo
import turismap.shared.generated.resources.auth_cd_mostrar_contrasena
import turismap.shared.generated.resources.auth_cd_ocultar_contrasena
import turismap.shared.generated.resources.auth_contrasena_label
import turismap.shared.generated.resources.auth_correo_label
import turismap.shared.generated.resources.auth_correo_placeholder
import turismap.shared.generated.resources.auth_error_contrasena_requerida
import turismap.shared.generated.resources.auth_error_correo_invalido
import turismap.shared.generated.resources.auth_error_correo_requerido
import turismap.shared.generated.resources.auth_error_correo_restablecer
import turismap.shared.generated.resources.auth_error_crear_cuenta
import turismap.shared.generated.resources.auth_error_google
import turismap.shared.generated.resources.auth_error_google_no_configurado
import turismap.shared.generated.resources.auth_error_iniciar_sesion
import turismap.shared.generated.resources.auth_error_restablecer
import turismap.shared.generated.resources.auth_interes_artesanias
import turismap.shared.generated.resources.auth_interes_cafe
import turismap.shared.generated.resources.auth_interes_cascadas
import turismap.shared.generated.resources.auth_interes_miradores
import turismap.shared.generated.resources.auth_o_continua_con
import turismap.shared.generated.resources.auth_olvide_contrasena
import turismap.shared.generated.resources.auth_registro_contrasena_label
import turismap.shared.generated.resources.auth_registro_descripcion
import turismap.shared.generated.resources.auth_registro_intereses_titulo
import turismap.shared.generated.resources.auth_registro_nombre_label
import turismap.shared.generated.resources.auth_registro_terminos
import turismap.shared.generated.resources.auth_registro_titulo
import turismap.shared.generated.resources.auth_restablecimiento_enviado
import turismap.shared.generated.resources.auth_sin_cuenta_pregunta
import turismap.shared.generated.resources.auth_subtitulo_ruta
import turismap.shared.generated.resources.logo_turismap
import turismap.shared.generated.resources.repo_auth_error_contrasena_longitud
import turismap.shared.generated.resources.repo_auth_error_nombre_longitud

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit = {},
    onSolicitarGoogleSignIn: ((onTokenRecibido: (String?) -> Unit) -> Unit)? = null
) {
    val esOscuro = isSystemInDarkTheme()
    val colorFondoArriba = if (esOscuro) Color(0xFF12221A) else Color(0xFFE8F0EC)
    val colorFondoAbajo = if (esOscuro) Color(0xFF0F1A14) else Color(0xFFF7FAF8)
    val verdeOscuro = if (esOscuro) Color(0xFF4CAF50) else Color(0xFF1B4934)
    val terracota = if (esOscuro) Color(0xFFFF8A65) else Color(0xFFD96B52)
    val grisTexto = if (esOscuro) Color(0xFFB0BEC5) else Color(0xFF555555)
    val fondoCampos = if (esOscuro) Color(0xFF1E1E1E) else Color.White

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
                    colors = listOf(colorFondoArriba, colorFondoAbajo)
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Logo
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(fondoCampos)
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.logo_turismap),
                contentDescription = stringResource(Res.string.auth_cd_logo),
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = fondoCampos,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = terracota, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(Res.string.auth_badge_pueblos_vivos), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = terracota)
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
            color = verdeOscuro
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.auth_bienvenida_descripcion),
            fontSize = 14.sp,
            color = grisTexto,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo Correo
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.auth_correo_label), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = grisTexto)
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
                    unfocusedContainerColor = fondoCampos,
                    focusedContainerColor = fondoCampos,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = verdeOscuro
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
            Text(stringResource(Res.string.auth_contrasena_label), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = grisTexto)
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
                    unfocusedContainerColor = fondoCampos,
                    focusedContainerColor = fondoCampos,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = verdeOscuro
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
            Text(stringResource(Res.string.auth_restablecimiento_enviado), color = verdeOscuro, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.auth_olvide_contrasena),
            fontSize = 13.sp,
            color = terracota,
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
            colors = ButtonDefaults.buttonColors(containerColor = verdeOscuro),
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
            HorizontalDivider(modifier = Modifier.weight(1f), color = grisTexto.copy(alpha = 0.3f))
            Text(
                text = stringResource(Res.string.auth_o_continua_con),
                color = grisTexto,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = grisTexto.copy(alpha = 0.3f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botón de Inicio con Google
        OutlinedButton(
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
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, if (esOscuro) Color(0xFF333333) else Color(0xFFDDDDDD)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = fondoCampos
            ),
            enabled = !cargando && !cargandoGoogle
        ) {
            if (cargandoGoogle) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = verdeOscuro)
            } else {
                // Icono 'G' de Google
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
                    color = if (esOscuro) Color.White else Color(0xFF333333)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Enlace Registro
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.auth_sin_cuenta_pregunta), color = grisTexto, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(Res.string.auth_btn_registrate_gratis),
                color = verdeOscuro,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { mostrarVentanaRegistro = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (mostrarVentanaRegistro) {
            VentanaRegistro(
                onDismiss = { mostrarVentanaRegistro = false },
                onRegistroExitoso = {
                    mostrarVentanaRegistro = false
                    onLoginExitoso()
                }
            )
        }
    }
}

private val EMAIL_REGEX = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$".toRegex()

fun esEmailValido(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentanaRegistro(onDismiss: () -> Unit, onRegistroExitoso: () -> Unit) {
    val esOscuro = isSystemInDarkTheme()
    val verdeOscuro = if (esOscuro) Color(0xFF4CAF50) else Color(0xFF1B4934)
    val colorFondo = if (esOscuro) Color(0xFF1E1E1E) else Color(0xFFF7FAF8)
    val grisTexto = if (esOscuro) Color(0xFFB0BEC5) else Color(0xFF555555)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var aceptoTerminos by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    var interesCascadas by remember { mutableStateOf(false) }
    var interesCafe by remember { mutableStateOf(false) }
    var interesArtesanias by remember { mutableStateOf(false) }
    var interesMiradores by remember { mutableStateOf(false) }

    val nombreCorto = nombre.isNotEmpty() && nombre.trim().length < 2
    val claveCorta = password.isNotEmpty() && password.length < 8
    val formularioValido = nombre.trim().length >= 2 && esEmailValido(email) && password.length >= 8 && aceptoTerminos

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorFondo,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(stringResource(Res.string.auth_registro_titulo), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = verdeOscuro)
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(Res.string.auth_registro_descripcion), fontSize = 14.sp, color = grisTexto)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it },
                label = { Text(stringResource(Res.string.auth_registro_nombre_label)) },
                leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nombreCorto,
                supportingText = {
                    if (nombreCorto) {
                        Text(
                            stringResource(Res.string.repo_auth_error_nombre_longitud),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = verdeOscuro),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text(stringResource(Res.string.auth_correo_label)) },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = verdeOscuro),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text(stringResource(Res.string.auth_registro_contrasena_label)) },
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = claveCorta,
                supportingText = {
                    if (claveCorta) {
                        Text(
                            stringResource(Res.string.repo_auth_error_contrasena_longitud),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = verdeOscuro),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(stringResource(Res.string.auth_registro_intereses_titulo), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = verdeOscuro)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipInteres(stringResource(Res.string.auth_interes_cascadas), interesCascadas, Modifier.weight(1f)) { interesCascadas = !interesCascadas }
                ChipInteres(stringResource(Res.string.auth_interes_cafe), interesCafe, Modifier.weight(1f)) { interesCafe = !interesCafe }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipInteres(stringResource(Res.string.auth_interes_artesanias), interesArtesanias, Modifier.weight(1f)) { interesArtesanias = !interesArtesanias }
                ChipInteres(stringResource(Res.string.auth_interes_miradores), interesMiradores, Modifier.weight(1f)) { interesMiradores = !interesMiradores }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = aceptoTerminos, onCheckedChange = { aceptoTerminos = it }, colors = CheckboxDefaults.colors(checkedColor = verdeOscuro))
                Text(stringResource(Res.string.auth_registro_terminos), fontSize = 12.sp, color = grisTexto)
            }

            mensajeError?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!cargando && formularioValido) {
                        cargando = true
                        mensajeError = null
                        val intereses = buildList {
                            if (interesCascadas) add("Cascadas")
                            if (interesCafe) add("Fincas de Café")
                            if (interesArtesanias) add("Artesanías")
                            if (interesMiradores) add("Miradores")
                        }
                        coroutineScope.launch {
                            try {
                                AutenticacionRepository.registrar(nombre, email, password, intereses)
                                onRegistroExitoso()
                            } catch (error: CancellationException) {
                                throw error
                            } catch (error: Exception) {
                                mensajeError = error.message ?: getString(Res.string.auth_error_crear_cuenta)
                            } finally {
                                cargando = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = verdeOscuro,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !cargando && formularioValido
            ) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(stringResource(Res.string.auth_btn_crear_cuenta), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun ChipInteres(texto: String, seleccionado: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val esOscuro = isSystemInDarkTheme()
    val verdeOscuro = if (esOscuro) Color(0xFF4CAF50) else Color(0xFF1B4934)
    val fondoChip = if (esOscuro) Color(0xFF1E1E1E) else Color.White
    val colorBorde = if (esOscuro) Color(0xFF333333) else Color.LightGray
    val colorTexto = if (esOscuro) Color(0xFFB0BEC5) else Color.Gray

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (seleccionado) verdeOscuro.copy(alpha = 0.1f) else fondoChip,
        border = BorderStroke(1.dp, if (seleccionado) verdeOscuro else colorBorde),
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            color = if (seleccionado) verdeOscuro else colorTexto,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}