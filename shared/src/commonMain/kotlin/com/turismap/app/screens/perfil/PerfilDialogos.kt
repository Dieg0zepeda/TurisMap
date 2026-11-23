package com.turismap.app.screens.perfil

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.TerracotaTextoAdaptable
import com.turismap.app.theme.VerdeTextoAdaptable
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_cancelar
import turismap.shared.generated.resources.btn_cerrar
import turismap.shared.generated.resources.btn_guardar
import turismap.shared.generated.resources.perfil_editar_error_nombre_corto
import turismap.shared.generated.resources.perfil_editar_guardando
import turismap.shared.generated.resources.perfil_editar_nombre_label
import turismap.shared.generated.resources.perfil_editar_telefono_label
import turismap.shared.generated.resources.perfil_editar_titulo
import turismap.shared.generated.resources.perfil_privacidad_cambiar_contrasena
import turismap.shared.generated.resources.perfil_privacidad_enviando
import turismap.shared.generated.resources.perfil_privacidad_perfil_publico_desc
import turismap.shared.generated.resources.perfil_privacidad_perfil_publico_titulo
import turismap.shared.generated.resources.perfil_privacidad_titulo
import turismap.shared.generated.resources.perfil_privacidad_ubicacion_desc
import turismap.shared.generated.resources.perfil_privacidad_ubicacion_titulo

@Composable
internal fun DialogoPrivacidadPerfil(
    perfilPublico: Boolean,
    compartirUbicacion: Boolean,
    enviandoRecuperacion: Boolean,
    paleta: PerfilPaleta,
    onPerfilPublicoChange: (Boolean) -> Unit,
    onCompartirUbicacionChange: (Boolean) -> Unit,
    onCambiarContrasenaClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!enviandoRecuperacion) onDismiss() },
        containerColor = paleta.fondoTarjeta,
        title = { Text(stringResource(Res.string.perfil_privacidad_titulo), fontWeight = FontWeight.Bold, color = paleta.colorTextoPrincipal) },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(Res.string.perfil_privacidad_perfil_publico_titulo), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = paleta.colorTextoPrincipal)
                        Text(stringResource(Res.string.perfil_privacidad_perfil_publico_desc), fontSize = 12.sp, color = paleta.colorTextoSecundario)
                    }
                    Switch(
                        checked = perfilPublico,
                        onCheckedChange = onPerfilPublicoChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VerdeTextoAdaptable,
                            checkedTrackColor = VerdeTextoAdaptable.copy(alpha = 0.5f)
                        )
                    )
                }
                HorizontalDivider(color = paleta.colorDivisor)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(Res.string.perfil_privacidad_ubicacion_titulo), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = paleta.colorTextoPrincipal)
                        Text(stringResource(Res.string.perfil_privacidad_ubicacion_desc), fontSize = 12.sp, color = paleta.colorTextoSecundario)
                    }
                    Switch(
                        checked = compartirUbicacion,
                        onCheckedChange = onCompartirUbicacionChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VerdeTextoAdaptable,
                            checkedTrackColor = VerdeTextoAdaptable.copy(alpha = 0.5f)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCambiarContrasenaClick,
                    enabled = !enviandoRecuperacion,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = paleta.colorTextoPrincipal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = paleta.fondoApp, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(
                            if (enviandoRecuperacion) {
                                Res.string.perfil_privacidad_enviando
                            } else {
                                Res.string.perfil_privacidad_cambiar_contrasena
                            }
                        ),
                        color = paleta.fondoApp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !enviandoRecuperacion
            ) {
                Text(stringResource(Res.string.btn_cerrar), color = VerdeTextoAdaptable, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
internal fun DialogoEditarPerfil(
    nombreInicial: String,
    telefonoInicial: String,
    guardandoPerfil: Boolean,
    paleta: PerfilPaleta,
    onGuardarClick: (nombre: String, telefono: String) -> Unit,
    onDismiss: () -> Unit
) {
    var tempNombre by remember(nombreInicial) { mutableStateOf(nombreInicial.take(120)) }
    var tempTelefono by remember(telefonoInicial) { mutableStateOf(telefonoInicial) }
    val nombreInvalido = tempNombre.trim().length < 2

    AlertDialog(
        onDismissRequest = { if (!guardandoPerfil) onDismiss() },
        containerColor = paleta.fondoTarjeta,
        title = { Text(stringResource(Res.string.perfil_editar_titulo), fontWeight = FontWeight.Bold, color = paleta.colorTextoPrincipal) },
        text = {
            Column {
                OutlinedTextField(
                    value = tempNombre,
                    onValueChange = { if (it.length <= 120) tempNombre = it },
                    label = { Text(stringResource(Res.string.perfil_editar_nombre_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !guardandoPerfil,
                    singleLine = true,
                    isError = nombreInvalido,
                    supportingText = {
                        if (nombreInvalido) {
                            Text(
                                stringResource(Res.string.perfil_editar_error_nombre_corto),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(color = paleta.colorTextoPrincipal)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempTelefono,
                    onValueChange = { tempTelefono = it },
                    label = { Text(stringResource(Res.string.perfil_editar_telefono_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !guardandoPerfil,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(color = paleta.colorTextoPrincipal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardarClick(tempNombre, tempTelefono) },
                enabled = !nombreInvalido && !guardandoPerfil,
                colors = ButtonDefaults.buttonColors(containerColor = paleta.verdeEsmeralda)
            ) {
                Text(
                    stringResource(
                        if (guardandoPerfil) Res.string.perfil_editar_guardando else Res.string.btn_guardar
                    ),
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !guardandoPerfil
            ) {
                Text(stringResource(Res.string.btn_cancelar), color = TerracotaTextoAdaptable)
            }
        }
    )
}
