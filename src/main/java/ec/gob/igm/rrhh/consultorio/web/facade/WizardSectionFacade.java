package ec.gob.igm.rrhh.consultorio.web.facade;

import java.io.Serializable;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoValidationCoordinator;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoValidationCoordinator.FichaCompletaValidationInput;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoValidationCoordinator.Step1ValidationInput;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoWizardNavigationCoordinator;
import ec.gob.igm.rrhh.consultorio.web.service.Step2OrchestratorService;
import ec.gob.igm.rrhh.consultorio.web.service.Step2OrchestratorService.Step2RiskCommand;
import ec.gob.igm.rrhh.consultorio.web.service.Step3OrchestratorService;
import ec.gob.igm.rrhh.consultorio.web.service.Step3OrchestratorService.Step3SaveCommand;
import ec.gob.igm.rrhh.consultorio.web.service.ValidationUiResult;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WizardSectionFacade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CentroMedicoWizardNavigationCoordinator wizardNavigationCoordinator;
    @Inject
    private CentroMedicoWizardFacade centroMedicoWizardFacade;
    @Inject
    private CentroMedicoValidationCoordinator validationCoordinator;
    @Inject
    private Step1Facade step1Facade;
    @Inject
    private AuditFacade auditFacade;
    @EJB
    private Step2OrchestratorService step2OrchestratorService;
    @EJB
    private Step3OrchestratorService step3OrchestratorService;

    public String retrocederStep(String activeStep) { return wizardNavigationCoordinator.retrocederStep(activeStep); }
    public void guardarStepActual(CentroMedicoWizardFacade.GuardarStepActualCommand cmd) { centroMedicoWizardFacade.guardarStepActual(cmd); }

    public ValidationUiResult validarStep1(Step1ValidationInput input) { return validationCoordinator.validarStep1(input); }
    public ValidationUiResult validarStep2(FichaRiesgo fichaRiesgo, java.util.List<String> actividadesLab, java.util.List<String> medidasPreventivas, boolean requerirDetalle) {
        return validationCoordinator.validarStep2(fichaRiesgo, actividadesLab, medidasPreventivas, requerirDetalle);
    }
    public ValidationUiResult validarStep3(java.util.List<ConsultaDiagnostico> listaDiag, String aptitudSel, String recomendaciones, String medicoNombre, String medicoCodigo) {
        return validationCoordinator.validarStep3(listaDiag, aptitudSel, recomendaciones, medicoNombre, medicoCodigo);
    }
    public ValidationUiResult verificarFichaCompleta(FichaCompletaValidationInput input) { return validationCoordinator.verificarFichaCompleta(input); }

    public Step1Facade.SaveStep1Result guardarStep1(Step1Facade.SaveStep1Command cmd) { return step1Facade.guardar(cmd); }
    public FichaRiesgo guardarStep2(Step2RiskCommand cmd) { return step2OrchestratorService.save(cmd); }
    public FichaOcupacional guardarStep3(Step3SaveCommand command) { return step3OrchestratorService.saveStep3(command); }

    public void registrarAuditoria(String accion, String entidad, String recurso, String detalle) {
        auditFacade.registrar(accion, entidad, recurso, detalle);
    }
}
