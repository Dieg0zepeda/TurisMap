package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import org.jetbrains.compose.resources.painterResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.logo_turismap

@Composable
fun LoginScreen(onLoginExitoso: () -> Unit = {}) {
    // Colores basados en el diseño
    val colorFondoArriba = Color(0xFFE8F0EC)
    val colorFondoAbajo = Color(0xFFF7FAF8)
    val verdeOscuro = Color(0xFF1B4934)
    val terracota = Color(0xFFD96B52)
    val grisTexto = Color(0xFF555555)

    // Estados para los campos de texto
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var mostrarVentanaRegistro by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var restablecimientoEnviado by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Función de validación (Ahora usa Kotlin puro para KMP)
    fun validarFormulario(): Boolean {
        var isValid = true
        emailError = null
        passwordError = null

        if (email.isBlank()) {
            emailError = "El correo es requerido"
            isValid = false
        } else if (!esEmailValido(email)) {
            emailError = "Formato de correo inválido"
            isValid = false
        }

        if (password.length < 6) {
            passwordError = "La contraseña debe tener al menos 6 caracteres"
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

        // 1. Logo (Imagen real de 4 cuadrantes)
        Box(
            modifier = Modifier
                .size(150.dp) // Un poco más grande para que luzca bien
                .clip(CircleShape)
                .background(Color.White)
                .padding(5.dp), // Menos padding para aprovechar el espacio
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = org.jetbrains.compose.resources.painterResource(Res.drawable.logo_turismap),
                contentDescription = "Logo TurisMap 4 Cuadrantes",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Etiqueta "PUEBLOS VIVOS"
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = terracota, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("PUEBLOS VIVOS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = terracota)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Textos de Bienvenida
        Text(
            text = "RUTA DE LAS FLORES - EL SALVADOR",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Bienvenido a TurisMap",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = verdeOscuro
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Explora pueblos vivos, cascadas y la mejor ruta del café en El Salvador.",
            fontSize = 14.sp,
            color = grisTexto,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Formulario
        // Campo Correo
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Correo electrónico", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = grisTexto)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("viajero@laruta.sv") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = verdeOscuro
                ),
                shape = RoundedCornerShape(12.dp)
            )
            if (emailError != null) {
                Text(text = emailError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Contraseña
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Contraseña", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = grisTexto)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("••••••••••••") },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Mostrar contraseña")
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = passwordError != null,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = verdeOscuro
                ),
                shape = RoundedCornerShape(12.dp)
            )
            if (passwordError != null) {
                Text(text = passwordError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
            }
        }
        mensajeError?.let { mensaje ->
            Text(mensaje, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
        if (restablecimientoEnviado) {
            Text("Si el correo está registrado, recibirás instrucciones para restablecer tu contraseña.", color = verdeOscuro, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recuperación de contraseña
        Text(
            text = "¿Olvidaste tu contraseña?",
            fontSize = 13.sp,
            color = terracota,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.End).clickable {
                if (!esEmailValido(email)) {
                    emailError = "Ingresa un correo válido para restablecer tu contraseña"
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
                            mensajeError = error.message ?: "No se pudo solicitar el restablecimiento."
                        } finally {
                            cargando = false
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Ingresar
        Button(
            onClick = {
                if (validarFormulario() && !cargando) {
                    cargando = true
                    mensajeError = null
                    coroutineScope.launch {
                        try {
                            AutenticacionRepository.iniciarSesion(email, password)
                            onLoginExitoso()
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            mensajeError = error.message ?: "No se pudo iniciar sesión."
                        } finally {
                            cargando = false
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = verdeOscuro),
            shape = RoundedCornerShape(16.dp),
            enabled = !cargando
        ) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Ingresar a la Ruta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Enlace Registro
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿Aún no tienes cuenta? ", color = grisTexto, fontSize = 14.sp)
            Text(
                text = "Regístrate gratis",
                color = verdeOscuro,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { mostrarVentanaRegistro = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        // ... aquí termina tu diseño principal de Login

        if (mostrarVentanaRegistro) {
            VentanaRegistro(
                onDismiss = { mostrarVentanaRegistro = false }, // Para cerrar si tocan afuera
                onRegistroExitoso = {
                    mostrarVentanaRegistro = false
                    onLoginExitoso()
                }
            )
        }
    } // Fin de LoginScreen

}



// Validación usando Regex de Kotlin (Compatible con KMP)
fun esEmailValido(email: String): Boolean {
    val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})".toRegex()
    return emailRegex.matches(email)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentanaRegistro(onDismiss: () -> Unit, onRegistroExitoso: () -> Unit) {
    val verdeOscuro = Color(0xFF1B4934)
    val colorFondo = Color(0xFFF7FAF8)
    val grisTexto = Color(0xFF555555)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var aceptoTerminos by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Intereses (Chips)
    var interesCascadas by remember { mutableStateOf(false) }
    var interesCafe by remember { mutableStateOf(false) }
    var interesArtesanias by remember { mutableStateOf(false) }
    var interesMiradores by remember { mutableStateOf(false) }

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
            // Título
            Text("Crea tu cuenta de viajero", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = verdeOscuro)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Descubre rutas secretas, guarda tus municipios favoritos y planifica tu aventura.", fontSize = 14.sp, color = grisTexto)

            Spacer(modifier = Modifier.height(24.dp))

            // Campo Nombre
            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it },
                label = { Text("Nombre completo") },
                leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = verdeOscuro),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Correo
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = verdeOscuro),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Crea una clave segura") },
                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Mostrar")
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = verdeOscuro),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Intereses
            Text("¿Qué te inspira explorar?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = verdeOscuro)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipInteres("Cascadas", interesCascadas, Modifier.weight(1f)) { interesCascadas = !interesCascadas }
                ChipInteres("Fincas de Café", interesCafe, Modifier.weight(1f)) { interesCafe = !interesCafe }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipInteres("Artesanías", interesArtesanias, Modifier.weight(1f)) { interesArtesanias = !interesArtesanias }
                ChipInteres("Miradores", interesMiradores, Modifier.weight(1f)) { interesMiradores = !interesMiradores }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Términos
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = aceptoTerminos, onCheckedChange = { aceptoTerminos = it }, colors = CheckboxDefaults.colors(checkedColor = verdeOscuro))
                Text("Acepto los Términos y Condiciones", fontSize = 12.sp, color = grisTexto)
            }

            mensajeError?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Crear Cuenta
            Button(
                onClick = {
                    if (!cargando && nombre.isNotBlank() && esEmailValido(email) && password.length >= 6 && aceptoTerminos) {
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
                                mensajeError = error.message ?: "No se pudo crear la cuenta."
                            } finally {
                                cargando = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = verdeOscuro),
                shape = RoundedCornerShape(16.dp),
                enabled = !cargando && nombre.isNotBlank() && esEmailValido(email) && password.length >= 6 && aceptoTerminos
            ) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Crear Cuenta Gratis", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun ChipInteres(texto: String, seleccionado: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val verdeOscuro = Color(0xFF1B4934)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (seleccionado) verdeOscuro.copy(alpha = 0.1f) else Color.White,
        border = BorderStroke(1.dp, if (seleccionado) verdeOscuro else Color.LightGray),
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(vertical = 12.dp),
            textAlign = TextAlign.Center,
            color = if (seleccionado) verdeOscuro else Color.Gray,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
    }
}