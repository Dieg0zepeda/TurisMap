package com.turismap.app.screens.mapa

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModalCapasMapa(
    filtroTipoMarcadores: String,
    cantidadLugares: Int,
    cantidadComercios: Int,
    paleta: MapaPaleta,
    onSeleccionarFiltro: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val totalPuntos = cantidadLugares + cantidadComercios

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (paleta.esOscuro) Color(0xFF0F172A) else Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = paleta.airbnbBorde) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(if (paleta.esOscuro) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                        .border(2.dp, paleta.turismapAcento, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    RemoteImage(
                        url = "https://i.pinimg.com/1200x/8b/44/31/8b44315342377795f91d4d37b8493635.jpg",
                        contentDescription = "Mascota Café TurisMap",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "¡Hola, Viajero!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = paleta.airbnbOscuro
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = paleta.turismapPrimario.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, paleta.turismapPrimario.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "Ruta de las Flores · GPS",
                            color = paleta.turismapPrimario,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = paleta.airbnbBorde, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "CAPAS Y PUNTOS EN EL MAPA",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = paleta.airbnbGris,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            ItemFiltroWaze(
                icono = "🧭",
                titulo = "Todos los puntos",
                subtitulo = "Pueblos y comercios registrados",
                cantidad = totalPuntos,
                seleccionado = filtroTipoMarcadores == "Todos",
                paleta = paleta,
                onClick = { onSeleccionarFiltro("Todos") }
            )

            ItemFiltroWaze(
                icono = "📍",
                titulo = "Pueblos y Atractivos",
                subtitulo = "Ataco, Apaneca, Juayúa y miradores",
                cantidad = cantidadLugares,
                seleccionado = filtroTipoMarcadores == "Destinos",
                paleta = paleta,
                onClick = { onSeleccionarFiltro("Destinos") }
            )

            ItemFiltroWaze(
                icono = "🏪",
                titulo = "Comercios y Servicios",
                subtitulo = "Restaurantes, hoteles, cafés y tiendas",
                cantidad = cantidadComercios,
                seleccionado = filtroTipoMarcadores == "Comercios",
                paleta = paleta,
                onClick = { onSeleccionarFiltro("Comercios") }
            )
        }
    }
}

@Composable
private fun ItemFiltroWaze(
    icono: String,
    titulo: String,
    subtitulo: String,
    cantidad: Int,
    seleccionado: Boolean,
    paleta: MapaPaleta,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (seleccionado) paleta.turismapPrimario.copy(alpha = 0.12f) else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (seleccionado) paleta.turismapPrimario else paleta.airbnbBorde
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icono, fontSize = 22.sp)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (seleccionado) paleta.turismapPrimario else paleta.airbnbOscuro
                )
                Text(
                    text = subtitulo,
                    fontSize = 12.sp,
                    color = paleta.airbnbGris
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (seleccionado) paleta.turismapPrimario else paleta.airbnbFondo
            ) {
                Text(
                    text = cantidad.toString(),
                    color = if (seleccionado) Color.White else paleta.airbnbOscuro,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
