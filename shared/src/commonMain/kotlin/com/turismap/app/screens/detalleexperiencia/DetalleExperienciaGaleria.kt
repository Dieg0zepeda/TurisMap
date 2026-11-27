package com.turismap.app.screens.detalleexperiencia

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turismap.app.components.RemoteImage
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.detalle_exp_cd_foto_galeria
import turismap.shared.generated.resources.detalle_exp_galeria_titulo

@Composable
internal fun GaleriaMiniaturas(
    fotos: List<String>,
    indiceActivo: Int,
    paleta: DetalleExperienciaPaleta,
    onSeleccionarFoto: (Int) -> Unit
) {
    if (fotos.size <= 1) return

    val listState = rememberLazyListState()
    LaunchedEffect(indiceActivo) {
        if (indiceActivo in fotos.indices) {
            listState.animateScrollToItem(indiceActivo)
        }
    }

    Spacer(Modifier.height(18.dp))
    Text(
        text = stringResource(Res.string.detalle_exp_galeria_titulo, fotos.size),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = paleta.textoSecundario
    )
    Spacer(Modifier.height(10.dp))

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(fotos) { index, fotoUrl ->
            val esActiva = index == indiceActivo
            val forma = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .width(128.dp)
                    .height(92.dp)
                    .clip(forma)
                    .border(
                        width = if (esActiva) 2.dp else 1.dp,
                        color = if (esActiva) paleta.primario else paleta.borde,
                        shape = forma
                    )
                    .clickable { onSeleccionarFoto(index) }
            ) {
                RemoteImage(
                    url = fotoUrl,
                    contentDescription = stringResource(
                        Res.string.detalle_exp_cd_foto_galeria,
                        index + 1,
                        fotos.size
                    ),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (esActiva) 1f else 0.78f)
                )
            }
        }
    }
}
