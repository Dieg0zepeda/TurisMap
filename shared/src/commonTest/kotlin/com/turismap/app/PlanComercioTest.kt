package com.turismap.app

import com.turismap.app.data.repository.PlanComercio
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlanComercioTest {

    @Test
    fun formateaPrecioConMonedaYPeriodicidadDelPlan() {
        val plan = PlanComercio(
            precio = 5.0,
            moneda = "USD",
            periodicidad = "MES"
        )

        assertEquals("5 USD / MES", plan.precioFormateado())
    }

    @Test
    fun conservaDecimalesEnElPrecio() {
        val plan = PlanComercio(
            precio = 15.5,
            moneda = "USD",
            periodicidad = "MES"
        )

        assertEquals("15.5 USD / MES", plan.precioFormateado())
    }

    @Test
    fun noInventaPrecioCuandoFaltaElCampo() {
        val plan = PlanComercio(
            moneda = "USD",
            periodicidad = "MES"
        )

        assertNull(plan.precioFormateado())
    }

    @Test
    fun leePlanSinPrecioYConIdInternoNulo() {
        val json = """
            {
              "nombre": "Plan de prueba",
              "descripcion": "Descripción",
              "beneficios": [],
              "moneda": "USD",
              "periodicidad": "MES",
              "orden": 1,
              "activo": true,
              "id": null
            }
        """.trimIndent()

        val plan = Json.decodeFromString(PlanComercio.serializer(), json)

        assertNull(plan.precio)
        assertNull(plan.id)
        assertNull(plan.precioFormateado())
    }

    @Test
    fun leePrecioNumericoEnteroComoDouble() {
        val json = """
            {
              "nombre": "Plan de prueba",
              "precio": 5,
              "moneda": "USD",
              "periodicidad": "MES",
              "orden": 1,
              "activo": true
            }
        """.trimIndent()

        val plan = Json.decodeFromString(PlanComercio.serializer(), json)

        assertEquals(5.0, plan.precio)
        assertEquals("5 USD / MES", plan.precioFormateado())
    }
}
