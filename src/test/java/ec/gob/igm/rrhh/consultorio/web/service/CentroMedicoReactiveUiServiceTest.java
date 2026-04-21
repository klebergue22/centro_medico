package ec.gob.igm.rrhh.consultorio.web.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CentroMedicoReactiveUiServiceTest {

    private final CentroMedicoReactiveUiService service = new CentroMedicoReactiveUiService();

    @Test
    void onConsumoTiempoChangeDebeDesmarcarNoConsumeCuandoHayTiempoRegistrado() {
        Boolean[] noConsume = { Boolean.TRUE };
        Integer[] tiempoConsumo = { 18 };
        Integer[] tiempoAbstinencia = { null };

        service.onConsumoTiempoChange(noConsume, tiempoConsumo, tiempoAbstinencia, 0);

        assertFalse(noConsume[0]);
    }

    @Test
    void onNoConsumeChangeNoDebeBorrarDatosSoloDesmarcarSiHayTiempoRegistrado() {
        Boolean[] noConsume = { Boolean.TRUE };
        Boolean[] exConsumidor = { Boolean.TRUE };
        Integer[] tiempoConsumo = { 12 };
        Integer[] tiempoAbstinencia = { 3 };

        service.onNoConsumeChange(noConsume, exConsumidor, tiempoConsumo, tiempoAbstinencia, 0);

        assertFalse(noConsume[0]);
        assertTrue(exConsumidor[0]);
        assertEquals(12, tiempoConsumo[0]);
        assertEquals(3, tiempoAbstinencia[0]);
    }
}
