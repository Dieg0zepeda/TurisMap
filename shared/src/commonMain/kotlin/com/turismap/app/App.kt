package com.turismap.app

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App() {
    MaterialTheme {
        Scaffold(
            topBar = { TurisMapTopBar() },
            bottomBar = { SmoothNavBar() }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    FeaturedDestinationCard()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurisMapTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(text = "Ruta de las Flores", style = MaterialTheme.typography.titleMedium)
        },
        navigationIcon = {
            IconButton(onClick = { /* Acción del menú */ }) {
                Text("☰") // Menú hamburguesa temporal
            }
        },
        actions = {
            IconButton(onClick = { /* Acción de búsqueda */ }) {
                Text("🔍") // Lupa temporal
            }
        }
    )
}

@Composable
fun FeaturedDestinationCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                            startY = 200f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Surface(
                    color = Color(0xFFB75C4C), // Color terracota
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Featured Destination",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Discover Ataco",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Immerse yourself in vibrant murals, artisanal coffee, and cobblestone charm.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SmoothNavBar() {
    var selectedIndex by remember { mutableStateOf(0) }

    // Emojis temporales en lugar de ImageVector para evitar el error de Icons
    val items = listOf("🧭", "🗺️", "🍽️", "👤")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, iconText ->
            NavItem(
                iconText = iconText,
                isSelected = selectedIndex == index,
                onClick = { selectedIndex = index }
            )
        }
    }
}

@Composable
fun NavItem(iconText: String, isSelected: Boolean, onClick: () -> Unit) {
    // Animación de salto hacia arriba
    val yOffset by animateDpAsState(
        targetValue = if (isSelected) (-20).dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "yOffset"
    )

    Box(
        modifier = Modifier
            .offset(y = yOffset)
            .size(50.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color(0xFF4A148C) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // En lugar de un Icon, usamos un Text para el emoji
        Text(
            text = iconText,
            fontSize = 24.sp
        )
    }
}