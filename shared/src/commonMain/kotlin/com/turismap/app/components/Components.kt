package com.turismap.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.theme.GrisBorde
import com.turismap.app.theme.GrisTextoSecundario
import com.turismap.app.theme.SuperficieBarraInferiorAdaptable
import com.turismap.app.theme.TextoInactivoBarraOscuro
import com.turismap.app.theme.VerdeBosqueMarca
import com.turismap.app.theme.VerdeSalviaOscuro
import com.turismap.app.theme.VerdeTextoAdaptable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.nav_tab_explorar
import turismap.shared.generated.resources.nav_tab_guia
import turismap.shared.generated.resources.nav_tab_mapa
import turismap.shared.generated.resources.nav_tab_perfil

@Composable
fun FigmaBottomBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    data class NavTab(val labelRes: StringResource, val icon: ImageVector)
    val tabs = listOf(
        NavTab(Res.string.nav_tab_explorar, Icons.Default.Home),
        NavTab(Res.string.nav_tab_mapa, Icons.Default.Place),
        NavTab(Res.string.nav_tab_guia, Icons.Default.DateRange),
        NavTab(Res.string.nav_tab_perfil, Icons.Default.Person)
    )

    val esModoOscuro = isSystemInDarkTheme()
    val colorPildoraActiva = if (esModoOscuro) {
        VerdeSalviaOscuro.copy(alpha = 0.18f)
    } else {
        VerdeBosqueMarca.copy(alpha = 0.12f)
    }
    val colorActivo = VerdeTextoAdaptable
    val colorInactivo = if (esModoOscuro) TextoInactivoBarraOscuro else GrisTextoSecundario

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        color = SuperficieBarraInferiorAdaptable
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(
                thickness = 1.dp,
                color = GrisBorde
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedIndex == index
                    val label = stringResource(tab.labelRes)
                    val colorContenido = if (isSelected) colorActivo else colorInactivo

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onTabSelected(index) }
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(30.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) colorPildoraActiva else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = label,
                                tint = colorContenido,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = colorContenido,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
