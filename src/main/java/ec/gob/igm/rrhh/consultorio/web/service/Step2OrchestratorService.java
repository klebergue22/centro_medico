package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.Date;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgoDet;
import ec.gob.igm.rrhh.consultorio.service.FichaRiesgoDetService;
import ec.gob.igm.rrhh.consultorio.service.FichaRiesgoService;
import ec.gob.igm.rrhh.consultorio.web.audit.CentroMedicoAuditService;

@Stateless
/**
 * Class Step2OrchestratorService: orquesta la lógica de presentación y flujo web.
 */
public class Step2OrchestratorService implements Serializable {

    @EJB
    private FichaRiesgoService fichaRiesgoService;
    @EJB
    private FichaRiesgoDetService fichaRiesgoDetService;
    @EJB
    private RiskDetailMapper riskDetailMapper;
    @EJB
    private UserContextService userContextService;
    @EJB
    private CentroMedicoAuditService auditService;

    public FichaRiesgo save(Step2RiskCommand cmd) {
        ensureFichaSavedOrThrow(cmd.ficha());

        String user = userContextService.resolveCurrentUser();
        FichaRiesgo riesgo = upsertRiskHeader(cmd, user);
        int totalDetalles = replaceRiskDetails(cmd, user);
        registrarAuditoriaStep2(cmd, riesgo, totalDetalles, user);

        return riesgo;
    }

    private void ensureFichaSavedOrThrow(FichaOcupacional ficha) {
        if (ficha == null || ficha.getIdFicha() == null) {
            throw new IllegalArgumentException("Primero debe existir y estar guardada la ficha (ID_FICHA).");
        }
    }

    private FichaRiesgo upsertRiskHeader(Step2RiskCommand cmd, String user) {
        FichaRiesgo riesgo = cmd.fichaRiesgo() != null ? cmd.fichaRiesgo() : new FichaRiesgo();
        riesgo.setFicha(cmd.ficha());
        riesgo.setPuestoTrabajo(cmd.ficha() != null ? cmd.ficha().getCiiu() : null);

        riskDetailMapper.mapRiskActivitiesToHeader(riesgo, cmd.actividadesLab());
        riesgo.setMedidasPreventivas(riskDetailMapper.construirMedidas(cmd.medidasPreventivas()));
        stampAuditFieldsForRiskHeader(riesgo, cmd.now(), user);

        return fichaRiesgoService.guardar(riesgo, user);
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

    private int replaceRiskDetails(Step2RiskCommand cmd, String user) {
        fichaRiesgoDetService.eliminarPorFicha(cmd.ficha().getIdFicha());
        int total = 0;
        for (FichaRiesgoDet det : riskDetailMapper.mapCheckedRiskItems(cmd.ficha(), cmd.riesgos())) {
            fichaRiesgoDetService.guardar(det, user);
            total++;
        }
        for (FichaRiesgoDet det : riskDetailMapper.mapOtherRiskItems(cmd.ficha(), cmd.otrosRiesgos())) {
            fichaRiesgoDetService.guardar(det, user);
            total++;
        }
        return total;
    }

    private void registrarAuditoriaStep2(Step2RiskCommand cmd, FichaRiesgo riesgo, int totalDetalles, String user) {
        Long idFicha = cmd.ficha() != null ? cmd.ficha().getIdFicha() : null;
        auditService.registrar(
                "GUARDAR_STEP2",
                "FICHA_RIESGO",
                "ID_FICHA_RIESGO",
                "ID_FICHA=" + idFicha + ", ID_FICHA_RIESGO="
                        + (riesgo != null ? riesgo.getIdFichaRiesgo() : null)
                        + ", DETALLES_RIESGO=" + totalDetalles,
                user
        );
        auditService.registrar(
                "UPSERT_DETALLE_RIESGO",
                "FICHA_RIESGO_DET",
                "ID_FICHA",
                "ID_FICHA=" + idFicha + ", TOTAL=" + totalDetalles,
                user
        );
    }

    public record Step2RiskCommand(
            FichaOcupacional ficha,
            FichaRiesgo fichaRiesgo,
            java.util.List<String> actividadesLab,
            java.util.List<String> medidasPreventivas,
            java.util.Map<String, Boolean> riesgos,
            java.util.Map<String, String> otrosRiesgos,
            Date now
    ) {
    }
}
