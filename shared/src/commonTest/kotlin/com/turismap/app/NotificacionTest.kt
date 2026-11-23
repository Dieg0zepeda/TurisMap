package com.turismap.app

import com.turismap.app.data.models.AccionNotificacion
import com.turismap.app.data.models.Notificacion
import com.turismap.app.data.models.TipoNotificacion
import com.turismap.app.data.models.contarNotificacionesNoLeidas
import com.turismap.app.data.models.formatearBadgeNotificaciones
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NotificacionTest {

    @Test
    fun cuentaSoloNotificacionesNoLeidas() {
        val lista = listOf(
            Notificacion(id = "n1", leida = false),
            Notificacion(id = "n2", leida = true),
            Notificacion(id = "n3", leida = false),
            Notificacion(id = "n4", leida = true)
        )

        assertEquals(2, contarNotificacionesNoLeidas(lista))
        assertEquals(0, contarNotificacionesNoLeidas(emptyList()))
        assertEquals(0, contarNotificacionesNoLeidas(listOf(Notificacion(id = "n5", leida = true))))
    }

    @Test
    fun formateaBadgeParaCeroRangoNormalYMayoresA99() {
        assertNull(formatearBadgeNotificaciones(-1))
        assertNull(formatearBadgeNotificaciones(0))
        assertEquals("1", formatearBadgeNotificaciones(1))
        assertEquals("12", formatearBadgeNotificaciones(12))
        assertEquals("99", formatearBadgeNotificaciones(99))
        assertEquals("99+", formatearBadgeNotificaciones(100))
        assertEquals("99+", formatearBadgeNotificaciones(250))
    }

    @Test
    fun resuelveTipoYAccionParaSolicitudAprobadaConYsinComercioId() {
        val aprobadaConComercio = Notificacion(
            id = "n-ok",
            tipo = Notificacion.TIPO_SOLICITUD_APROBADA,
            comercioId = "comercio_123"
        )
        val aprobadaSinComercio = Notificacion(
            id = "n-ok-vacio",
            tipo = "  SOLICITUD_APROBADA ",
            comercioId = "   "
        )

        assertEquals(TipoNotificacion.SOLICITUD_APROBADA, aprobadaConComercio.tipoResuelto)
        assertTrue(aprobadaConComercio.esAprobada)
        assertFalse(aprobadaConComercio.esRechazada)
        assertEquals(AccionNotificacion.VER_COMERCIO, aprobadaConComercio.accionPrincipal)

        assertEquals(TipoNotificacion.SOLICITUD_APROBADA, aprobadaSinComercio.tipoResuelto)
        assertEquals(AccionNotificacion.NINGUNA, aprobadaSinComercio.accionPrincipal)
    }

    @Test
    fun resuelveTipoYAccionParaSolicitudRechazadaYTipoDesconocido() {
        val rechazada = Notificacion(
            id = "n-rechazada",
            tipo = Notificacion.TIPO_SOLICITUD_RECHAZADA,
            mensaje = "Falta completar la dirección."
        )
        val desconocida = Notificacion(
            id = "n-otro",
            tipo = "promocion_nueva"
        )

        assertEquals(TipoNotificacion.SOLICITUD_RECHAZADA, rechazada.tipoResuelto)
        assertTrue(rechazada.esRechazada)
        assertFalse(rechazada.esAprobada)
        assertEquals(AccionNotificacion.VER_PLANES, rechazada.accionPrincipal)

        assertEquals(TipoNotificacion.OTRO, desconocida.tipoResuelto)
        assertFalse(desconocida.esAprobada)
        assertFalse(desconocida.esRechazada)
        assertEquals(AccionNotificacion.NINGUNA, desconocida.accionPrincipal)
    }
}
