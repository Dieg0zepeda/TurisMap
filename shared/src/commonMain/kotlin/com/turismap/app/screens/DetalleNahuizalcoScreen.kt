package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.data.repository.LugaresRepository
import com.turismap.app.data.repository.MunicipioBD
import com.turismap.app.theme.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.cascada
import turismap.shared.generated.resources.iglesia
import turismap.shared.generated.resources.mercado
import turismap.shared.generated.resources.talleres
import turismap.shared.generated.resources.titulo_nahuizalco

@Composable
fun DetalleNahuizalcoScreen(
    onBackClick: () -> Unit = {}
) {
    // 1. Variables de estado y repositorio
    var municipioBD by remember { mutableStateOf<MunicipioBD?>(null) }
    val repositorio = LugaresRepository

    // 2. Efecto para cargar los datos de "nahuizalco"
    LaunchedEffect(Unit) {
        municipioBD = repositorio.obtenerMunicipioPorId("nahuizalco")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisFondo)
            .verticalScroll(rememberScrollState())
    ) {
        // --- 1. CABECERA ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFF8BA896))
        ) {
            Image(
                painter = painterResource(Res.drawable.titulo_nahuizalco),
                contentDescription = "Nahuizalco",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape, color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(40.dp).clickable { onBackClick() }
                ) { Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", modifier = Modifier.padding(8.dp), tint = Color(0xFF1A202C)) }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", modifier = Modifier.padding(10.dp), tint = Color(0xFF1A202C))
                    }
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Guardar", modifier = Modifier.padding(10.dp), tint = Color(0xFF1A202C))
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = VerdePrimario, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ruta de las Flores · CA-8", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VerdePrimario)
                }
            }
        }

        // --- 2. TARJETA PRINCIPAL DE INFORMACIÓN ---
        Column(modifier = Modifier.offset(y = (-20).dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sonsonate · El Salvador", fontSize = 12.sp, color = GrisTextoSecundario)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // TÍTULO DESDE FIREBASE
                    Text(municipioBD?.nombre ?: "Cargando...", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A202C))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Capital del mercado nocturno y la cultura viva", fontSize = 14.sp, color = GrisTextoSecundario)

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            // ELEVACIÓN DESDE FIREBASE
                            Text(municipioBD?.elevacion ?: "...", fontSize = 13.sp, color = Color(0xFF1A202C), fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            // CLIMA DESDE FIREBASE
                            Text(municipioBD?.clima ?: "...", fontSize = 13.sp, color = Color(0xFF1A202C), fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = TerracotaAcento, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            // CALIFICACIÓN DESDE FIREBASE
                            Text(municipioBD?.calificacion?.toString() ?: "0.0", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Historia & Esencia", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
                    Spacer(modifier = Modifier.height(12.dp))
                    // HISTORIA DESDE FIREBASE
                    Text(
                        text = municipioBD?.historia ?: "Cargando historia...",
                        fontSize = 14.sp, lineHeight = 22.sp, color = GrisTextoSecundario
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(shape = RoundedCornerShape(12.dp), color = TerracotaAcento.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NightlightRound, contentDescription = null, tint = TerracotaAcento)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("MOMENTO IDEAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TerracotaAcento)
                                // TEMPORADA/MOMENTO DESDE FIREBASE
                                Text(municipioBD?.mejorTemporadaDesc ?: "Cargando...", fontSize = 13.sp, color = Color(0xFF1A202C))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Atractivos Imperdibles", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
                        Text("4 Destacados", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VerdePrimario)
                    }
                    Text("Lo que no te puedes perder en tu recorrido", fontSize = 13.sp, color = GrisTextoSecundario)
                }
            }

            // --- 3. CARRUSEL DE ATRACTIVOS ---
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.offset(y = (-20).dp)
            ) {
                item {
                    AtractivoCardNahuizalco(
                        etiqueta = "Gastronomía", etiquetaColor = TerracotaAcento,
                        titulo = "Mercado Nocturno", descripcion = "Disfruta de platillos típicos en un ambiente mágico iluminado por luces tradicionales.",
                        accion = "Ver platillos →",
                        imagenRes = Res.drawable.mercado
                    )
                }
                item {
                    AtractivoCardNahuizalco(
                        etiqueta = "Tradición Viva", etiquetaColor = VerdePrimario,
                        titulo = "Talleres de Mimbre", descripcion = "Observa a los maestros artesanos tejiendo maravillas en tule, madera y mimbre.",
                        accion = "Ver talleres →",
                        imagenRes = Res.drawable.talleres
                    )
                }
                item {
                    AtractivoCardNahuizalco(
                        etiqueta = "Arquitectura", etiquetaColor = TerracotaAcento,
                        titulo = "Iglesia San Juan", descripcion = "Patrimonio histórico que resguarda la fe y la cultura de este pueblo de raíces pipiles.",
                        accion = "Conocer más →",
                        imagenRes = Res.drawable.iglesia
                    )
                }
                item {
                    AtractivoCardNahuizalco(
                        etiqueta = "Naturaleza", etiquetaColor = VerdePrimario,
                        titulo = "La Golondrinera", descripcion = "Espectacular cascada escondida entre la vegetación de la zona, ideal para senderismo.",
                        accion = "Cómo llegar →",
                        imagenRes = Res.drawable.cascada
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 4. DIRECTORIO COMUNITARIO ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = VerdePrimario)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("DIRECTORIO COMUNITARIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f), letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("¿Planeas tu visita a Nahuizalco?", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Encuentra las mejores artesanías de tule, comedores típicos y guías turísticos.", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f), lineHeight = 18.sp)

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        DirectorioIconoNahuizalco(icono = Icons.Default.ShoppingBag, texto = "18 Tiendas de\nArtesanías")
                        DirectorioIconoNahuizalco(icono = Icons.Default.Restaurant, texto = "20 Puestos\nGastronómicos")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        DirectorioIconoNahuizalco(icono = Icons.Default.DirectionsWalk, texto = "8 Guías\nLocales")
                        DirectorioIconoNahuizalco(icono = Icons.Default.LocalCafe, texto = "5 Cafés\nTradicionales")
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { }, modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracotaAcento), shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Ver Guía de Comercio y Ocio →", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 5. UBICACIÓN Y MAPA ---
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Ubicación y Entorno", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
                    Text("Depto. de Sonsonate", fontSize = 12.sp, color = GrisTextoSecundario)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E8F0))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {

                        Surface(modifier = Modifier.fillMaxWidth().padding(12.dp), shape = RoundedCornerShape(12.dp), color = Color.White) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TerracotaAcento, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ruta de las Flores · CA-8", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A202C))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { }, modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, VerdePrimario), colors = ButtonDefaults.outlinedButtonColors(contentColor = VerdePrimario)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(9.dp))
                        Text("Ver en Mapa", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { }, modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = TerracotaAcento)
                    ) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(9.dp))
                        Text("Guardar Viaje", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// Componentes privados para evitar conflictos con los otros archivos
@Composable
private fun AtractivoCardNahuizalco(etiqueta: String, etiquetaColor: Color, titulo: String, descripcion: String, accion: String, imagenRes: DrawableResource) {
    Card(
        modifier = Modifier.width(200.dp).height(240.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp).background(Color(0xFFE2E8F0))) {
                Image(
                    painter = painterResource(imagenRes),
                    contentDescription = titulo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(10.dp).align(Alignment.TopEnd)
                ) {
                    Text(etiqueta, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = etiquetaColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Text(titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
                Spacer(modifier = Modifier.height(4.dp))
                Text(descripcion, fontSize = 12.sp, color = GrisTextoSecundario, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.weight(1f))
                Text(accion, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerracotaAcento)
            }
        }
    }
}

@Composable
private fun DirectorioIconoNahuizalco(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(130.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
            Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(texto, fontSize = 12.sp, color = Color.White, lineHeight = 16.sp)
    }
}