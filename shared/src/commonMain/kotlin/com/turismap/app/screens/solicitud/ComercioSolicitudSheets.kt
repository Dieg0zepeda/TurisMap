package com.turismap.app.screens.solicitud

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.FondoBlancoAdaptable
import com.turismap.app.theme.GrisBorde
import com.turismap.app.theme.GrisTextoSecundario
import com.turismap.app.theme.TextoPrincipalOscuro
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_cerrar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> SelectorModalSheet(
    titulo: String,
    iconoOpcion: ImageVector,
    opciones: List<T>,
    opcionSeleccionada: (T) -> Boolean,
    etiquetaOpcion: (T) -> String,
    claveOpcion: (T) -> Any,
    onDismiss: () -> Unit,
    onSeleccionar: (T) -> Unit
) {
    val esModoOscuro = isSystemInDarkTheme()
    val colorAcento = if (esModoOscuro) Color(0xFF6FCF97) else Color(0xFF1B4934)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FondoBlancoAdaptable,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = GrisBorde) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = iconoOpcion,
                        contentDescription = null,
                        tint = colorAcento,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = titulo,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoPrincipalOscuro,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.btn_cerrar),
                        tint = GrisTextoSecundario
                    )
                }
            }

            HorizontalDivider(color = GrisBorde, thickness = 1.dp)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(items = opciones, key = claveOpcion) { opcion ->
                    val estaSeleccionada = opcionSeleccionada(opcion)
                    val fondoFila = if (estaSeleccionada) {
                        colorAcento.copy(alpha = if (esModoOscuro) 0.16f else 0.10f)
                    } else {
                        Color.Transparent
                    }
                    val bordeFila = if (estaSeleccionada) {
                        BorderStroke(1.dp, colorAcento.copy(alpha = if (esModoOscuro) 0.45f else 0.30f))
                    } else {
                        null
                    }

                    Surface(
                        onClick = { onSeleccionar(opcion) },
                        shape = RoundedCornerShape(14.dp),
                        color = fondoFila,
                        border = bordeFila,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 54.dp)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = iconoOpcion,
                                contentDescription = null,
                                tint = if (estaSeleccionada) colorAcento else GrisTextoSecundario,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = etiquetaOpcion(opcion),
                                fontSize = 15.sp,
                                fontWeight = if (estaSeleccionada) FontWeight.SemiBold else FontWeight.Medium,
                                color = TextoPrincipalOscuro,
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (estaSeleccionada) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colorAcento,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
