package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgoDet;
import ec.gob.igm.rrhh.consultorio.service.FichaRiesgoDetService;
import ec.gob.igm.rrhh.consultorio.service.FichaRiesgoService;

@Stateless
public class Step2RiskOrchestratorService implements Serializable {

    @EJB
    private FichaRiesgoService fichaRiesgoService;
    @EJB
    private FichaRiesgoDetService fichaRiesgoDetService;

    public FichaRiesgo save(Step2RiskCommand cmd) {
        ensureFichaSavedOrThrow(cmd.ficha());

        FichaRiesgo riesgo = upsertRiskHeader(cmd);
        replaceRiskDetails(cmd);

        return riesgo;
    }

    private void ensureFichaSavedOrThrow(FichaOcupacional ficha) {
        if (ficha == null || ficha.getIdFicha() == null) {
            throw new IllegalArgumentException("Primero debe existir y estar guardada la ficha (ID_FICHA).");
        }
    }

    private FichaRiesgo upsertRiskHeader(Step2RiskCommand cmd) {
        FichaRiesgo riesgo = cmd.fichaRiesgo() != null ? cmd.fichaRiesgo() : new FichaRiesgo();
        riesgo.setFicha(cmd.ficha());

        riesgo.setActividad1(getSafe(cmd.actividadesLab(), 0));
        riesgo.setActividad2(getSafe(cmd.actividadesLab(), 1));
        riesgo.setActividad3(getSafe(cmd.actividadesLab(), 2));
        riesgo.setActividad4(getSafe(cmd.actividadesLab(), 3));
        riesgo.setActividad5(getSafe(cmd.actividadesLab(), 4));
        riesgo.setActividad6(getSafe(cmd.actividadesLab(), 5));
        riesgo.setActividad7(getSafe(cmd.actividadesLab(), 6));

        riesgo.setMedidasPreventivas(construirMedidas(cmd.medidasPreventivas()));
        stampAuditFieldsForRiskHeader(riesgo, cmd.now(), cmd.user());

        return fichaRiesgoService.guardar(riesgo);
    }

    private void stampAuditFieldsForRiskHeader(FichaRiesgo fr, Date now, String user) {
        if (fr.getIdFichaRiesgo() == null) {
            fr.setEstado("BORRADOR");
            fr.setFCreacion(now);
            fr.setUsrCreacion(user);
        } else {
            fr.setFActualizacion(now);
            fr.setUsrActualizacion(user);
        }
    }

    private void replaceRiskDetails(Step2RiskCommand cmd) {
        fichaRiesgoDetService.eliminarPorFicha(cmd.ficha().getIdFicha());
        persistCheckedRiskItems(cmd);
        persistOtherRiskItems(cmd);
    }

    private void persistCheckedRiskItems(Step2RiskCommand cmd) {
        if (cmd.riesgos() == null || cmd.riesgos().isEmpty()) {
            return;
        }
        int orden = 1;

        for (Map.Entry<String, Boolean> e : cmd.riesgos().entrySet()) {
            if (!Boolean.TRUE.equals(e.getValue())) {
                continue;
            }

            RiskKey rk = parseRiskKey(e.getKey());
            if (rk == null) {
                continue;
            }

            FichaRiesgoDet det = new FichaRiesgoDet();
            det.setFicha(cmd.ficha());
            det.setGrupo(rk.grupo());
            det.setItem(rk.item());
            det.setActividadNro(rk.actividad());
            det.setMarcado("S");
            det.setOrden(orden++);

            fichaRiesgoDetService.guardar(det, cmd.user());
        }
    }

    private void persistOtherRiskItems(Step2RiskCommand cmd) {
        if (cmd.otrosRiesgos() == null || cmd.otrosRiesgos().isEmpty()) {
            return;
        }
        int ordenOtros = 10000;

        for (Map.Entry<String, String> e : cmd.otrosRiesgos().entrySet()) {
            String val = e.getValue();
            if (isBlank(val)) {
                continue;
            }

            RiskKey rk = parseRiskKeyOtros(e.getKey());
            if (rk == null) {
                continue;
            }

            FichaRiesgoDet det = new FichaRiesgoDet();
            det.setFicha(cmd.ficha());
            det.setGrupo(rk.grupo());
            det.setItem("OTROS: " + val.trim());
            det.setActividadNro(rk.actividad());
            det.setMarcado("S");
            det.setOrden(ordenOtros++);

            fichaRiesgoDetService.guardar(det, cmd.user());
        }
    }

    private String construirMedidas(List<String> medidas) {
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

    private RiskKey parseRiskKey(String key) {
        if (isBlank(key)) {
            return null;
        }
        String k = key.trim();
        int last = k.lastIndexOf('_');
        if (last < 0) {
            return null;
        }
        Integer act = parseActividad(k.substring(last + 1));
        if (act == null || k.length() < 3) {
            return null;
        }

        String prefRaw = k.substring(0, 3);
        String grupo = grupoFromPrefix(prefRaw);
        String item = k.substring(0, last).replace('_', ' ');
        String prefWithSpace = prefRaw + " ";
        if (item.startsWith(prefWithSpace)) {
            item = item.substring(prefWithSpace.length());
        }
        return new RiskKey(grupo, item, act);
    }

    private RiskKey parseRiskKeyOtros(String key) {
        if (isBlank(key)) {
            return null;
        }
        String k = key.trim();
        int last = k.lastIndexOf('_');
        if (last < 0) {
            return null;
        }
        Integer act = parseActividad(k.substring(last + 1));
        if (act == null || k.length() < 3) {
            return null;
        }

        String prefRaw = k.substring(0, 3);
        String grupo = grupoFromPrefix(prefRaw);
        return new RiskKey(grupo, "OTROS", act);
    }

    private Integer parseActividad(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String grupoFromPrefix(String pref) {
        switch (pref) {
            case "FIS":
                return "FISICO";
            case "SEG":
                return "SEGURIDAD";
            case "QUI":
                return "QUIMICO";
            case "BIO":
                return "BIOLOGICO";
            case "ERG":
                return "ERGONOMICO";
            case "PSI":
                return "PSICOSOCIAL";
            default:
                return "OTROS";
        }
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

    private record RiskKey(String grupo, String item, Integer actividad) {
    }

    public record Step2RiskCommand(
            FichaOcupacional ficha,
            FichaRiesgo fichaRiesgo,
            List<String> actividadesLab,
            List<String> medidasPreventivas,
            Map<String, Boolean> riesgos,
            Map<String, String> otrosRiesgos,
            Date now,
            String user
    ) {
    }
}
