package com.turismap.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.*

// 1. Modelo de datos
data class ItemGuia(
    val id: String,
    val titulo: String,
    val subtitulo: String,
    val categoriaTag: String,
    val calificacion: String,
    val precio: String?,
    val textoBoton: String?,
    val descripcion: String,
    val horario: String = "Lunes a Domingo · 8:00 AM - 8:00 PM",
    val telefono: String = "+503 2222-3333",
    val caracteristicas: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuiaScreen() {
    var categoriaFiltro by remember { mutableStateOf("Todos") }
    var searchQuery by remember { mutableStateOf("") }
    var itemSeleccionado by remember { mutableStateOf<ItemGuia?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val categorias = listOf("Todos", "Restaurantes", "Hoteles", "Cafés", "Artesanías")

    val lugaresAtaco = remember {
        listOf(
            ItemGuia("at1", "Hotel El Carmen", "Ataco · A 3 min del parque", "Hoteles", "4.9 (280)", "$75 / noche", "Reservar", "Antigua casa patrimonial convertida en hotel boutique. Habitaciones con vista al jardín y café de cortesía.", caracteristicas = listOf("WiFi Gratis", "Parqueo Privado", "Desayuno Incluido", "Piscina")),
            ItemGuia("at2", "D'Rústico Restaurante", "Famoso por postres y moka", "Restaurantes", "4.8 (195)", "$$ · Tradicional", null, "El mejor rincón de Ataco para degustar parrilladas al estilo argentino y postres artesanales.", caracteristicas = listOf("Pet Friendly", "Música en Vivo", "Terraza al Aire Libre", "Bar")),
            ItemGuia("at3", "Café Tayúa", "Café de estricta altura", "Cafés", "4.7 (120)", "$ · Gourmet", "Ver menú", "Especialistas en métodos de extracción de café y repostería local.", caracteristicas = listOf("Opciones Veganas", "WiFi de Alta Velocidad", "Pet Friendly")),
            ItemGuia("at4", "Telares de Ataco", "Ropa y decoración tejida", "Artesanías", "4.9 (90)", "$$ · Local", "Ver tienda", "Tradición viva en telares de pedal. Encuentra bufandas, mantas y decoración única.", caracteristicas = listOf("Hecho a Mano", "Envíos Internacionales", "Aceptan Tarjeta"))
        )
    }

    val lugaresApaneca = remember {
        listOf(
            ItemGuia("ap1", "Finca Café Albania", "Laberinto y tirolesa", "Cafés & Aventura", "4.9 (420)", "$5 / entrada", "Explorar", "Resuelve el laberinto más grande de Centroamérica y prueba el famoso café de altura.", caracteristicas = listOf("Parqueo Amplio", "Juegos Infantiles", "Restaurante", "Guías Locales")),
            ItemGuia("ap2", "Hotel Santa Leticia", "Cabañas en el bosque", "Hoteles", "4.8 (310)", "$85 / noche", "Reservar", "Alojamiento rodeado de naturaleza con un sitio arqueológico dentro de sus instalaciones.", caracteristicas = listOf("Sitio Arqueológico", "Piscina Climatizada", "Restaurante", "Eventos")),
            ItemGuia("ap3", "La Cocina de la Abuela", "Sopas y comida típica", "Restaurantes", "4.6 (150)", "$$ · Típica", null, "Sopas de gallina india y comida a la leña para combatir el frío de Apaneca.", caracteristicas = listOf("Comida a la Leña", "Ambiente Familiar", "Económico"))
        )
    }

    val lugaresJuayua = remember {
        listOf(
            ItemGuia("ju1", "Festival Gastronómico", "Fines de semana en la plaza", "Restaurantes", "4.9 (1200)", "$$ · Variado", "Ver platillos", "Evento icónico con decenas de toldos ofreciendo desde carnes exóticas hasta antojitos salvadoreños.", caracteristicas = listOf("Música en Vivo", "Al Aire Libre", "Variedad Gastronómica", "Familiar")),
            ItemGuia("ju2", "Hotel Juayúa", "Vistas al volcán", "Hoteles", "4.7 (240)", "$60 / noche", "Reservar", "Cómodo hotel con piscina y terraza panorámica, perfecto para descansar después de ir a las cascadas.", caracteristicas = listOf("Vista al Volcán", "Piscina Infinita", "Bar", "Parqueo")),
            ItemGuia("ju3", "Café La Escalera", "Arte y café", "Cafés", "4.8 (85)", "$ · Relajado", null, "Pequeña cafetería bohemia con exposiciones de arte local y excelente café de la región.", caracteristicas = listOf("Galería de Arte", "Ambiente Bohemio", "Música Acústica"))
        )
    }

    val lugaresSalcoatitan = remember {
        listOf(
            ItemGuia("sa1", "Plaza de la Yuca", "La mejor yuca frita", "Restaurantes", "4.9 (650)", "$ · Típico", "Ver locales", "Conjunto de pequeños restaurantes famosos por servir la yuca frita o sancochada más auténtica en hoja de huerta.", caracteristicas = listOf("Comida Típica", "Al Aire Libre", "Económico")),
            ItemGuia("sa2", "Café Quetzalcóatl", "Frente a la ceiba", "Cafés", "4.6 (110)", "$$ · Postres", "Ver menú", "Disfruta de un postre de maíz mientras observas la majestuosa ceiba centenaria del parque.", caracteristicas = listOf("Vista a la Plaza", "Postres de Maíz", "WiFi")),
            ItemGuia("sa3", "Galería de Mosaicos", "Arte en piedra y vidrio", "Artesanías", "4.8 (75)", "$$ · Arte", "Explorar", "Taller especializado en la creación de coloridos murales y recuerdos hechos en mosaico.", caracteristicas = listOf("Talleres Interactivos", "Artesanías Únicas", "Aceptan Tarjeta"))
        )
    }

    val lugaresNahuizalco = remember {
        listOf(
            ItemGuia("na1", "Mercado Nocturno", "Comida típica bajo las estrellas", "Restaurantes", "4.8 (800)", "$ · Típico", "Ver guía", "Experiencia única donde la plaza se ilumina con luces tenues y se venden platillos tradicionales.", caracteristicas = listOf("Horario Nocturno", "Iluminación Tradicional", "Seguridad Local")),
            ItemGuia("na2", "Taller de Mimbre La Pipil", "Muebles y decoración", "Artesanías", "4.9 (320)", "$$ · Mimbre", "Contactar", "Observa a los artesanos tejiendo maravillas en tule, madera y mimbre. Compra directa a los creadores.", caracteristicas = listOf("Demostraciones en Vivo", "Muebles a Medida", "Hecho a Mano")),
            ItemGuia("na3", "Café de la Plaza", "Pan dulce y café de olla", "Cafés", "4.5 (95)", "$ · Casero", null, "Ideal para sentarse a tomar un café caliente con pan dulce tradicional mientras se observa la actividad del pueblo.", caracteristicas = listOf("Café de Olla", "Pan Recién Horneado", "Ambiente Clásico"))
        )
    }

    fun filtrarLista(lista: List<ItemGuia>): List<ItemGuia> {
        return lista.filter { item ->
            val coincideCategoria = if (categoriaFiltro == "Todos") true else item.categoriaTag.contains(categoriaFiltro, ignoreCase = true)
            val coincideBusqueda = if (searchQuery.isBlank()) true else {
                item.titulo.contains(searchQuery, ignoreCase = true) || item.subtitulo.contains(searchQuery, ignoreCase = true)
            }
            coincideCategoria && coincideBusqueda
        }
    }

    val atacoFiltrado = filtrarLista(lugaresAtaco)
    val apanecaFiltrado = filtrarLista(lugaresApaneca)
    val juayuaFiltrado = filtrarLista(lugaresJuayua)
    val salcoatitanFiltrado = filtrarLista(lugaresSalcoatitan)
    val nahuizalcoFiltrado = filtrarLista(lugaresNahuizalco)

    if (itemSeleccionado != null) {
        ModalBottomSheet(
            onDismissRequest = { itemSeleccionado = null },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            DetalleLugarBottomSheet(item = itemSeleccionado!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(GrisFondo)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("TURISMAP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VerdePrimario, letterSpacing = 1.2.sp)
                Text("Guía Comercial", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
            }
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.White, border = BorderStroke(1.dp, GrisBorde)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, contentDescription = null, tint = VerdePrimario, modifier = Modifier.size(20.dp)) }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp).shadow(elevation = 4.dp, shape = RoundedCornerShape(14.dp)),
            color = Color.White, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, GrisBorde)
        ) {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(fontSize = 14.sp, color = Color(0xFF1A202C)),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text("Buscar hoteles, cafés, artesanías...", fontSize = 14.sp, color = GrisTextoSecundario)
                        }
                        innerTextField()
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categorias) { categoria ->
                val isSelected = categoria == categoriaFiltro
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(50)).clickable { categoriaFiltro = categoria },
                    color = if (isSelected) VerdePrimario else Color.White, shape = RoundedCornerShape(50),
                    border = if (!isSelected) BorderStroke(1.dp, GrisBorde) else null, shadowElevation = if (isSelected) 3.dp else 1.dp
                ) {
                    Text(
                        text = categoria, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF2D3748), modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        if (atacoFiltrado.isEmpty() && apanecaFiltrado.isEmpty() && juayuaFiltrado.isEmpty() && salcoatitanFiltrado.isEmpty() && nahuizalcoFiltrado.isEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.SearchOff, contentDescription = null, tint = GrisTextoSecundario, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No se encontraron lugares", color = GrisTextoSecundario, fontWeight = FontWeight.Bold)
            }
        }

        if (atacoFiltrado.isNotEmpty()) {
            SeccionGuiaEncabezado("Concepción de Ataco", "Pueblo de murales, telares y café gourmet")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(atacoFiltrado) { item -> TarjetaGuiaItem(item = item, onClick = { itemSeleccionado = item }) }
            }
            Spacer(modifier = Modifier.height(26.dp))
        }

        if (apanecaFiltrado.isNotEmpty()) {
            SeccionGuiaEncabezado("Apaneca", "Aventura extrema y clima de montaña")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(apanecaFiltrado) { item -> TarjetaGuiaItem(item = item, onClick = { itemSeleccionado = item }) }
            }
            Spacer(modifier = Modifier.height(26.dp))
        }

        if (juayuaFiltrado.isNotEmpty()) {
            SeccionGuiaEncabezado("Juayúa", "Capital de la gastronomía y las cascadas")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(juayuaFiltrado) { item -> TarjetaGuiaItem(item = item, onClick = { itemSeleccionado = item }) }
            }
            Spacer(modifier = Modifier.height(26.dp))
        }

        if (salcoatitanFiltrado.isNotEmpty()) {
            SeccionGuiaEncabezado("Salcoatitán", "Arte en mosaico y la mejor yuca frita")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(salcoatitanFiltrado) { item -> TarjetaGuiaItem(item = item, onClick = { itemSeleccionado = item }) }
            }
            Spacer(modifier = Modifier.height(26.dp))
        }

        if (nahuizalcoFiltrado.isNotEmpty()) {
            SeccionGuiaEncabezado("Nahuizalco", "Cultura pipil, mimbre y mercado nocturno")
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(nahuizalcoFiltrado) { item -> TarjetaGuiaItem(item = item, onClick = { itemSeleccionado = item }) }
            }
            Spacer(modifier = Modifier.height(26.dp))
        }
    }
}

