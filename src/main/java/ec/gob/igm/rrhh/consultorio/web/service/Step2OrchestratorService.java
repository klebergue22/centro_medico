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

    public FichaRiesgo save(Step2RiskCommand cmd) {
        ensureFichaSavedOrThrow(cmd.ficha());

        String user = userContextService.resolveCurrentUser();
        FichaRiesgo riesgo = upsertRiskHeader(cmd, user);
        replaceRiskDetails(cmd, user);

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

        riskDetailMapper.mapRiskActivitiesToHeader(riesgo, cmd.actividadesLab());
        riesgo.setMedidasPreventivas(riskDetailMapper.construirMedidas(cmd.medidasPreventivas()));
        stampAuditFieldsForRiskHeader(riesgo, cmd.now(), user);

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

    private void replaceRiskDetails(Step2RiskCommand cmd, String user) {
        fichaRiesgoDetService.eliminarPorFicha(cmd.ficha().getIdFicha());

        for (FichaRiesgoDet det : riskDetailMapper.mapCheckedRiskItems(cmd.ficha(), cmd.riesgos())) {
            fichaRiesgoDetService.guardar(det, user);
        }
        for (FichaRiesgoDet det : riskDetailMapper.mapOtherRiskItems(cmd.ficha(), cmd.otrosRiesgos())) {
            fichaRiesgoDetService.guardar(det, user);
        }
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
