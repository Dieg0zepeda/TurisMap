package com.turismap.app.screens.detalleexperiencia

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.data.repository.Resena

@Composable
internal fun SeccionResenasExperiencia(
    resenas: List<Resena>,
    tieneUsuarioAutenticado: Boolean,
    esPropietario: Boolean,
    calificacion: Int,
    comentario: String,
    enviando: Boolean,
    mensaje: String?,
    paleta: DetalleExperienciaPaleta,
    onCalificacionChange: (Int) -> Unit,
    onComentarioChange: (String) -> Unit,
    onPublicarResena: () -> Unit
) {
    Spacer(Modifier.height(28.dp))
    Text(
        "Reseñas de Visitantes",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = paleta.colorTextoPrincipal
    )
    Spacer(Modifier.height(6.dp))

    if (resenas.isEmpty()) {
        Text(
            "Aún no hay comentarios para este lugar. ¡Sé el primero en compartir tu experiencia!",
            color = paleta.colorTextoSecundario,
            fontSize = 13.sp
        )
    } else {
        resenas.forEach { r ->
            Card(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (paleta.esOscuro) {
                        Color(0xFF1E293B).copy(alpha = 0.6f)
                    } else {
                        Color(0xFFF1F5F9)
                    }
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, paleta.colorBordeSuave)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("★".repeat(r.calificacion), color = paleta.verdeTurisMap, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        r.comentario.ifBlank { "Excelente lugar recomendado." },
                        color = paleta.colorTextoPrincipal,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(20.dp))
    Text(
        if (!tieneUsuarioAutenticado) "Inicia sesión para reseñar"
        else if (esPropietario) "No puedes reseñar tu propio comercio"
        else "Escribe tu reseña",
        fontWeight = FontWeight.Bold,
        color = paleta.colorTextoPrincipal,
        fontSize = 14.sp
    )

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..5).forEach { valor ->
            TextButton(
                onClick = { onCalificacionChange(valor) },
                enabled = tieneUsuarioAutenticado && !esPropietario
            ) {
                Text(
                    if (valor <= calificacion) "★" else "☆",
                    color = paleta.verdeTurisMap,
                    fontSize = 24.sp
                )
            }
        }
    }

    OutlinedTextField(
        value = comentario,
        onValueChange = onComentarioChange,
        label = { Text("Comentario (opcional)") },
        modifier = Modifier.fillMaxWidth(),
        enabled = tieneUsuarioAutenticado && !esPropietario,
        minLines = 2,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = paleta.colorTextoPrincipal,
            unfocusedTextColor = paleta.colorTextoPrincipal,
            focusedContainerColor = paleta.colorFondoInput,
            unfocusedContainerColor = paleta.colorFondoInput,
            focusedBorderColor = paleta.verdeTurisMap,
            unfocusedBorderColor = paleta.colorBordeSuave
        )
    )

    mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

    Button(
        onClick = onPublicarResena,
        enabled = tieneUsuarioAutenticado && !esPropietario && !enviando,
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = paleta.verdeTurisMap)
    ) {
        if (enviando) {
            CircularProgressIndicator(Modifier.size(18.dp), color = Color.White)
        } else {
            Text("Publicar reseña", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun BarraInferiorExperiencia(
    datosUi: DetalleExperienciaDatosUi,
    paleta: DetalleExperienciaPaleta,
    onLlamar: () -> Unit,
    onAbrirWhatsapp: () -> Unit,
    onIniciarRuta: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier.fillMaxWidth(),
        color = paleta.colorBarraInferior,
        shadowElevation = 16.dp,
        border = BorderStroke(1.dp, paleta.colorBordeSuave)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    if (datosUi.esPremium) "Socio Premium" else "por consumo",
                    fontSize = 11.sp,
                    color = paleta.colorTextoSecundario
                )
                Text(
                    "Desde ${datosUi.precioTexto} USD",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = paleta.colorTextoPrincipal
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (datosUi.comercio.telefono.isNotBlank()) {
                    IconButton(
                        onClick = onLlamar,
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (paleta.esOscuro) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                RoundedCornerShape(10.dp)
                            )
                            .border(1.dp, paleta.colorBordeSuave, RoundedCornerShape(10.dp))
                    ) {
                        Text("📞", fontSize = 16.sp)
                    }
                }

                if ((datosUi.esSocio || datosUi.esPremium) && datosUi.telefonoLimpio.isNotBlank()) {
                    IconButton(
                        onClick = onAbrirWhatsapp,
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                paleta.verdeTurisMap.copy(alpha = 0.15f),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                paleta.verdeTurisMap.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            )
                    ) {
                        Text("💬", fontSize = 16.sp)
                    }
                }

                Button(
                    onClick = onIniciarRuta,
                    colors = ButtonDefaults.buttonColors(containerColor = paleta.verdeTurisMap),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        "Iniciar ruta",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
