package com.turismap.app.screens.detalleexperiencia

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun MenuDigitalDialog(
    nombreComercio: String,
    paleta: DetalleExperienciaPaleta,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("📖", fontSize = 22.sp)
                Text(
                    "Menú Digital y Catálogo",
                    fontWeight = FontWeight.Bold,
                    color = paleta.verdeTurisMap
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Establecimiento: $nombreComercio",
                    fontWeight = FontWeight.Bold,
                    color = paleta.colorTextoPrincipal
                )
                Text(
                    "Como beneficio de Socio Premium, este comercio cuenta con carta digitalizada en alta resolución y catálogo de productos.",
                    color = paleta.colorTextoSecundario,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = paleta.verdeTurisMap)
            ) {
                Text("Cerrar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = if (paleta.esOscuro) Color(0xFF1E1B18) else Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
