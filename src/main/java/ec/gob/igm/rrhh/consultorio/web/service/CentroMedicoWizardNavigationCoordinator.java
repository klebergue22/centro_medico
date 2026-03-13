package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;

@ApplicationScoped
public class CentroMedicoWizardNavigationCoordinator implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(CentroMedicoWizardNavigationCoordinator.class);

    @jakarta.inject.Inject
    private CentroMedicoWizardService centroMedicoWizardService;

    @jakarta.ejb.EJB
    private CentroMedicoPdfWorkflowService centroMedicoPdfWorkflowService;

    public void guardarStepActual(GuardarStepActualCommand cmd) {
        String next = centroMedicoWizardService.saveCurrentStepAndGetNext(
                cmd.activeStep,
                cmd.guardarStep1,
                cmd.guardarStep2,
                cmd.guardarStep3);

        if (!canTransition(next)) {
            return;
        }

        cmd.onActiveStepChange.accept(next);

        if ("step4".equals(next)) {
            onEnterStep4AutoRegenerar(cmd);
            cmd.onStep4EnteredUiSync.run();
        }
    }

    private boolean canTransition(String next) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return ctx != null && !ctx.isValidationFailed() && next != null;
    }

    private void onEnterStep4AutoRegenerar(GuardarStepActualCommand cmd) {
        try {
            cmd.onResetPdfState.run();

            CentroMedicoPdfWorkflowService.Step4FlowResult result = centroMedicoPdfWorkflowService.onEnterStep4AutoRegenerar(
                    new CentroMedicoPdfWorkflowService.Step4FlowCommand(
                            cmd.ficha,
                            cmd.prepareFichaCommand,
                            cmd.prepareCertificadoCommand));

            if (result == null || result.skipped || result.ficha == null) {
                return;
            }

            cmd.onApplyStep4Result.accept(new Step4UiState(
                    result.ficha,
                    result.fichaLista,
                    result.fichaToken,
                    result.certificadoListo,
                    result.certificadoToken));

        } catch (Exception ex) {
            LOG.warn("Auto-regeneración Step4 falló", ex);
        }
    }

    public String retrocederStep(String activeStep) {
        return centroMedicoWizardService.retrocederStep(activeStep);
    }

    public static class GuardarStepActualCommand {
        public final String activeStep;
        public final Runnable guardarStep1;
        public final Runnable guardarStep2;
        public final Runnable guardarStep3;
        public final Consumer<String> onActiveStepChange;
        public final Runnable onStep4EnteredUiSync;
        public final Runnable onResetPdfState;
        public final Consumer<Step4UiState> onApplyStep4Result;
        public final FichaOcupacional ficha;
        public final CentroMedicoPdfWorkflowService.PrepareFichaCommandData prepareFichaCommand;
        public final CentroMedicoPdfWorkflowService.PrepareCertificadoCommandData prepareCertificadoCommand;

        public GuardarStepActualCommand(
                String activeStep,
                Runnable guardarStep1,
                Runnable guardarStep2,
                Runnable guardarStep3,
                Consumer<String> onActiveStepChange,
                Runnable onStep4EnteredUiSync,
                Runnable onResetPdfState,
                Consumer<Step4UiState> onApplyStep4Result,
                FichaOcupacional ficha,
                CentroMedicoPdfWorkflowService.PrepareFichaCommandData prepareFichaCommand,
                CentroMedicoPdfWorkflowService.PrepareCertificadoCommandData prepareCertificadoCommand) {
            this.activeStep = activeStep;
            this.guardarStep1 = guardarStep1;
            this.guardarStep2 = guardarStep2;
            this.guardarStep3 = guardarStep3;
            this.onActiveStepChange = onActiveStepChange;
            this.onStep4EnteredUiSync = onStep4EnteredUiSync;
            this.onResetPdfState = onResetPdfState;
            this.onApplyStep4Result = onApplyStep4Result;
            this.ficha = ficha;
            this.prepareFichaCommand = prepareFichaCommand;
            this.prepareCertificadoCommand = prepareCertificadoCommand;
        }
    }

    public static class Step4UiState {
        public final FichaOcupacional ficha;
        public final boolean fichaPdfListo;
        public final String pdfTokenFicha;
        public final boolean certificadoListo;
        public final String pdfTokenCertificado;

        public Step4UiState(FichaOcupacional ficha,
                boolean fichaPdfListo,
                String pdfTokenFicha,
                boolean certificadoListo,
                String pdfTokenCertificado) {
            this.ficha = ficha;
            this.fichaPdfListo = fichaPdfListo;
            this.pdfTokenFicha = pdfTokenFicha;
            this.certificadoListo = certificadoListo;
            this.pdfTokenCertificado = pdfTokenCertificado;
        }
    }
}
