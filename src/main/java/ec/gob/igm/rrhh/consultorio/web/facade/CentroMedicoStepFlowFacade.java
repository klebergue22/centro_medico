package ec.gob.igm.rrhh.consultorio.web.facade;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfWorkflowService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoWizardNavigationCoordinator;

@ApplicationScoped
public class CentroMedicoStepFlowFacade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CentroMedicoWizardNavigationCoordinator wizardNavigationCoordinator;

    public void guardarStepActual(GuardarStepActualFacadeCommand cmd) {
        wizardNavigationCoordinator.guardarStepActual(
                new CentroMedicoWizardNavigationCoordinator.GuardarStepActualCommand(
                        cmd.activeStep,
                        cmd.guardarStep1,
                        cmd.guardarStep2,
                        cmd.guardarStep3,
                        cmd.onActiveStepChange,
                        cmd.onStep4EnteredUiSync,
                        cmd.onResetPdfState,
                        cmd.onApplyStep4Result,
                        cmd.ficha,
                        cmd.prepareFichaCommandSupplier,
                        cmd.prepareCertificadoCommandSupplier));
    }

    public static class GuardarStepActualFacadeCommand {
        public final String activeStep;
        public final Runnable guardarStep1;
        public final Runnable guardarStep2;
        public final Runnable guardarStep3;
        public final Consumer<String> onActiveStepChange;
        public final Runnable onStep4EnteredUiSync;
        public final Runnable onResetPdfState;
        public final Consumer<CentroMedicoWizardNavigationCoordinator.Step4UiState> onApplyStep4Result;
        public final FichaOcupacional ficha;
        public final Supplier<CentroMedicoPdfWorkflowService.PrepareFichaCommandData> prepareFichaCommandSupplier;
        public final Supplier<CentroMedicoPdfWorkflowService.PrepareCertificadoCommandData> prepareCertificadoCommandSupplier;

        public GuardarStepActualFacadeCommand(
                String activeStep,
                Runnable guardarStep1,
                Runnable guardarStep2,
                Runnable guardarStep3,
                Consumer<String> onActiveStepChange,
                Runnable onStep4EnteredUiSync,
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
            this.onStep4EnteredUiSync = onStep4EnteredUiSync;
            this.onResetPdfState = onResetPdfState;
            this.onApplyStep4Result = onApplyStep4Result;
            this.ficha = ficha;
            this.prepareFichaCommandSupplier = prepareFichaCommandSupplier;
            this.prepareCertificadoCommandSupplier = prepareCertificadoCommandSupplier;
        }
    }
}
