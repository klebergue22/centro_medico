package ec.gob.igm.rrhh.consultorio.web.facade;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfWorkflowService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfFacadeService;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfControllerSupport;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfTemplateCoordinator;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfUiCoordinator;
import ec.gob.igm.rrhh.consultorio.web.session.PdfSessionStore;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PdfPreviewState;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PdfCertificadoViewData;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PdfFichaViewData;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoWizardNavigationCoordinator;

@ApplicationScoped
public class PdfSectionFacade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CentroMedicoPdfWorkflowService centroMedicoPdfWorkflowService;
    @Inject
    private CentroMedicoPdfControllerSupport centroMedicoPdfControllerSupport;
    @Inject
    private CentroMedicoPdfUiCoordinator centroMedicoPdfUiCoordinator;
    @Inject
    private CentroMedicoPdfFacadeService centroMedicoPdfFacadeService;

    public PdfFichaViewData capturePdfFichaViewData(
            CentroMedicoPdfControllerSupport.CapturePdfFichaInput input) {
        return centroMedicoPdfControllerSupport.capturePdfFichaViewData(input);
    }

    public PdfCertificadoViewData capturePdfCertificadoViewData(
            CentroMedicoPdfControllerSupport.CapturePdfCertificadoInput input) {
        return centroMedicoPdfControllerSupport.capturePdfCertificadoViewData(input);
    }

    public CentroMedicoPdfWorkflowService.FichaFlowResult onPrepararFichaPdf(CentroMedicoPdfWorkflowService.PrepareFichaFlowCommand cmd) {
        return centroMedicoPdfWorkflowService.onPrepararFichaPdf(cmd);
    }

    public CentroMedicoPdfWorkflowService.CertificadoFlowResult onPrepararCertificadoPdf(CentroMedicoPdfWorkflowService.PrepareCertificadoFlowCommand cmd) {
        return centroMedicoPdfWorkflowService.onPrepararCertificadoPdf(cmd);
    }

    public CentroMedicoPdfWorkflowService.PreviewFlowResult prepararVistaPrevia(CentroMedicoPdfWorkflowService.PreparePreviewFlowCommand cmd) {
        return centroMedicoPdfWorkflowService.prepararVistaPrevia(cmd);
    }

    public void onPrepararFichaPdfSuccess(CentroMedicoPdfWorkflowService.FichaFlowResult result,
                                          FacesContext ctx,
                                          PdfPreviewState state,
                                          Consumer<FichaOcupacional> fichaSetter,
                                          Consumer<String> activeStepSetter,
                                          Consumer<Boolean> mostrarDlgCedulaSetter) {
        centroMedicoPdfControllerSupport.onPrepararFichaPdfSuccess(result, ctx, state, fichaSetter, activeStepSetter, mostrarDlgCedulaSetter);
    }

    public CentroMedicoPdfWorkflowService.PrepareFichaCommandData buildPrepareFichaCommand(CentroMedicoPdfTemplateCoordinator coordinator,
                                                                                           PdfFichaViewData data) {
        return centroMedicoPdfControllerSupport.buildPrepareFichaCommand(coordinator, data);
    }

    public CentroMedicoPdfWorkflowService.PrepareCertificadoCommandData buildPrepareCertificadoCommand(CentroMedicoPdfTemplateCoordinator coordinator,
                                                                                                        PdfCertificadoViewData data) {
        return centroMedicoPdfControllerSupport.buildPrepareCertificadoCommand(coordinator, data);
    }

    public CentroMedicoPdfUiCoordinator.PdfUiState onPrepararCertificadoPdfSuccess(CentroMedicoPdfWorkflowService.CertificadoFlowResult result,
                                                                                    FacesContext ctx,
                                                                                    PdfSessionStore pdfSessionStore,
                                                                                    String currentCertToken) {
        return centroMedicoPdfUiCoordinator.onPrepararCertificadoPdfSuccess(result, ctx, pdfSessionStore, currentCertToken);
    }

    public CentroMedicoPdfUiCoordinator.PdfUiState onPrepararVistaPreviaSuccess(CentroMedicoPdfWorkflowService.PreviewFlowResult result,
                                                                                 FacesContext ctx,
                                                                                 PdfSessionStore pdfSessionStore,
                                                                                 String currentCertToken) {
        return centroMedicoPdfUiCoordinator.onPrepararVistaPreviaSuccess(result, ctx, pdfSessionStore, currentCertToken);
    }

    public void applyPdfUiState(CentroMedicoPdfUiCoordinator.PdfUiState state,
                                PdfPreviewState pdfPreviewState,
                                Consumer<FichaOcupacional> fichaSetter,
                                Consumer<String> activeStepSetter,
                                Consumer<Boolean> mostrarDlgCedulaSetter) {
        centroMedicoPdfControllerSupport.applyPdfUiState(
                centroMedicoPdfUiCoordinator,
                state,
                pdfPreviewState,
                fichaSetter,
                activeStepSetter,
                mostrarDlgCedulaSetter);
    }

    public void resetStep4PdfState(PdfPreviewState pdfPreviewState,
                                   Consumer<FichaOcupacional> fichaSetter,
                                   Consumer<String> activeStepSetter,
                                   Consumer<Boolean> mostrarDlgCedulaSetter) {
        centroMedicoPdfControllerSupport.resetStep4PdfState(
                centroMedicoPdfUiCoordinator,
                pdfPreviewState,
                fichaSetter,
                activeStepSetter,
                mostrarDlgCedulaSetter);
    }

    public void applyStep4State(CentroMedicoWizardNavigationCoordinator.Step4UiState state,
                                PdfPreviewState pdfPreviewState,
                                Consumer<FichaOcupacional> fichaSetter,
                                Consumer<String> activeStepSetter,
                                Consumer<Boolean> mostrarDlgCedulaSetter) {
        centroMedicoPdfControllerSupport.applyStep4State(
                centroMedicoPdfUiCoordinator,
                state,
                pdfPreviewState,
                fichaSetter,
                activeStepSetter,
                mostrarDlgCedulaSetter);
    }

    public void applyCleanupPdfPreviewState(PdfPreviewState pdfPreviewState,
                                            Consumer<FichaOcupacional> fichaSetter,
                                            Consumer<String> activeStepSetter,
                                            Consumer<Boolean> mostrarDlgCedulaSetter) {
        centroMedicoPdfControllerSupport.applyCleanupPdfPreviewState(
                centroMedicoPdfUiCoordinator,
                pdfPreviewState,
                fichaSetter,
                activeStepSetter,
                mostrarDlgCedulaSetter);
    }

    public void cleanupPdfPreview(FacesContext ctx, PdfSessionStore pdfSessionStore, String token) {
        centroMedicoPdfFacadeService.cleanupPdfPreview(ctx, pdfSessionStore, token);
    }

    public String obtenerTipoEvaluacionPdf(String tipoEval, String tipoEvaluacion) {
        return centroMedicoPdfControllerSupport.obtenerTipoEvaluacionPdf(tipoEval, tipoEvaluacion);
    }

    public void showValidationMessage(FacesContext ctx, String summary, List<String> errors) {
        centroMedicoPdfControllerSupport.showValidationMessage(ctx, summary, errors);
    }

    public CentroMedicoPdfUiCoordinator.PdfUiState cleanupPdfPreviewState(FacesContext ctx,
            PdfSessionStore pdfSessionStore, String currentToken) {
        return centroMedicoPdfUiCoordinator.cleanupPdfPreview(ctx, pdfSessionStore, currentToken);
    }

    public void syncCamposDesdeObjetosInternal(
            ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfControllerSupport.SyncCamposDesdeObjetosInput input) {
        centroMedicoPdfControllerSupport.syncCamposDesdeObjetosInternal(input);
    }

    public void cargarAtencionPrioritaria(FichaOcupacional ficha,
                                          boolean discapacidad,
                                          boolean catastrofica,
                                          boolean embarazada,
                                          boolean lactancia,
                                          boolean adultoMayor,
                                          java.util.Map<String, String> rep,
                                          org.slf4j.Logger log) {
        centroMedicoPdfControllerSupport.cargarAtencionPrioritaria(
                ficha,
                discapacidad,
                catastrofica,
                embarazada,
                lactancia,
                adultoMayor,
                rep,
                log);
    }

    public void cargarActividadLaboralArrays(int hRows,
                                             List<String> actLabCentroTrabajo,
                                             List<String> actLabActividad,
                                             List<String> actLabTiempo,
                                             List<Boolean> actLabTrabajoAnterior,
                                             List<Boolean> actLabTrabajoActual,
                                             List<Boolean> actLabIncidenteChk,
                                             List<Boolean> actLabAccidenteChk,
                                             List<Boolean> actLabEnfermedadChk,
                                             List<Boolean> iessSi,
                                             List<Boolean> iessNo,
                                             List<?> iessFecha,
                                             List<String> iessEspecificar,
                                             List<String> actLabObservaciones,
                                             java.util.Map<String, String> rep) {
        centroMedicoPdfControllerSupport.cargarActividadLaboralArrays(
                hRows, actLabCentroTrabajo, actLabActividad, actLabTiempo, actLabTrabajoAnterior, actLabTrabajoActual,
                actLabIncidenteChk, actLabAccidenteChk, actLabEnfermedadChk, iessSi, iessNo, iessFecha,
                iessEspecificar, actLabObservaciones, rep);
    }

}
