package com.turismap.app.screens.perfil

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.turismap.app.theme.TerracotaTextoAdaptable
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_cancelar
import turismap.shared.generated.resources.btn_guardar
import turismap.shared.generated.resources.perfil_editar_error_nombre_corto
import turismap.shared.generated.resources.perfil_editar_guardando
import turismap.shared.generated.resources.perfil_editar_nombre_label
import turismap.shared.generated.resources.perfil_editar_telefono_label
import turismap.shared.generated.resources.perfil_editar_titulo

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