@Composable
private fun SeccionGuiaEncabezado(titulo: String, subtitulo: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitulo, fontSize = 12.sp, color = GrisTextoSecundario)
        }
    }
}

@Composable
private fun TarjetaGuiaItem(item: ItemGuia, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(220.dp).shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp)).clickable { onClick() },
        shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(VerdePrimario.copy(alpha = 0.85f)), contentAlignment = Alignment.TopEnd) {
                Surface(modifier = Modifier.align(Alignment.BottomStart).padding(10.dp), shape = RoundedCornerShape(6.dp), color = Color.Black.copy(alpha = 0.6f)) {
                    Text(item.categoriaTag, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                }
                Surface(modifier = Modifier.padding(8.dp).size(30.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.9f)) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = TerracotaAcento, modifier = Modifier.size(16.dp)) }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(item.titulo, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C), maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Text(item.subtitulo, fontSize = 11.sp, color = GrisTextoSecundario, maxLines = 1)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = TerracotaAcento, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(item.calificacion, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2D3748))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    item.precio?.let { Text(it, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C)) }
                    item.textoBoton?.let { botonTexto ->
                        Button(
                            onClick = { onClick() },
                            shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = TerracotaAcento),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), modifier = Modifier.height(30.dp)
                        ) {
                            Text(botonTexto, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleLugarBottomSheet(item: ItemGuia) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 32.dp, top = 8.dp)) {
        Text(text = item.titulo, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A202C))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = item.subtitulo, fontSize = 14.sp, color = GrisTextoSecundario)

        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(8.dp), color = VerdePrimario.copy(alpha = 0.1f)) {
                Text(text = item.categoriaTag, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VerdePrimario, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = TerracotaAcento, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(item.calificacion, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (item.caracteristicas.isNotEmpty()) {
            Text("Servicios y Destacados", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(item.caracteristicas) { caracteristica ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TerracotaAcento.copy(alpha = 0.08f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TerracotaAcento, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = caracteristica, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TerracotaAcento)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text("Acerca de", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A202C))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = item.descripcion, fontSize = 14.sp, color = GrisTextoSecundario, lineHeight = 20.sp)

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = GrisBorde)
        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = VerdePrimario, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = item.horario, fontSize = 14.sp, color = Color(0xFF2D3748))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Phone, contentDescription = null, tint = VerdePrimario, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = item.telefono, fontSize = 14.sp, color = Color(0xFF2D3748), fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { }, modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TerracotaAcento), shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Ver ubicación en el Mapa", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}