package com.turismap.app.screens.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import com.turismap.app.theme.TerracotaTextoAdaptable

@Composable
internal fun CabeceraPerfil(
    fotoPortada: String?,
    fotoPerfil: String?,
    paleta: PerfilPaleta,
    onCambiarPortadaClick: () -> Unit,
    onCambiarFotoPerfilClick: () -> Unit,
    onEditarPerfilClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
        // Foto de Portada
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(paleta.verdeEsmeralda)
                .clickable(onClick = onCambiarPortadaClick)
        ) {
            fotoPortada?.takeIf { it.isNotBlank() }?.let { url ->
                RemoteImage(
                    url = url,
                    contentDescription = "Foto de portada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            IconButton(
                onClick = onCambiarPortadaClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = "Cambiar Portada",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Foto de Perfil
        Box(modifier = Modifier.size(130.dp).align(Alignment.BottomCenter)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(4.dp, paleta.fondoApp, CircleShape)
                    .clickable(onClick = onCambiarFotoPerfilClick),
                contentAlignment = Alignment.Center
            ) {
                if (!fotoPerfil.isNullOrBlank()) {
                    RemoteImage(
                        url = fotoPerfil,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .size(40.dp)
                    .background(paleta.terracota, CircleShape)
                    .border(2.dp, paleta.fondoApp, CircleShape)
                    .clickable(onClick = onEditarPerfilClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar Perfil",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
internal fun EncabezadoDatosUsuario(
    nombreMostrar: String,
    correoMostrar: String,
    errorPerfil: String?,
    paleta: PerfilPaleta
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = nombreMostrar,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = paleta.colorTextoPrincipal,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = correoMostrar,
            fontSize = 14.sp,
            color = paleta.colorTextoSecundario,
            modifier = Modifier.padding(top = 4.dp)
        )
        errorPerfil?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
    }
}

@Composable
internal fun SeccionEstadisticasPerfil(
    cantidadFavoritos: String,
    visitas: Int,
    paleta: PerfilPaleta,
    onFavoritosClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ItemEstadistica(
            valor = cantidadFavoritos,
            etiqueta = "Favoritos",
            colorEtiqueta = TerracotaTextoAdaptable,
            colorTexto = paleta.colorTextoPrincipal,
            onClick = onFavoritosClick
        )
        ItemEstadistica(
            valor = visitas.toString(),
            etiqueta = "Visitas",
            colorEtiqueta = paleta.colorTextoPrincipal,
            colorTexto = paleta.colorTextoPrincipal
        )
    }
}

@Composable
internal fun TarjetaTurisMapNegocios(
    paleta: PerfilPaleta,
    onVerPlanesClick: () -> Unit,
    onVerEnWebClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = paleta.verdeEsmeralda)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(shape = RoundedCornerShape(6.dp), color = Color.White.copy(alpha = 0.15f)) {
                Text(
                    text = "TurisMap Negocios",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Promociona tu negocio en la Ruta",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "¿Tienes un hotel, café, restaurante o taller en la CA-8? Llega a más de 45,000 viajeros mensuales en busca de experiencias auténticas.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onVerPlanesClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = paleta.terracota),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Ver planes para mi negocio",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                OutlinedButton(
                    onClick = onVerEnWebClick,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Ver en la web")
                }
            }
        }
    }
}

@Composable
internal fun SeccionConfiguracionPerfil(
    idiomaSeleccionado: String,
    cerrandoSesion: Boolean,
    paleta: PerfilPaleta,
    onIdiomaClick: () -> Unit,
    onPrivacidadClick: () -> Unit,
    onCerrarSesionClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "PERSONALIZACIÓN",
            fontSize = 12.sp,
            color = paleta.colorTextoSecundario,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
        CardOpciones(paleta.fondoTarjeta) {
            ItemMenuOpcion(
                icono = Icons.Default.Public,
                titulo = "Idioma",
                subtitulo = idiomaSeleccionado,
                colorTexto = paleta.colorTextoPrincipal,
                colorIcono = paleta.colorTextoPrincipal,
                onClick = onIdiomaClick
            )
        }

        Text(
            "PRIVACIDAD & SEGURIDAD",
            fontSize = 12.sp,
            color = paleta.colorTextoSecundario,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
        )
        CardOpciones(paleta.fondoTarjeta) {
            ItemMenuOpcion(
                icono = Icons.Default.Security,
                titulo = "Ajustes de Privacidad",
                subtitulo = "Contraseña y visibilidad",
                colorTexto = paleta.colorTextoPrincipal,
                colorIcono = paleta.colorTextoPrincipal,
                onClick = onPrivacidadClick
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onCerrarSesionClick,
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracotaTextoAdaptable),
            border = BorderStroke(2.dp, TerracotaTextoAdaptable),
            shape = RoundedCornerShape(16.dp),
            enabled = !cerrandoSesion
        ) {
            if (cerrandoSesion) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = TerracotaTextoAdaptable)
            } else {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ItemEstadistica(
    valor: String,
    etiqueta: String,
    colorEtiqueta: Color,
    colorTexto: Color,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(
                if (onClick != null) {
                    Modifier.clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(horizontal = 12.dp, vertical = 6.dp)
                } else {
                    Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                }
            )
    ) {
        Text(text = valor, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorTexto)
        Text(text = etiqueta, fontSize = 12.sp, color = colorEtiqueta, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CardOpciones(fondoTarjeta: Color, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(fondoTarjeta, RoundedCornerShape(16.dp))
            .padding(vertical = 4.dp),
        content = content
    )
}

@Composable
private fun ItemMenuOpcion(
    icono: ImageVector,
    titulo: String,
    subtitulo: String = "",
    colorTexto: Color,
    colorIcono: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, fontSize = 16.sp, color = colorTexto, fontWeight = FontWeight.Medium)
            if (subtitulo.isNotEmpty()) {
                Text(text = subtitulo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
