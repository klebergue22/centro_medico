package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.Date;

import jakarta.enterprise.context.ApplicationScoped;

import ec.gob.igm.rrhh.consultorio.web.util.CentroMedicoCalcUtil;

@ApplicationScoped
/**
 * Class CentroMedicoReactiveUiService: orquesta la lógica de presentación y flujo web.
 */
public class CentroMedicoReactiveUiService implements Serializable {

    private static final long serialVersionUID = 1L;

    public Integer recalculateEdad(Date fechaNacimiento, CentroMedicoCalcUtil calcUtil) {
        return calcUtil.calcularEdad(fechaNacimiento);
    }

    public String buildEdadCalculationMessage(Integer edad) {
        return "Edad calculada: " + (edad == null ? "(sin fecha)" : edad + " años");
    }

    public Double recalculateImc(Double peso, Double tallaCm, CentroMedicoCalcUtil calcUtil) {
        return calcUtil.recalcularIMC(peso, tallaCm);
    }

    public boolean validarEdadMinima(Integer edad, CentroMedicoCalcUtil calcUtil) {
        return calcUtil.validarEdadMinima(edad);
    }

    public void onNoConsumeChange(Boolean[] consNoConsume, Boolean[] consExConsumidor,
                                  Integer[] consTiempoConsumoMeses, Integer[] consTiempoAbstinenciaMeses,
                                  int idx) {
        if (hasConsumoRegistrado(consTiempoConsumoMeses[idx], consTiempoAbstinenciaMeses[idx])) {
            consNoConsume[idx] = false;
        }
    }

    public void onConsumoTiempoChange(Boolean[] consNoConsume,
                                      Integer[] consTiempoConsumoMeses,
                                      Integer[] consTiempoAbstinenciaMeses,
                                      int idx) {
        if (hasConsumoRegistrado(consTiempoConsumoMeses[idx], consTiempoAbstinenciaMeses[idx])) {
            consNoConsume[idx] = false;
        }
    }

    private boolean hasConsumoRegistrado(Integer tiempoConsumo, Integer tiempoAbstinencia) {
        return isTiempoMayorACero(tiempoConsumo) || isTiempoMayorACero(tiempoAbstinencia);
    }

    private boolean isTiempoMayorACero(Integer value) {
        return value != null && value > 0;
    }

    public AttentionPriorityState onToggleDiscapacidad(boolean apDiscapacidad,
                                                        String discapTipo,
                                                        String discapDesc,
                                                        Integer discapPorc) {
        if (!apDiscapacidad) {
            return new AttentionPriorityState(null, null, null, null, null);
        }
        return new AttentionPriorityState(discapTipo, discapDesc, discapPorc, null, null);
    }

    public AttentionPriorityState onToggleCatastrofica(boolean apCatastrofica,
                                                        String catasDiagnostico,
                                                        Boolean catasCalificada) {
        if (!apCatastrofica) {
            return new AttentionPriorityState(null, null, null, null, null);
        }
        return new AttentionPriorityState(null, null, null, catasDiagnostico, catasCalificada);
    }

    public static class AttentionPriorityState {
        private final String discapTipo;
        private final String discapDesc;
        private final Integer discapPorc;
        private final String catasDiagnostico;
        private final Boolean catasCalificada;

        public AttentionPriorityState(String discapTipo, String discapDesc, Integer discapPorc,
                                      String catasDiagnostico, Boolean catasCalificada) {
            this.discapTipo = discapTipo;
            this.discapDesc = discapDesc;
            this.discapPorc = discapPorc;
            this.catasDiagnostico = catasDiagnostico;
            this.catasCalificada = catasCalificada;
        }

        public String getDiscapTipo() {
            return discapTipo;
        }

        public String getDiscapDesc() {
            return discapDesc;
        }

        public Integer getDiscapPorc() {
            return discapPorc;
        }

        public String getCatasDiagnostico() {
            return catasDiagnostico;
        }

        public Boolean getCatasCalificada() {
            return catasCalificada;
        }
    }
}
