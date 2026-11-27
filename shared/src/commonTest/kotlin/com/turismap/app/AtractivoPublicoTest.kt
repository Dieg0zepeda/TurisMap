package com.turismap.app

import com.turismap.app.components.construirUriNavegacionAndroid
import com.turismap.app.components.construirUrlFallbackGoogleMaps
import com.turismap.app.components.esCoordenadaNavegable
import com.turismap.app.data.models.AtractivoPublico
import com.turismap.app.data.models.CategoriaAtractivoPublico
import com.turismap.app.data.models.Coordenada
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.AtractivosPublicosRepository
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.esCoordenadaValida
import com.turismap.app.screens.mapa.FiltroMarcadoresMapa
import com.turismap.app.screens.mapa.MarcadorMapa
import com.turismap.app.screens.mapa.buscarMarcadorMasCercanoParaAutoseleccion
import com.turismap.app.screens.mapa.filtrarAtractivosPublicos
import com.turismap.app.screens.mapa.filtrarMarcadoresPorCapa
import com.turismap.app.screens.mapa.separarCoordenadasSuperpuestasMarcadores
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AtractivoPublicoTest {

    private val ataco = Lugar(
        id = "ataco",
        nombre = "Concepción de Ataco",
        ubicacionCoordenadas = Coordenada(13.8703, -89.8483)
    )

    private val apaneca = Lugar(
        id = "apaneca",
        nombre = "Apaneca",
        ubicacionCoordenadas = Coordenada(13.8569, -89.8031)
    )

    private val juayua = Lugar(
        id = "juayua",
        nombre = "Juayúa",
        ubicacionCoordenadas = Coordenada(13.8417, -89.7456)
    )

    private val comercioAtaco = MarcadorMapa.Comercio(
        comercio = Comercio(
            id = "cafe-1",
            lugarId = "ataco",
            nombre = "Café del Portal",
            categoria = "cafeteria",
            coordenadas = Coordenada(13.8708, -89.8479)
        ),
        etiquetaCategoria = "Cafetería",
        nombreMunicipio = "Concepción de Ataco",
        coordenadas = Coordenada(13.8708, -89.8479)
    )

    @Test
    fun resuelveCategoriasConTildesMayusculasPluralesYValoresInvalidos() {
        assertEquals(CategoriaAtractivoPublico.MONTANA, CategoriaAtractivoPublico.desdeTexto("Montañas"))
        assertEquals(CategoriaAtractivoPublico.MONTANA, CategoriaAtractivoPublico.desdeTexto("  MONTAÑA "))
        assertEquals(CategoriaAtractivoPublico.MONTANA, CategoriaAtractivoPublico.desdeTexto("cordillera"))
        assertEquals(CategoriaAtractivoPublico.MIRADOR, CategoriaAtractivoPublico.desdeTexto("Miradores"))
        assertEquals(CategoriaAtractivoPublico.MIRADOR, CategoriaAtractivoPublico.desdeTexto("VIEWPOINT"))
        assertEquals(CategoriaAtractivoPublico.LAGUNA, CategoriaAtractivoPublico.desdeTexto("Lagunas"))
        assertEquals(CategoriaAtractivoPublico.LAGUNA, CategoriaAtractivoPublico.desdeTexto("  laguna "))
        assertEquals(CategoriaAtractivoPublico.RIO, CategoriaAtractivoPublico.desdeTexto("Ríos"))
        assertEquals(CategoriaAtractivoPublico.RIO, CategoriaAtractivoPublico.desdeTexto("río"))
        assertEquals(CategoriaAtractivoPublico.RIO, CategoriaAtractivoPublico.desdeTexto("Cascada natural"))
        assertEquals(CategoriaAtractivoPublico.CERRO, CategoriaAtractivoPublico.desdeTexto("Cerros"))
        assertEquals(CategoriaAtractivoPublico.CERRO, CategoriaAtractivoPublico.desdeTexto("CERRO"))

        assertNull(CategoriaAtractivoPublico.desdeTextoOrNull(null))
        assertNull(CategoriaAtractivoPublico.desdeTextoOrNull("   "))
        assertNull(CategoriaAtractivoPublico.desdeTextoOrNull("categoria_desconocida"))
        assertEquals(CategoriaAtractivoPublico.MIRADOR, CategoriaAtractivoPublico.desdeTexto("desconocido"))
    }

    @Test
    fun catalogoSeedCubreLasCincoCategoriasConCoordenadasValidasYSinHotlinks() {
        val seed = AtractivosPublicosRepository.catalogoSeedRespaldo

        assertEquals(5, seed.size)
        assertEquals(
            CategoriaAtractivoPublico.entries.toSet(),
            seed.map { it.categoriaResuelta }.toSet()
        )
        seed.forEach { atractivo ->
            assertTrue(atractivo.activo)
            assertTrue(atractivo.nombre.isNotBlank())
            assertTrue(atractivo.tieneCoordenadasValidas)
            assertTrue(atractivo.imagenes.isEmpty())
        }
    }

    @Test
    fun validaCoordenadasNavegablesYConstruyeUrisDeNavegacionExterna() {
        assertTrue(esCoordenadaNavegable(13.8922, -89.7875))
        assertFalse(esCoordenadaNavegable(0.0, 0.0))
        assertFalse(esCoordenadaNavegable(0.0, -89.7875))
        assertFalse(esCoordenadaNavegable(13.8922, 0.0))
        assertFalse(esCoordenadaNavegable(91.0, -89.7875))
        assertFalse(esCoordenadaNavegable(13.8922, -181.0))
        assertFalse(esCoordenadaNavegable(Double.NaN, -89.7875))
        assertFalse(esCoordenadaNavegable(13.8922, Double.POSITIVE_INFINITY))

        assertEquals(
            "google.navigation:q=13.8922,-89.7875",
            construirUriNavegacionAndroid(13.8922, -89.7875)
        )
        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=13.8922,-89.7875",
            construirUrlFallbackGoogleMaps(13.8922, -89.7875)
        )
    }

    @Test
    fun filtraAtractivosPorBusquedaYSubcategoriaYLuegoPorCapaDelMapa() {
        val seed = AtractivosPublicosRepository.catalogoSeedRespaldo
        val destinos = listOf(MarcadorMapa.Destino(ataco), MarcadorMapa.Destino(apaneca))
        val comercios = listOf(comercioAtaco)
        val atractivosMarcadores = seed.map { MarcadorMapa.AtractivoPublico(it) }

        // Búsqueda tolerante a tildes, mayúsculas y palabras clave de categoría
        val porRio = filtrarAtractivosPublicos(seed, consulta = "RÍOS")
        assertEquals(listOf("seed_chorros_la_calera_juayua"), porRio.map { it.id })

        val porMunicipioYCategoria = filtrarAtractivosPublicos(
            atractivos = seed,
            consulta = "apaneca",
            categoriaSeleccionada = CategoriaAtractivoPublico.LAGUNA
        )
        assertEquals(listOf("seed_laguna_verde_apaneca"), porMunicipioYCategoria.map { it.id })

        // Capa TODOS incluye pueblos, comercios y atractivos
        val capaTodos = filtrarMarcadoresPorCapa(
            destinos = destinos,
            comercios = comercios,
            atractivos = atractivosMarcadores,
            filtroCapa = FiltroMarcadoresMapa.TODOS
        )
        assertEquals(destinos.size + comercios.size + atractivosMarcadores.size, capaTodos.size)

        // Capa ATRACTIVOS sin subfiltro incluye los 5 atractivos y excluye pueblos/comercios
        val capaAtractivos = filtrarMarcadoresPorCapa(
            destinos = destinos,
            comercios = comercios,
            atractivos = atractivosMarcadores,
            filtroCapa = FiltroMarcadoresMapa.ATRACTIVOS
        )
        assertEquals(5, capaAtractivos.size)
        assertTrue(capaAtractivos.all { it is MarcadorMapa.AtractivoPublico })

        // Capa ATRACTIVOS con subfiltro CERRO devuelve solo Cerros
        val soloCerros = filtrarMarcadoresPorCapa(
            destinos = destinos,
            comercios = comercios,
            atractivos = atractivosMarcadores,
            filtroCapa = FiltroMarcadoresMapa.ATRACTIVOS,
            categoriaAtractivo = CategoriaAtractivoPublico.CERRO
        )
        assertEquals(1, soloCerros.size)
        val marcadorCerro = assertIs<MarcadorMapa.AtractivoPublico>(soloCerros.first())
        assertEquals(CategoriaAtractivoPublico.CERRO, marcadorCerro.categoria)
    }

    @Test
    fun atractivosConCoordenadasDistintasNoSeDesplazanYColisionesSeSeparanDeterministamente() {
        val atractivosSeed = AtractivosPublicosRepository.catalogoSeedRespaldo
            .map { MarcadorMapa.AtractivoPublico(it) }

        val sinColisiones = separarCoordenadasSuperpuestasMarcadores(atractivosSeed)
        assertEquals(atractivosSeed, sinColisiones)

        // Si dos atractivos y un pueblo comparten exactamente la misma coordenada,
        // el pueblo queda anclado y los atractivos se separan en anillo deterministamente
        val puntoComun = ataco.ubicacionCoordenadas
        val destinoAncla = MarcadorMapa.Destino(ataco)
        val a1 = MarcadorMapa.AtractivoPublico(
            AtractivoPublico(
                id = "a1",
                nombre = "Mirador Norte",
                categoria = CategoriaAtractivoPublico.MIRADOR.claveFirestore,
                coordenadas = puntoComun
            )
        )
        val a2 = MarcadorMapa.AtractivoPublico(
            AtractivoPublico(
                id = "a2",
                nombre = "Mirador Sur",
                categoria = CategoriaAtractivoPublico.MIRADOR.claveFirestore,
                coordenadas = puntoComun
            )
        )

        val entrada = listOf(destinoAncla, a1, a2)
        val separados = separarCoordenadasSuperpuestasMarcadores(entrada)
        val destinoResultado = separados.first { it.idUnico == destinoAncla.idUnico }
        val atractivosResultado = separados.filterIsInstance<MarcadorMapa.AtractivoPublico>()

        assertEquals(puntoComun, destinoResultado.coordenadas)
        assertEquals(2, atractivosResultado.size)
        atractivosResultado.forEach { marcador ->
            assertTrue(esCoordenadaValida(marcador.coordenadas))
            assertNotEquals(puntoComun, marcador.coordenadas)
        }
        assertEquals(2, atractivosResultado.map { it.coordenadas }.distinct().size)

        val separadosInvertidos = separarCoordenadasSuperpuestasMarcadores(entrada.reversed())
        assertEquals(
            separados.associate { it.idUnico to it.coordenadas },
            separadosInvertidos.associate { it.idUnico to it.coordenadas }
        )
    }

    @Test
    fun autoseleccionInteligentePrefiereAtractivoCercanoYNoAbrePuebloCuandoEstaLejos() {
        val lugares = listOf(ataco, apaneca, juayua)
        val atractivos = AtractivosPublicosRepository.catalogoSeedRespaldo
        val comerciosPropios = listOf(comercioAtaco)

        // 1. Coordenadas de Mirador de La Cruz (~560 m de Ataco): selecciona AtractivoPublico, NO Ataco
        val miradorSeleccionado = buscarMarcadorMasCercanoParaAutoseleccion(
            latObjetivo = 13.8744,
            lngObjetivo = -89.8453,
            lugares = lugares,
            atractivosPublicos = atractivos,
            comerciosConCoordenadaPropia = comerciosPropios
        )
        val atractivo = assertIs<MarcadorMapa.AtractivoPublico>(miradorSeleccionado)
        assertEquals("seed_mirador_la_cruz_ataco", atractivo.atractivo.id)

        // 2. Coordenadas exactas de Ataco: selecciona Destino (Ataco)
        val puebloSeleccionado = buscarMarcadorMasCercanoParaAutoseleccion(
            latObjetivo = ataco.ubicacionCoordenadas.latitud,
            lngObjetivo = ataco.ubicacionCoordenadas.longitud,
            lugares = lugares,
            atractivosPublicos = atractivos,
            comerciosConCoordenadaPropia = comerciosPropios
        )
        val destino = assertIs<MarcadorMapa.Destino>(puebloSeleccionado)
        assertEquals("ataco", destino.lugar.id)

        // 3. Coordenadas propias de un comercio: selecciona Comercio
        val comercioSeleccionado = buscarMarcadorMasCercanoParaAutoseleccion(
            latObjetivo = comercioAtaco.coordenadas.latitud,
            lngObjetivo = comercioAtaco.coordenadas.longitud,
            lugares = lugares,
            atractivosPublicos = atractivos,
            comerciosConCoordenadaPropia = comerciosPropios
        )
        val comercio = assertIs<MarcadorMapa.Comercio>(comercioSeleccionado)
        assertEquals("cafe-1", comercio.comercio.id)

        // 4. Coordenadas de un punto alejado (> 350 m de cualquier marcador): no selecciona pueblo por error
        val fueraDeUmbral = buscarMarcadorMasCercanoParaAutoseleccion(
            latObjetivo = 13.8200,
            lngObjetivo = -89.8800,
            lugares = lugares,
            atractivosPublicos = atractivos,
            comerciosConCoordenadaPropia = comerciosPropios
        )
        assertNull(fueraDeUmbral)
    }
}
