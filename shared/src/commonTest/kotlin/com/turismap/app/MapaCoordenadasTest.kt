package com.turismap.app

import com.turismap.app.data.models.Coordenada
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.Comercio
import com.turismap.app.data.repository.esCoordenadaValida
import com.turismap.app.data.repository.parsearCoordenadaDesdeTexto
import com.turismap.app.screens.mapa.buscarMunicipioDeComercio
import com.turismap.app.screens.mapa.coincideBusquedaMapa
import com.turismap.app.screens.mapa.normalizarTextoMapa
import com.turismap.app.screens.mapa.relevanciaBusquedaMapa
import com.turismap.app.screens.mapa.resolverCoordenadasComercio
import com.turismap.app.screens.mapa.separarCoordenadasSuperpuestas
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MapaCoordenadasTest {

    private val atacoCentro = Coordenada(latitud = 13.8703, longitud = -89.8486)
    private val juayuaCentro = Coordenada(latitud = 13.8417, longitud = -89.7456)

    private val atacoDestino = Lugar(
        id = "ataco",
        nombre = "Concepción de Ataco",
        ubicacionCoordenadas = atacoCentro
    )

    private val juayuaDestino = Lugar(
        id = "juayua",
        nombre = "Juayúa",
        ubicacionCoordenadas = juayuaCentro
    )

    private val municipiosMap = mapOf(
        atacoDestino.id to atacoDestino,
        juayuaDestino.id to juayuaDestino
    )

    @Test
    fun comercioPriorizaSusCoordenadasPropiasAntesQueElMunicipio() {
        val coordenadaReal = Coordenada(latitud = 13.8742, longitud = -89.8441)
        val comercio = Comercio(
            id = "negocio-1",
            lugarId = "ataco",
            nombre = "Café del Mirador",
            coordenadas = coordenadaReal
        )

        val resuelta = resolverCoordenadasComercio(comercio, municipiosMap)

        assertEquals(coordenadaReal, resuelta)
    }

    @Test
    fun comercioAceptaUbicacionCoordenadasYCamposPlanosLatLng() {
        val desdeUbicacion = Comercio(
            id = "negocio-2",
            lugarId = "ataco",
            ubicacionCoordenadas = Coordenada(13.8715, -89.8492)
        )
        val desdePlanos = Comercio(
            id = "negocio-3",
            lugarId = "ataco",
            latitud = 13.8725,
            longitud = -89.8501
        )

        assertEquals(Coordenada(13.8715, -89.8492), resolverCoordenadasComercio(desdeUbicacion, municipiosMap))
        assertEquals(Coordenada(13.8725, -89.8501), resolverCoordenadasComercio(desdePlanos, municipiosMap))
    }

    @Test
    fun comercioConCoordenadasInvalidasHaceFallbackAlMunicipio() {
        val comercioInvalido = Comercio(
            id = "negocio-4",
            lugarId = "  JUAYÚA ",
            coordenadas = Coordenada(0.0, 0.0),
            latitud = Double.NaN,
            longitud = -250.0
        )

        val resuelta = resolverCoordenadasComercio(comercioInvalido, municipiosMap)

        assertEquals(juayuaCentro, resuelta)
    }

    @Test
    fun parsearCoordenadaDesdeTextoValidaYDescartaTiposInesperados() {
        assertEquals(Coordenada(13.8703, -89.8486), parsearCoordenadaDesdeTexto("13.8703, -89.8486"))
        assertEquals(Coordenada(13.8703, -89.8486), parsearCoordenadaDesdeTexto("13.8703;-89.8486"))
        assertNull(parsearCoordenadaDesdeTexto(""))
        assertNull(parsearCoordenadaDesdeTexto("sin coordenadas"))
        assertNull(parsearCoordenadaDesdeTexto("0.0, 0.0"))
        assertNull(parsearCoordenadaDesdeTexto("95.0, -89.8486"))
        assertFalse(esCoordenadaValida(Coordenada(13.87, 0.0)))
    }

    @Test
    fun municipioPermaneceFijoYCuatroComerciosEnAtacoSeSeparanEnAnillo() {
        val comerciosAtaco = (1..4).map { indice ->
            Lugar(
                id = "comercio:ataco-$indice",
                nombre = "Negocio $indice",
                ubicacionCoordenadas = atacoCentro
            )
        }
        val entrada = listOf(atacoDestino) + comerciosAtaco

        val separados = separarCoordenadasSuperpuestas(entrada)
        val municipioResultado = separados.first { it.id == atacoDestino.id }
        val comerciosResultado = separados.filter { it.id.startsWith("comercio:") }

        // Regla de ancla: el destino/municipio no se mueve de su coordenada original
        assertEquals(atacoCentro, municipioResultado.ubicacionCoordenadas)

        // Los 4 comercios quedan separados entre sí y distintos al centro del municipio
        assertEquals(4, comerciosResultado.size)
        comerciosResultado.forEach { comercio ->
            assertTrue(esCoordenadaValida(comercio.ubicacionCoordenadas))
            assertNotEquals(atacoCentro, comercio.ubicacionCoordenadas)
        }
        assertEquals(4, comerciosResultado.map { it.ubicacionCoordenadas }.distinct().size)

        // Determinismo: invertir el orden de entrada produce las mismas coordenadas por ID
        val separadosOrdenInvertido = separarCoordenadasSuperpuestas(entrada.reversed())
        val porIdOriginal = separados.associate { it.id to it.ubicacionCoordenadas }
        val porIdInvertido = separadosOrdenInvertido.associate { it.id to it.ubicacionCoordenadas }
        assertEquals(porIdOriginal, porIdInvertido)
    }

    @Test
    fun cuatroComerciosSinDestinoEnMismoPuntoSeSeparanTodos() {
        val soloComercios = (1..4).map { indice ->
            Lugar(
                id = "comercio:cliente-mismo-$indice",
                nombre = "Local $indice",
                ubicacionCoordenadas = atacoCentro
            )
        }

        val separados = separarCoordenadasSuperpuestas(soloComercios)

        assertEquals(4, separados.map { it.ubicacionCoordenadas }.distinct().size)
        separados.forEach { assertTrue(esCoordenadaValida(it.ubicacionCoordenadas)) }
    }

    @Test
    fun comerciosConCoordenadasRealesDistintasNoSeAlteran() {
        val c1 = Lugar(id = "comercio:1", ubicacionCoordenadas = Coordenada(13.8703, -89.8486))
        val c2 = Lugar(id = "comercio:2", ubicacionCoordenadas = Coordenada(13.8730, -89.8460))
        val c3 = Lugar(id = "comercio:3", ubicacionCoordenadas = Coordenada(13.8680, -89.8510))

        val resultado = separarCoordenadasSuperpuestas(listOf(c1, c2, c3))

        assertEquals(listOf(c1, c2, c3), resultado)
    }

    @Test
    fun busquedaMapaNormalizaTildesMayusculasYEspaciosDeTeclado() {
        assertEquals("cafe el mirador", normalizarTextoMapa("  Café   El Mirador  "))
        assertTrue(coincideBusquedaMapa("cafe", "Café El Mirador"))
        assertTrue(coincideBusquedaMapa("  juayua  ", "Juayúa"))
        assertTrue(coincideBusquedaMapa("CABANA", "Cabaña del Bosque"))
        assertTrue(coincideBusquedaMapa("artesanias ", "Tienda de Artesanías", "Concepción de Ataco"))
        assertFalse(coincideBusquedaMapa("playa", "Café El Mirador", "Concepción de Ataco"))
    }

    @Test
    fun busquedaMapaSoportaMultiplesPalabrasYPriorizaCoincidenciasEnNombre() {
        assertTrue(coincideBusquedaMapa("cafe ataco", "Café El Mirador", "cafeteria", "Concepción de Ataco"))
        assertTrue(coincideBusquedaMapa("restaurante juayua", "La Casa Verde", "Restaurante", "Juayúa"))

        val relNombreInicio = relevanciaBusquedaMapa("cafe", "Café El Mirador", "Restaurante", "Ataco")
        val relNombreContenido = relevanciaBusquedaMapa("mirador", "Café El Mirador", "Restaurante", "Ataco")
        val relSecundario = relevanciaBusquedaMapa("ataco", "Café El Mirador", "Restaurante", "Concepción de Ataco")

        assertTrue(relNombreInicio < relNombreContenido)
        assertTrue(relNombreContenido < relSecundario)
        assertEquals(Int.MAX_VALUE, relevanciaBusquedaMapa("volcan", "Café El Mirador", "Ataco"))
    }

    @Test
    fun comercioConCampoMunicipioSinLugarIdResuelveMunicipioYCoordenadas() {
        val comercioSoloMunicipio = Comercio(
            id = "negocio-nuevo",
            lugarId = "",
            municipio = "Concepción de Ataco",
            nombre = "Café Nuevo"
        )

        assertEquals(atacoDestino, buscarMunicipioDeComercio(comercioSoloMunicipio, municipiosMap))
        assertEquals(atacoCentro, resolverCoordenadasComercio(comercioSoloMunicipio, municipiosMap))
    }
}
