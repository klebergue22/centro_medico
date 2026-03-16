package ec.gob.igm.rrhh.consultorio.web.facade;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.primefaces.PrimeFaces;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfWorkflowService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoWizardNavigationCoordinator;

@ApplicationScoped
/**
 * Class CentroMedicoWizardFacade: expone una fachada para simplificar operaciones del módulo web.
 */
public class CentroMedicoWizardFacade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CentroMedicoStepFlowFacade centroMedicoStepFlowFacade;

    public void guardarStepActual(GuardarStepActualCommand cmd) {
        centroMedicoStepFlowFacade.guardarStepActual(
                new CentroMedicoStepFlowFacade.GuardarStepActualFacadeCommand(
                        cmd.activeStep,
                        cmd.guardarStep1,
                        cmd.guardarStep2,
                        cmd.guardarStep3,
                        cmd.onActiveStepChange,
                        () -> PrimeFaces.current().ajax().update(cmd.step4WizardUpdateTarget),
                        cmd.onResetPdfState,
                        cmd.onApplyStep4Result,
                        cmd.ficha,
                        cmd.prepareFichaCommandSupplier,
                        cmd.prepareCertificadoCommandSupplier));
    }

    public static class GuardarStepActualCommand {
        public final String activeStep;
        public final Runnable guardarStep1;
        public final Runnable guardarStep2;
        public final Runnable guardarStep3;
        public final Consumer<String> onActiveStepChange;
        public final String step4WizardUpdateTarget;
        public final Runnable onResetPdfState;
        public final Consumer<CentroMedicoWizardNavigationCoordinator.Step4UiState> onApplyStep4Result;
        public final FichaOcupacional ficha;
        public final Supplier<CentroMedicoPdfWorkflowService.PrepareFichaCommandData> prepareFichaCommandSupplier;
        public final Supplier<CentroMedicoPdfWorkflowService.PrepareCertificadoCommandData> prepareCertificadoCommandSupplier;

        public GuardarStepActualCommand(
                String activeStep,
                Runnable guardarStep1,
                Runnable guardarStep2,
                Runnable guardarStep3,
                Consumer<String> onActiveStepChange,
                String step4WizardUpdateTarget,
                Runnable onResetPdfState,
                Consumer<CentroMedicoWizardNavigationCoordinator.Step4UiState> onApplyStep4Result,
                FichaOcupacional ficha,
                Supplier<CentroMedicoPdfWorkflowService.PrepareFichaCommandData> prepareFichaCommandSupplier,
                Supplier<CentroMedicoPdfWorkflowService.PrepareCertificadoCommandData> prepareCertificadoCommandSupplier) {
            this.activeStep = activeStep;
            this.guardarStep1 = guardarStep1;
            this.guardarStep2 = guardarStep2;
            this.guardarStep3 = guardarStep3;
            this.onActiveStepChange = onActiveStepChange;
            this.step4WizardUpdateTarget = step4WizardUpdateTarget;
            this.onResetPdfState = onResetPdfState;
            this.onApplyStep4Result = onApplyStep4Result;
            this.ficha = ficha;
            this.prepareFichaCommandSupplier = prepareFichaCommandSupplier;
            this.prepareCertificadoCommandSupplier = prepareCertificadoCommandSupplier;
        }
    }
}
