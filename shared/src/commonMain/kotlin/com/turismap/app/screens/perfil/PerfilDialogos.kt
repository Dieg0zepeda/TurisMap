package com.turismap.app.screens.perfil

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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

@Composable
internal fun DialogoPrivacidadPerfil(
    perfilPublico: Boolean,
    compartirUbicacion: Boolean,
    paleta: PerfilPaleta,
    onPerfilPublicoChange: (Boolean) -> Unit,
    onCompartirUbicacionChange: (Boolean) -> Unit,
    onCambiarContrasenaClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = paleta.fondoTarjeta,
        title = { Text("Privacidad", fontWeight = FontWeight.Bold, color = paleta.colorTextoPrincipal) },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Perfil Público", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = paleta.colorTextoPrincipal)
                        Text("Permite que otros vean tus guardados.", fontSize = 12.sp, color = paleta.colorTextoSecundario)
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
                        Text("Ubicación en Reseñas", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = paleta.colorTextoPrincipal)
                        Text("Comparte de dónde es tu reseña.", fontSize = 12.sp, color = paleta.colorTextoSecundario)
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = paleta.colorTextoPrincipal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = paleta.fondoApp, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar Contraseña", color = paleta.fondoApp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = VerdeTextoAdaptable, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
internal fun DialogoEditarPerfil(
    nombreInicial: String,
    telefonoInicial: String,
    paleta: PerfilPaleta,
    onGuardarClick: (nombre: String, telefono: String) -> Unit,
    onDismiss: () -> Unit
) {
    var tempNombre by remember { mutableStateOf(nombreInicial) }
    var tempTelefono by remember { mutableStateOf(telefonoInicial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = paleta.fondoTarjeta,
        title = { Text("Editar Perfil", fontWeight = FontWeight.Bold, color = paleta.colorTextoPrincipal) },
        text = {
            Column {
                OutlinedTextField(
                    value = tempNombre,
                    onValueChange = { tempNombre = it },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(color = paleta.colorTextoPrincipal)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempTelefono,
                    onValueChange = { tempTelefono = it },
                    label = { Text("Teléfono de Contacto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(color = paleta.colorTextoPrincipal)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardarClick(tempNombre, tempTelefono) },
                enabled = tempNombre.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = paleta.verdeEsmeralda)
            ) {
                Text("Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TerracotaTextoAdaptable)
            }
        }
    )
}

@Composable
internal fun DialogoIdiomaPerfil(
    idiomaSeleccionado: String,
    paleta: PerfilPaleta,
    onSeleccionarIdioma: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val opcionesIdioma = listOf("Español", "English", "Français")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = paleta.fondoTarjeta,
        title = { Text("Idioma", fontWeight = FontWeight.Bold, color = paleta.colorTextoPrincipal) },
        text = {
            Column {
                opcionesIdioma.forEach { idioma ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSeleccionarIdioma(idioma) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (idioma == idiomaSeleccionado),
                            onClick = { onSeleccionarIdioma(idioma) },
                            colors = RadioButtonDefaults.colors(selectedColor = VerdeTextoAdaptable)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = idioma, fontSize = 16.sp, color = paleta.colorTextoPrincipal)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = TerracotaTextoAdaptable)
            }
        }
    )
}
