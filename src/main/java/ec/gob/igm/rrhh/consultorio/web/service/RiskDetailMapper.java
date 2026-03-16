package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgoDet;

@Stateless
/**
 * Class RiskDetailMapper: orquesta la lógica de presentación y flujo web.
 */
public class RiskDetailMapper implements Serializable {

    @EJB
    private RiskKeyParser riskKeyParser;

    public void mapRiskActivitiesToHeader(FichaRiesgo riesgo, List<String> actividadesLab) {
        riesgo.setActividad1(getSafe(actividadesLab, 0));
        riesgo.setActividad2(getSafe(actividadesLab, 1));
        riesgo.setActividad3(getSafe(actividadesLab, 2));
        riesgo.setActividad4(getSafe(actividadesLab, 3));
        riesgo.setActividad5(getSafe(actividadesLab, 4));
        riesgo.setActividad6(getSafe(actividadesLab, 5));
        riesgo.setActividad7(getSafe(actividadesLab, 6));
    }

    public String construirMedidas(List<String> medidas) {
        if (medidas == null || medidas.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < medidas.size(); i++) {
            String m = medidas.get(i);
            if (!isBlank(m)) {
                if (sb.length() > 0) {
                    sb.append(" | ");
                }
                sb.append("M").append(i + 1).append(": ").append(m.trim());
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    public List<FichaRiesgoDet> mapCheckedRiskItems(FichaOcupacional ficha, Map<String, Boolean> riesgos) {
        List<FichaRiesgoDet> detalles = new ArrayList<>();
        if (riesgos == null || riesgos.isEmpty()) {
            return detalles;
        }

        int orden = 1;
        for (Map.Entry<String, Boolean> e : riesgos.entrySet()) {
            if (!Boolean.TRUE.equals(e.getValue())) {
                continue;
            }

            RiskKeyParser.RiskKey rk = riskKeyParser.parseRiskKey(e.getKey());
            if (rk == null) {
                continue;
            }

            FichaRiesgoDet det = new FichaRiesgoDet();
            det.setFicha(ficha);
            det.setGrupo(rk.grupo());
            det.setItem(rk.item());
            det.setActividadNro(rk.actividad());
            det.setMarcado("S");
            det.setOrden(orden++);
            detalles.add(det);
        }
        return detalles;
    }

    public List<FichaRiesgoDet> mapOtherRiskItems(FichaOcupacional ficha, Map<String, String> otrosRiesgos) {
        List<FichaRiesgoDet> detalles = new ArrayList<>();
        if (otrosRiesgos == null || otrosRiesgos.isEmpty()) {
            return detalles;
        }

        int ordenOtros = 10000;
        for (Map.Entry<String, String> e : otrosRiesgos.entrySet()) {
            String val = e.getValue();
            if (isBlank(val)) {
                continue;
            }

            RiskKeyParser.RiskKey rk = riskKeyParser.parseRiskKeyOtros(e.getKey());
            if (rk == null) {
                continue;
            }

            FichaRiesgoDet det = new FichaRiesgoDet();
            det.setFicha(ficha);
            det.setGrupo(rk.grupo());
            det.setItem("OTROS: " + val.trim());
            det.setActividadNro(rk.actividad());
            det.setMarcado("S");
            det.setOrden(ordenOtros++);
            detalles.add(det);
        }
        return detalles;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static <T> T getSafe(List<T> list, int idx) {
        if (list == null || idx < 0 || idx >= list.size()) {
            return null;
        }
        return list.get(idx);
    }
}
