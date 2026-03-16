package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.CentroMedicoPdfCommandFactory;
import ec.gob.igm.rrhh.consultorio.web.pdf.CertificadoPdfTemplateService;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoWizardNavigationCoordinator.Step4UiState;
import ec.gob.igm.rrhh.consultorio.web.session.PdfSessionStore;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PdfPreviewState;

@ApplicationScoped
/**
 * Class CentroMedicoPdfUiCoordinator: orquesta la lógica de presentación y flujo web.
 */
public class CentroMedicoPdfUiCoordinator implements Serializable {

    private static final long serialVersionUID = 1L;

    @jakarta.inject.Inject
    private CentroMedicoPdfFacadeService centroMedicoPdfFacadeService;

    public CentroMedicoPdfWorkflowService.PrepareFichaCommandData buildPrepareFichaCommand(BuildPrepareFichaUiCommand cmd) {
        CentroMedicoPdfFacadeService.BuildPrepareFichaCommand facadeCmd = new CentroMedicoPdfFacadeService.BuildPrepareFichaCommand();
        facadeCmd.ficha = cmd.ficha;
        facadeCmd.empleadoSel = cmd.empleadoSel;
        facadeCmd.personaAux = cmd.personaAux;
        facadeCmd.permitirIngresoManual = cmd.permitirIngresoManual;
        facadeCmd.asegurarPersonaAuxPersistida = cmd.asegurarPersonaAuxPersistida;
        facadeCmd.htmlFichaSupplier = cmd.htmlFichaSupplier;
        facadeCmd.centroMedicoPdfFacade = cmd.centroMedicoPdfFacade;
        return centroMedicoPdfFacadeService.buildPrepareFichaCommand(facadeCmd);
    }

    public CentroMedicoPdfWorkflowService.PrepareCertificadoCommandData buildPrepareCertificadoCommand(BuildPrepareCertificadoUiCommand cmd) {
        CentroMedicoPdfFacadeService.BuildPrepareCertificadoCommand facadeCmd = new CentroMedicoPdfFacadeService.BuildPrepareCertificadoCommand();
        facadeCmd.ficha = cmd.ficha;
        facadeCmd.verificarFichaCompleta = cmd.verificarFichaCompleta;
        facadeCmd.htmlCertificadoSupplier = cmd.htmlCertificadoSupplier;
        facadeCmd.fechaEmisionSetter = cmd.fechaEmisionSetter;
        facadeCmd.centroMedicoPdfFacade = cmd.centroMedicoPdfFacade;
        return centroMedicoPdfFacadeService.buildPrepareCertificadoCommand(facadeCmd);
    }

