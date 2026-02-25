package ec.gob.igm.rrhh.consultorio.web.mapper;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.web.util.SnUtils;
import jakarta.ejb.Stateless;

/**
 * Mapper del STEP1 (Examen físico regional):
 * - Toma los Boolean *Bool (checkbox) y los convierte a "S"/"N" en los campos EXF_*.
 *
 * Nota:
 * - En tu modelo SOLO existen algunos Bool (los "últimos" de ciertos tópicos):
 *   exfNarizSenosBool, exfToraxParrillaBool, exfCuelloTiroidesBool,
 *   exfAbdomenViscerasBool, exfAbdomenParedBool,
 *   exfColumnaFlexibilidadBool, exfColumnaDesviacionBool, exfColumnaDolorBool,
 *   exfExtSupBool, exfExtInfBool
 *
 * Los demás EXF_* se alimentan desde String/checkboxes que ya estás mapeando con preferSn
 * en el Ctrl; en esta v1 migramos SOLO los que hoy te están fallando por nombre.
 */
@Stateless
public class ExamenFisicoStep1Mapper {

    /**
     * Aplica "S"/"N" a los campos finales usando los Bool del modelo.
     * No toca los demás campos del examen físico que ya vienen desde la UI.
     */
    public void applyUltimosDeTopicos(FichaOcupacional ficha) {
        if (ficha == null) return;

        // ✅ Nariz - último: Senos paranasales
        ficha.setExfNarizSenosParanasa(
                SnUtils.fromBoolean(ficha.getExfNarizSenosBool())
        );

        // ✅ Cuello - Tiroides / masas
        ficha.setExfCuelloTiroidesMasas(
                SnUtils.fromBoolean(ficha.getExfCuelloTiroidesBool())
        );

        // ✅ Tórax - último: Parrilla costal
        ficha.setExfToraxParrillaCostal(
                SnUtils.fromBoolean(ficha.getExfToraxParrillaBool())
        );

        // ✅ Abdomen - vísceras / pared (en modelo están como Abdomen*)
        ficha.setExfAbdVisceras(
                SnUtils.fromBoolean(ficha.getExfAbdomenViscerasBool())
        );
        ficha.setExfAbdParedAbdominal(
                SnUtils.fromBoolean(ficha.getExfAbdomenParedBool())
        );

        // ✅ Columna
        ficha.setExfColFlexibilidad(
                SnUtils.fromBoolean(ficha.getExfColumnaFlexibilidadBool())
        );
        ficha.setExfColDesviacion(
                SnUtils.fromBoolean(ficha.getExfColumnaDesviacionBool())
        );
        ficha.setExfColDolor(
                SnUtils.fromBoolean(ficha.getExfColumnaDolorBool())
        );

        // ✅ Extremidades
        ficha.setExfExtMiembrosSup(
                SnUtils.fromBoolean(ficha.getExfExtSupBool())
        );
        ficha.setExfExtMiembrosInf(
                SnUtils.fromBoolean(ficha.getExfExtInfBool())
        );
    }
}
