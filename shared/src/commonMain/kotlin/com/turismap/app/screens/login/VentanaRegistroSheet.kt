package com.turismap.app.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.data.repository.AutenticacionRepository
import com.turismap.app.screens.esEmailValido
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.auth_btn_crear_cuenta
import turismap.shared.generated.resources.auth_cd_mostrar_contrasena
import turismap.shared.generated.resources.auth_cd_ocultar_contrasena
import turismap.shared.generated.resources.auth_correo_label
import turismap.shared.generated.resources.auth_error_crear_cuenta
import turismap.shared.generated.resources.auth_interes_artesanias
import turismap.shared.generated.resources.auth_interes_cafe
import turismap.shared.generated.resources.auth_interes_cascadas
import turismap.shared.generated.resources.auth_interes_miradores
import turismap.shared.generated.resources.auth_registro_contrasena_label
import turismap.shared.generated.resources.auth_registro_descripcion
import turismap.shared.generated.resources.auth_registro_intereses_titulo
import turismap.shared.generated.resources.auth_registro_nombre_label
import turismap.shared.generated.resources.auth_registro_terminos
import turismap.shared.generated.resources.auth_registro_titulo
import turismap.shared.generated.resources.repo_auth_error_contrasena_longitud
import turismap.shared.generated.resources.repo_auth_error_nombre_longitud

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VentanaRegistroSheet(
    paleta: LoginPaleta,
    onDismiss: () -> Unit,
    onRegistroExitoso: () -> Unit
) {
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
        containerColor = paleta.colorFondoSheet,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                stringResource(Res.string.auth_registro_titulo),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = paleta.verdeOscuro
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(Res.string.auth_registro_descripcion), fontSize = 14.sp, color = paleta.grisTexto)

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
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
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = paleta.verdeOscuro),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(Res.string.auth_correo_label)) },
                leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = paleta.verdeOscuro),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
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
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = paleta.verdeOscuro),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                stringResource(Res.string.auth_registro_intereses_titulo),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = paleta.verdeOscuro
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipInteresLogin(stringResource(Res.string.auth_interes_cascadas), interesCascadas, paleta, Modifier.weight(1f)) {
                    interesCascadas = !interesCascadas
                }
                ChipInteresLogin(stringResource(Res.string.auth_interes_cafe), interesCafe, paleta, Modifier.weight(1f)) {
                    interesCafe = !interesCafe
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipInteresLogin(stringResource(Res.string.auth_interes_artesanias), interesArtesanias, paleta, Modifier.weight(1f)) {
                    interesArtesanias = !interesArtesanias
                }
                ChipInteresLogin(stringResource(Res.string.auth_interes_miradores), interesMiradores, paleta, Modifier.weight(1f)) {
                    interesMiradores = !interesMiradores
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = aceptoTerminos,
                    onCheckedChange = { aceptoTerminos = it },
                    colors = CheckboxDefaults.colors(checkedColor = paleta.verdeOscuro)
                )
                Text(stringResource(Res.string.auth_registro_terminos), fontSize = 12.sp, color = paleta.grisTexto)
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
                    containerColor = paleta.verdeOscuro,
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