    public PdfUiState onPrepararVistaPreviaSuccess(CentroMedicoPdfWorkflowService.PreviewFlowResult result,
                                                    FacesContext ctx,
                                                    PdfSessionStore pdfSessionStore,
                                                    String pdfTokenCertificadoActual) {
        if (result == null) {
            return null;
        }

        PdfUiState state = applyFichaPreviewResult(result.fichaResult, new PdfUiState());
        if (!result.listo) {
            state.certificadoListo = false;
            return cleanupPdfPreview(ctx, pdfSessionStore, pdfTokenCertificadoActual, state);
        }

        state.pdfTokenCertificado = result.certificadoToken;
        state.certificadoListo = true;
        if (ctx != null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO, "PDF listo",
                    "Se generó el certificado para vista previa y descarga."));
        }
        return state;
    }

    public PdfUiState onPrepararCertificadoPdfSuccess(CentroMedicoPdfWorkflowService.CertificadoFlowResult result,
                                                       FacesContext ctx,
                                                       PdfSessionStore pdfSessionStore,
                                                       String pdfTokenCertificadoActual) {
        if (result == null) {
            return null;
        }

        PdfUiState state = applyFichaPreviewResult(result.fichaResult, new PdfUiState());
        if (!result.listo) {
            state = cleanupPdfPreview(ctx, pdfSessionStore, pdfTokenCertificadoActual, state);
            if (ctx != null) {
                centroMedicoPdfFacadeService.showValidationMessage(ctx, "Validación antes de generar el certificado", result.errores);
            }
            return state;
        }

        state.certificadoListo = true;
        state.pdfTokenCertificado = result.token;
        state.activeStep = "step4";
        state.mostrarDlgCedula = false;

        if (ctx != null) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "PDF Certificado listo",
                    "Se generó el certificado para vista previa y descarga."));
        }
        return state;
    }

    public PdfUiState cleanupPdfPreview(FacesContext ctx, PdfSessionStore pdfSessionStore, String token) {
        return cleanupPdfPreview(ctx, pdfSessionStore, token, new PdfUiState());
    }

    public PdfUiState cleanupPdfPreview(FacesContext ctx, PdfSessionStore pdfSessionStore, String token, PdfUiState baseState) {
        centroMedicoPdfFacadeService.cleanupPdfPreview(ctx, pdfSessionStore, token);
        return applyCleanupPdfPreviewState(baseState);
    }

    public PdfUiState applyCleanupPdfPreviewState(PdfUiState baseState) {
        PdfUiState state = baseState != null ? baseState : new PdfUiState();
        CentroMedicoPdfFacadeService.PdfPreviewState cleanupState = centroMedicoPdfFacadeService.cleanupPdfPreviewState();
        state.certificadoListo = cleanupState.certificadoListo;
        state.pdfTokenCertificado = cleanupState.pdfTokenCertificado;
        state.pdfObjectUrl = cleanupState.pdfObjectUrl;
        return state;
    }

    public PdfUiState applyFichaPreviewResult(CentroMedicoPdfWorkflowService.FichaFlowResult fichaResult, PdfUiState state) {
        PdfUiState next = state != null ? state : new PdfUiState();
        if (fichaResult == null || !fichaResult.listo) {
            return next;
        }
        next.ficha = fichaResult.ficha;
        next.fichaPdfListo = true;
        next.pdfTokenFicha = fichaResult.token;
        return next;
    }

    public PdfUiState resetStep4PdfState() {
        PdfUiState state = new PdfUiState();
        state.fichaPdfListo = false;
        state.certificadoListo = false;
        state.pdfTokenFicha = null;
        state.pdfTokenCertificado = null;
        return state;
    }

    public PdfUiState applyStep4State(Step4UiState step4State, PdfUiState currentState) {
        PdfUiState state = currentState != null ? currentState : new PdfUiState();
        if (step4State == null) {
            return state;
        }

        if (step4State.fichaPdfListo) {
            state.ficha = step4State.ficha;
            state.pdfTokenFicha = step4State.pdfTokenFicha;
            state.fichaPdfListo = true;
        }

        if (step4State.certificadoListo) {
            state.pdfTokenCertificado = step4State.pdfTokenCertificado;
            state.certificadoListo = true;
        }
        return state;
    }

    public String construirHtmlDesdePlantilla(ConstruirCertificadoHtmlCommand cmd) throws IOException {
        CentroMedicoPdfFacadeService.CertificadoTemplateCommand facadeCmd = new CentroMedicoPdfFacadeService.CertificadoTemplateCommand();
        facadeCmd.ficha = cmd.ficha;
        facadeCmd.fechaEmision = cmd.fechaEmision;
        facadeCmd.aptitudSel = cmd.aptitudSel;
        facadeCmd.tipoEval = cmd.tipoEval;
        facadeCmd.tipoEvaluacion = cmd.tipoEvaluacion;
        facadeCmd.institucion = cmd.institucion;
        facadeCmd.ruc = cmd.ruc;
        facadeCmd.noHistoria = cmd.noHistoria;
        facadeCmd.noArchivo = cmd.noArchivo;
        facadeCmd.centroTrabajo = cmd.centroTrabajo;
        facadeCmd.ciiu = cmd.ciiu;
        facadeCmd.apellido1 = cmd.apellido1;
        facadeCmd.apellido2 = cmd.apellido2;
        facadeCmd.nombre1 = cmd.nombre1;
        facadeCmd.nombre2 = cmd.nombre2;
        facadeCmd.sexo = cmd.sexo;
        facadeCmd.detalleObservaciones = cmd.detalleObservaciones;
        facadeCmd.recomendaciones = cmd.recomendaciones;
        facadeCmd.medicoNombre = cmd.medicoNombre;
        facadeCmd.medicoCodigo = cmd.medicoCodigo;
        facadeCmd.pdfResourceResolver = cmd.pdfResourceResolver;
        facadeCmd.pdfTemplateEngine = cmd.pdfTemplateEngine;
        facadeCmd.certificadoPdfTemplateService = cmd.certificadoPdfTemplateService;
        return centroMedicoPdfFacadeService.construirHtmlDesdePlantilla(facadeCmd);
    }

    public void applyPdfUiState(PdfUiState state,
                                PdfPreviewState pdfPreviewState,
                                Consumer<FichaOcupacional> fichaSetter,
                                Consumer<Boolean> fichaPdfListoSetter,
                                Consumer<String> pdfTokenFichaSetter,
                                Consumer<Boolean> certificadoListoSetter,
                                Consumer<String> pdfTokenCertificadoSetter,
                                Consumer<String> pdfObjectUrlSetter,
                                Consumer<String> activeStepSetter,
                                Consumer<Boolean> mostrarDlgCedulaSetter) {
        if (state == null) {
            return;
        }
        if (state.ficha != null) {
            fichaSetter.accept(state.ficha);
        }
        if (state.fichaPdfListo != null) {
            fichaPdfListoSetter.accept(state.fichaPdfListo);
            if (pdfPreviewState != null) {
                pdfPreviewState.setFichaPdfListo(state.fichaPdfListo);
            }
        }
        if (state.pdfTokenFicha != null || Boolean.TRUE.equals(state.fichaPdfListo)) {
            pdfTokenFichaSetter.accept(state.pdfTokenFicha);
            if (pdfPreviewState != null) {
                pdfPreviewState.setPdfTokenFicha(state.pdfTokenFicha);
            }
        }
        if (state.certificadoListo != null) {
            certificadoListoSetter.accept(state.certificadoListo);
            if (pdfPreviewState != null) {
                pdfPreviewState.setCertificadoListo(state.certificadoListo);
            }
        }
        if (state.pdfTokenCertificado != null || Boolean.FALSE.equals(state.certificadoListo)) {
            pdfTokenCertificadoSetter.accept(state.pdfTokenCertificado);
            if (pdfPreviewState != null) {
                pdfPreviewState.setPdfTokenCertificado(state.pdfTokenCertificado);
            }
        }
        if (state.pdfObjectUrl != null || Boolean.FALSE.equals(state.certificadoListo)) {
            pdfObjectUrlSetter.accept(state.pdfObjectUrl);
            if (pdfPreviewState != null) {
                pdfPreviewState.setPdfObjectUrl(state.pdfObjectUrl);
            }
        }
        if (state.activeStep != null) {
            activeStepSetter.accept(state.activeStep);
        }
        if (state.mostrarDlgCedula != null) {
            mostrarDlgCedulaSetter.accept(state.mostrarDlgCedula);
        }
    }

    public static class BuildPrepareFichaUiCommand {
        public FichaOcupacional ficha;
        public DatEmpleado empleadoSel;
        public PersonaAux personaAux;
        public boolean permitirIngresoManual;
        public Runnable asegurarPersonaAuxPersistida;
        public Supplier<String> htmlFichaSupplier;
        public CentroMedicoPdfFacade centroMedicoPdfFacade;
    }

    public static class BuildPrepareCertificadoUiCommand {
        public FichaOcupacional ficha;
        public Supplier<Boolean> verificarFichaCompleta;
        public CentroMedicoPdfCommandFactory.ThrowingSupplier<String> htmlCertificadoSupplier;
        public Consumer<Date> fechaEmisionSetter;
        public CentroMedicoPdfFacade centroMedicoPdfFacade;
    }

    public static class ConstruirCertificadoHtmlCommand {
        public FichaOcupacional ficha;
        public Date fechaEmision;
        public String aptitudSel;
        public String tipoEval;
        public String tipoEvaluacion;
        public String institucion;
        public String ruc;
        public String noHistoria;
        public String noArchivo;
        public String centroTrabajo;
        public String ciiu;
        public String apellido1;
        public String apellido2;
        public String nombre1;
        public String nombre2;
        public String sexo;
        public String detalleObservaciones;
        public String recomendaciones;
        public String medicoNombre;
        public String medicoCodigo;
        public PdfResourceResolver pdfResourceResolver;
        public PdfTemplateEngine pdfTemplateEngine;
        public CertificadoPdfTemplateService certificadoPdfTemplateService;
    }

    public static class PdfUiState {
        public FichaOcupacional ficha;
        public Boolean fichaPdfListo;
        public Boolean certificadoListo;
        public String pdfObjectUrl;
        public String pdfTokenFicha;
        public String pdfTokenCertificado;
        public String activeStep;
        public Boolean mostrarDlgCedula;
    }
}
