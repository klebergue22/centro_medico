package ec.gob.igm.rrhh.consultorio.web.facade;

import java.io.Serializable;
import java.util.function.Consumer;

import org.slf4j.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.web.ctrl.ControllerActionTemplate;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfTemplateCoordinator;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfWorkflowService;
import ec.gob.igm.rrhh.consultorio.web.mapper.PdfCertificadoInputAssembler;
import ec.gob.igm.rrhh.consultorio.web.mapper.PdfFichaInputAssembler;
import ec.gob.igm.rrhh.consultorio.web.session.PdfSessionStore;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;
import ec.gob.igm.rrhh.consultorio.web.pdf.CertificadoPdfTemplateService;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PdfPreviewState;

@ApplicationScoped
public class PdfPreviewFacade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PdfSectionFacade pdfSectionFacade;

    public void prepararVistaPreviaFicha(PrepareVistaPreviaFichaCommand cmd) {
        cmd.controllerActionTemplate.executeWithResult(
                "onPrepararFichaPdf",
                () -> pdfSectionFacade.onPrepararFichaPdf(
                        new CentroMedicoPdfWorkflowService.PrepareFichaFlowCommand(buildPrepareFichaCommand(cmd))),
                result -> pdfSectionFacade.onPrepararFichaPdfSuccess(
                        result,
                        FacesContext.getCurrentInstance(),
                        cmd.pdfPreviewState,
                        cmd.fichaSetter,
                        cmd.activeStepSetter,
                        cmd.mostrarDlgCedulaSetter),
                cmd.log,
                cmd.activeStep,
                cmd.noPersonaSel,
                cmd.cedulaBusqueda);
    }

    public void prepararVistaPreviaCertificado(PrepareVistaPreviaCertificadoCommand cmd) {
        cmd.controllerActionTemplate.executeWithResult(
                "onPrepararCertificadoPdf",
                () -> pdfSectionFacade.onPrepararCertificadoPdf(
                        new CentroMedicoPdfWorkflowService.PrepareCertificadoFlowCommand(
                                cmd.pdfPreviewState.isFichaPdfListo(),
                                cmd.pdfPreviewState.getPdfTokenFicha(),
                                buildPrepareFichaCommand(cmd.base),
                                buildPrepareCertificadoCommand(cmd.base))),
                result -> pdfSectionFacade.applyPdfUiState(
                        pdfSectionFacade.onPrepararCertificadoPdfSuccess(
                                result,
                                FacesContext.getCurrentInstance(),
                                cmd.pdfSessionStore,
                                cmd.pdfPreviewState.getPdfTokenCertificado()),
                        cmd.pdfPreviewState,
                        cmd.base.fichaSetter,
                        cmd.base.activeStepSetter,
                        cmd.base.mostrarDlgCedulaSetter),
                cmd.base.log,
                cmd.base.activeStep,
                cmd.base.noPersonaSel,
                cmd.base.cedulaBusqueda);
    }

    public void prepararVistaPrevia(PrepareVistaPreviaCommand cmd) {
        cmd.controllerActionTemplate.executeWithResult(
                "prepararVistaPrevia",
                () -> pdfSectionFacade.prepararVistaPrevia(
                        new CentroMedicoPdfWorkflowService.PreparePreviewFlowCommand(
                                cmd.pdfPreviewState.isFichaPdfListo(),
                                cmd.pdfPreviewState.getPdfTokenFicha(),
                                cmd.verificarFichaCompleta,
                                buildPrepareFichaCommand(cmd.base),
                                buildPrepareCertificadoCommand(cmd.base))),
                result -> pdfSectionFacade.applyPdfUiState(
                        pdfSectionFacade.onPrepararVistaPreviaSuccess(
                                result,
                                FacesContext.getCurrentInstance(),
                                cmd.pdfSessionStore,
                                cmd.pdfPreviewState.getPdfTokenCertificado()),
                        cmd.pdfPreviewState,
                        cmd.base.fichaSetter,
                        cmd.base.activeStepSetter,
                        cmd.base.mostrarDlgCedulaSetter),
                cmd.base.log,
                cmd.base.activeStep,
                cmd.base.noPersonaSel,
                cmd.base.cedulaBusqueda);
    }

    public void limpiarVistaPrevia(LimpiarVistaPreviaCommand cmd) {
        pdfSectionFacade.cleanupPdfPreview(
                FacesContext.getCurrentInstance(),
                cmd.pdfSessionStore,
                cmd.pdfPreviewState.getPdfTokenCertificado());
        pdfSectionFacade.applyCleanupPdfPreviewState(
                cmd.pdfPreviewState,
                cmd.fichaSetter,
                cmd.activeStepSetter,
                cmd.mostrarDlgCedulaSetter);
    }


    public CentroMedicoPdfWorkflowService.PrepareFichaCommandData buildPrepareFichaCommandData(BasePrepareCommand cmd) {
        return buildPrepareFichaCommand(cmd);
    }

    public CentroMedicoPdfWorkflowService.PrepareCertificadoCommandData buildPrepareCertificadoCommandData(BasePrepareCommand cmd) {
        return buildPrepareCertificadoCommand(cmd);
    }

    public void resetStep4PdfState(PdfPreviewState pdfPreviewState,
                                   Consumer<FichaOcupacional> fichaSetter,
                                   Consumer<String> activeStepSetter,
                                   Consumer<Boolean> mostrarDlgCedulaSetter) {
        pdfSectionFacade.resetStep4PdfState(pdfPreviewState, fichaSetter, activeStepSetter, mostrarDlgCedulaSetter);
    }

    private CentroMedicoPdfWorkflowService.PrepareFichaCommandData buildPrepareFichaCommand(BasePrepareCommand cmd) {
        return pdfSectionFacade.buildPrepareFichaCommand(
                cmd.centroMedicoPdfTemplateCoordinator,
                pdfSectionFacade.capturePdfFichaViewData(
                        cmd.pdfFichaInputAssembler.capture(
                                cmd.controllerRef,
                                cmd.log,
                                cmd.asegurarPersonaAuxPersistida,
                                cmd.syncCamposDesdeObjetosInternal,
                                cmd.recalcularIMC,
                                cmd.centroMedicoPdfFacade,
                                cmd.pdfResourceResolver,
                                cmd.hRows)));
    }

    private CentroMedicoPdfWorkflowService.PrepareCertificadoCommandData buildPrepareCertificadoCommand(BasePrepareCommand cmd) {
        return pdfSectionFacade.buildPrepareCertificadoCommand(
                cmd.centroMedicoPdfTemplateCoordinator,
                pdfSectionFacade.capturePdfCertificadoViewData(
                        cmd.pdfCertificadoInputAssembler.capture(
                                cmd.controllerRef,
                                cmd.verificarFichaCompleta,
                                cmd.setFechaEmision,
                                cmd.centroMedicoPdfFacade,
                                cmd.pdfResourceResolver,
                                cmd.pdfTemplateEngine,
                                cmd.certificadoPdfTemplateService)));
    }

    public static class BasePrepareCommand {
        public final Object controllerRef;
        public final Logger log;
        public final String activeStep;
        public final Integer noPersonaSel;
        public final String cedulaBusqueda;
        public final Runnable asegurarPersonaAuxPersistida;
        public final Runnable syncCamposDesdeObjetosInternal;
        public final Runnable recalcularIMC;
        public final Runnable verificarFichaCompleta;
        public final Consumer<java.util.Date> setFechaEmision;
        public final int hRows;
        public final PdfFichaInputAssembler pdfFichaInputAssembler;
        public final PdfCertificadoInputAssembler pdfCertificadoInputAssembler;
        public final CentroMedicoPdfTemplateCoordinator centroMedicoPdfTemplateCoordinator;
        public final CentroMedicoPdfFacade centroMedicoPdfFacade;
        public final PdfResourceResolver pdfResourceResolver;
        public final PdfTemplateEngine pdfTemplateEngine;
        public final CertificadoPdfTemplateService certificadoPdfTemplateService;
        public final Consumer<FichaOcupacional> fichaSetter;
        public final Consumer<String> activeStepSetter;
        public final Consumer<Boolean> mostrarDlgCedulaSetter;

        public BasePrepareCommand(
                Object controllerRef,
                Logger log,
                String activeStep,
                Integer noPersonaSel,
                String cedulaBusqueda,
                Runnable asegurarPersonaAuxPersistida,
                Runnable syncCamposDesdeObjetosInternal,
                Runnable recalcularIMC,
                Runnable verificarFichaCompleta,
                Consumer<java.util.Date> setFechaEmision,
                int hRows,
                PdfFichaInputAssembler pdfFichaInputAssembler,
                PdfCertificadoInputAssembler pdfCertificadoInputAssembler,
                CentroMedicoPdfTemplateCoordinator centroMedicoPdfTemplateCoordinator,
                CentroMedicoPdfFacade centroMedicoPdfFacade,
                PdfResourceResolver pdfResourceResolver,
                PdfTemplateEngine pdfTemplateEngine,
                CertificadoPdfTemplateService certificadoPdfTemplateService,
                Consumer<FichaOcupacional> fichaSetter,
                Consumer<String> activeStepSetter,
                Consumer<Boolean> mostrarDlgCedulaSetter) {
            this.controllerRef = controllerRef;
            this.log = log;
            this.activeStep = activeStep;
            this.noPersonaSel = noPersonaSel;
            this.cedulaBusqueda = cedulaBusqueda;
            this.asegurarPersonaAuxPersistida = asegurarPersonaAuxPersistida;
            this.syncCamposDesdeObjetosInternal = syncCamposDesdeObjetosInternal;
            this.recalcularIMC = recalcularIMC;
            this.verificarFichaCompleta = verificarFichaCompleta;
            this.setFechaEmision = setFechaEmision;
            this.hRows = hRows;
            this.pdfFichaInputAssembler = pdfFichaInputAssembler;
            this.pdfCertificadoInputAssembler = pdfCertificadoInputAssembler;
            this.centroMedicoPdfTemplateCoordinator = centroMedicoPdfTemplateCoordinator;
            this.centroMedicoPdfFacade = centroMedicoPdfFacade;
            this.pdfResourceResolver = pdfResourceResolver;
            this.pdfTemplateEngine = pdfTemplateEngine;
            this.certificadoPdfTemplateService = certificadoPdfTemplateService;
            this.fichaSetter = fichaSetter;
            this.activeStepSetter = activeStepSetter;
            this.mostrarDlgCedulaSetter = mostrarDlgCedulaSetter;
        }
    }

    public static class PrepareVistaPreviaFichaCommand extends BasePrepareCommand {
        public final ControllerActionTemplate controllerActionTemplate;
        public final PdfPreviewState pdfPreviewState;

        public PrepareVistaPreviaFichaCommand(ControllerActionTemplate controllerActionTemplate,
                                              PdfPreviewState pdfPreviewState,
                                              BasePrepareCommand base) {
            super(base.controllerRef, base.log, base.activeStep, base.noPersonaSel, base.cedulaBusqueda,
                    base.asegurarPersonaAuxPersistida, base.syncCamposDesdeObjetosInternal, base.recalcularIMC,
                    base.verificarFichaCompleta, base.setFechaEmision, base.hRows, base.pdfFichaInputAssembler,
                    base.pdfCertificadoInputAssembler, base.centroMedicoPdfTemplateCoordinator, base.centroMedicoPdfFacade,
                    base.pdfResourceResolver, base.pdfTemplateEngine, base.certificadoPdfTemplateService,
                    base.fichaSetter, base.activeStepSetter, base.mostrarDlgCedulaSetter);
            this.controllerActionTemplate = controllerActionTemplate;
            this.pdfPreviewState = pdfPreviewState;
        }
    }

    public static class PrepareVistaPreviaCertificadoCommand {
        public final ControllerActionTemplate controllerActionTemplate;
        public final PdfPreviewState pdfPreviewState;
        public final PdfSessionStore pdfSessionStore;
        public final BasePrepareCommand base;

        public PrepareVistaPreviaCertificadoCommand(ControllerActionTemplate controllerActionTemplate,
                                                    PdfPreviewState pdfPreviewState,
                                                    PdfSessionStore pdfSessionStore,
                                                    BasePrepareCommand base) {
            this.controllerActionTemplate = controllerActionTemplate;
            this.pdfPreviewState = pdfPreviewState;
            this.pdfSessionStore = pdfSessionStore;
            this.base = base;
        }
    }

    public static class PrepareVistaPreviaCommand {
        public final ControllerActionTemplate controllerActionTemplate;
        public final PdfPreviewState pdfPreviewState;
        public final PdfSessionStore pdfSessionStore;
        public final Runnable verificarFichaCompleta;
        public final BasePrepareCommand base;

        public PrepareVistaPreviaCommand(ControllerActionTemplate controllerActionTemplate,
                                         PdfPreviewState pdfPreviewState,
                                         PdfSessionStore pdfSessionStore,
                                         Runnable verificarFichaCompleta,
                                         BasePrepareCommand base) {
            this.controllerActionTemplate = controllerActionTemplate;
            this.pdfPreviewState = pdfPreviewState;
            this.pdfSessionStore = pdfSessionStore;
            this.verificarFichaCompleta = verificarFichaCompleta;
            this.base = base;
        }
    }

    public static class LimpiarVistaPreviaCommand {
        public final PdfSessionStore pdfSessionStore;
        public final PdfPreviewState pdfPreviewState;
        public final Consumer<FichaOcupacional> fichaSetter;
        public final Consumer<String> activeStepSetter;
        public final Consumer<Boolean> mostrarDlgCedulaSetter;

        public LimpiarVistaPreviaCommand(PdfSessionStore pdfSessionStore,
                                         PdfPreviewState pdfPreviewState,
                                         Consumer<FichaOcupacional> fichaSetter,
                                         Consumer<String> activeStepSetter,
                                         Consumer<Boolean> mostrarDlgCedulaSetter) {
            this.pdfSessionStore = pdfSessionStore;
            this.pdfPreviewState = pdfPreviewState;
            this.fichaSetter = fichaSetter;
            this.activeStepSetter = activeStepSetter;
            this.mostrarDlgCedulaSetter = mostrarDlgCedulaSetter;
        }
    }
}
