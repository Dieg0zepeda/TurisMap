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
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_cerrar
import turismap.shared.generated.resources.detalle_exp_menu_descripcion
import turismap.shared.generated.resources.detalle_exp_menu_establecimiento
import turismap.shared.generated.resources.detalle_exp_menu_titulo

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
                    stringResource(Res.string.detalle_exp_menu_titulo),
                    fontWeight = FontWeight.Bold,
                    color = paleta.verdeTurisMap
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(Res.string.detalle_exp_menu_establecimiento, nombreComercio),
                    fontWeight = FontWeight.Bold,
                    color = paleta.colorTextoPrincipal
                )
                Text(
                    stringResource(Res.string.detalle_exp_menu_descripcion),
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
                Text(stringResource(Res.string.btn_cerrar), color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = if (paleta.esOscuro) Color(0xFF1E1B18) else Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}
