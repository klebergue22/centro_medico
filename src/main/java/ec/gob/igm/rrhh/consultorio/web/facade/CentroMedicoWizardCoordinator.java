package ec.gob.igm.rrhh.consultorio.web.facade;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfWorkflowService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoWizardNavigationCoordinator;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PdfPreviewState;

@ApplicationScoped
public class CentroMedicoWizardCoordinator implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String STEP4_WIZARD_UPDATE_TARGET = "@([id$=wdzFicha])";

    @Inject
    private WizardSectionFacade wizardSectionFacade;

    @Inject
    private PdfPreviewFacade pdfPreviewFacade;

    public void guardarStepActual(GuardarStepActualCoordinatorCommand cmd) {
        wizardSectionFacade.guardarStepActual(
                new CentroMedicoWizardFacade.GuardarStepActualCommand(
                        cmd.activeStep,
                        cmd.guardarStep1,
                        cmd.guardarStep2,
                        cmd.guardarStep3,
                        cmd.onActiveStepChange,
                        STEP4_WIZARD_UPDATE_TARGET,
                        () -> pdfPreviewFacade.resetStep4PdfState(
                                cmd.pdfPreviewState,
                                cmd.fichaSetter,
                                cmd.onActiveStepChange,
                                cmd.mostrarDlgCedulaSetter),
                        cmd.onApplyStep4Result,
                        cmd.ficha,
                        () -> buildPrepareFichaCommand(cmd.basePrepareCommandSupplier),
                        () -> buildPrepareCertificadoCommand(cmd.basePrepareCommandSupplier)));
    }

    private CentroMedicoPdfWorkflowService.PrepareFichaCommandData buildPrepareFichaCommand(
            Supplier<PdfPreviewFacade.BasePrepareCommand> basePrepareCommandSupplier) {
        return pdfPreviewFacade.buildPrepareFichaCommandData(basePrepareCommandSupplier.get());
    }

    private CentroMedicoPdfWorkflowService.PrepareCertificadoCommandData buildPrepareCertificadoCommand(
            Supplier<PdfPreviewFacade.BasePrepareCommand> basePrepareCommandSupplier) {
        return pdfPreviewFacade.buildPrepareCertificadoCommandData(basePrepareCommandSupplier.get());
    }

    public static class GuardarStepActualCoordinatorCommand {
        public final String activeStep;
        public final Runnable guardarStep1;
        public final Runnable guardarStep2;
        public final Runnable guardarStep3;
        public final Consumer<String> onActiveStepChange;
        public final Consumer<CentroMedicoWizardNavigationCoordinator.Step4UiState> onApplyStep4Result;
        public final FichaOcupacional ficha;
        public final PdfPreviewState pdfPreviewState;
        public final Consumer<FichaOcupacional> fichaSetter;
        public final Consumer<Boolean> mostrarDlgCedulaSetter;
        public final Supplier<PdfPreviewFacade.BasePrepareCommand> basePrepareCommandSupplier;

        public GuardarStepActualCoordinatorCommand(
                String activeStep,
                Runnable guardarStep1,
                Runnable guardarStep2,
                Runnable guardarStep3,
                Consumer<String> onActiveStepChange,
                Consumer<CentroMedicoWizardNavigationCoordinator.Step4UiState> onApplyStep4Result,
                FichaOcupacional ficha,
                PdfPreviewState pdfPreviewState,
                Consumer<FichaOcupacional> fichaSetter,
                Consumer<Boolean> mostrarDlgCedulaSetter,
                Supplier<PdfPreviewFacade.BasePrepareCommand> basePrepareCommandSupplier) {
            this.activeStep = activeStep;
            this.guardarStep1 = guardarStep1;
            this.guardarStep2 = guardarStep2;
            this.guardarStep3 = guardarStep3;
            this.onActiveStepChange = onActiveStepChange;
            this.onApplyStep4Result = onApplyStep4Result;
            this.ficha = ficha;
            this.pdfPreviewState = pdfPreviewState;
            this.fichaSetter = fichaSetter;
            this.mostrarDlgCedulaSetter = mostrarDlgCedulaSetter;
            this.basePrepareCommandSupplier = basePrepareCommandSupplier;
        }
    }
}
