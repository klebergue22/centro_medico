package ec.gob.igm.rrhh.consultorio.web.facade;

import java.io.Serializable;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import ec.gob.igm.rrhh.consultorio.web.ctrl.ControllerActionTemplate;
import ec.gob.igm.rrhh.consultorio.web.mapper.PdfCertificadoInputAssembler;
import ec.gob.igm.rrhh.consultorio.web.mapper.PdfFichaInputAssembler;
import ec.gob.igm.rrhh.consultorio.web.pdf.CertificadoPdfTemplateService;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoPdfTemplateCoordinator;
import ec.gob.igm.rrhh.consultorio.web.session.PdfSessionStore;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PdfPreviewState;

@ApplicationScoped
public class PdfPreviewCommandFactory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PdfFichaInputAssembler pdfFichaInputAssembler;
    @Inject
    private PdfCertificadoInputAssembler pdfCertificadoInputAssembler;
    @Inject
    private CentroMedicoPdfTemplateCoordinator centroMedicoPdfTemplateCoordinator;
    @Inject
    private CentroMedicoPdfFacade centroMedicoPdfFacade;
    @Inject
    private PdfResourceResolver pdfResourceResolver;
    @Inject
    private PdfTemplateEngine pdfTemplateEngine;
    @Inject
    private CertificadoPdfTemplateService certificadoPdfTemplateService;

    public PdfPreviewFacade.PrepareVistaPreviaFichaCommand buildPrepareVistaPreviaFichaCommand(PdfCommandContext context) {
        return new PdfPreviewFacade.PrepareVistaPreviaFichaCommand(
                context.controllerActionTemplate,
                context.pdfPreviewState,
                buildBasePrepareCommand(context));
    }

    public PdfPreviewFacade.PrepareVistaPreviaCertificadoCommand buildPrepareVistaPreviaCertificadoCommand(PdfCommandContext context) {
        return new PdfPreviewFacade.PrepareVistaPreviaCertificadoCommand(
                context.controllerActionTemplate,
                context.pdfPreviewState,
                context.pdfSessionStore,
                buildBasePrepareCommand(context));
    }

    public PdfPreviewFacade.PrepareVistaPreviaCommand buildPrepareVistaPreviaCommand(PdfCommandContext context) {
        return new PdfPreviewFacade.PrepareVistaPreviaCommand(
                context.controllerActionTemplate,
                context.pdfPreviewState,
                context.pdfSessionStore,
                context.verificarFichaCompleta,
                buildBasePrepareCommand(context));
    }

    public PdfPreviewFacade.BasePrepareCommand buildBasePrepareCommand(PdfCommandContext context) {
        return new PdfPreviewFacade.BasePrepareCommand(
                context.controllerRef,
                context.log,
                context.activeStep,
                context.noPersonaSel,
                context.cedulaBusqueda,
                context.asegurarPersonaAuxPersistida,
                context.syncCamposDesdeObjetosInternal,
                context.recalcularIMC,
                context.verificarFichaCompleta,
                context.setFechaEmision,
                context.hRows,
                pdfFichaInputAssembler,
                pdfCertificadoInputAssembler,
                centroMedicoPdfTemplateCoordinator,
                centroMedicoPdfFacade,
                pdfResourceResolver,
                pdfTemplateEngine,
                certificadoPdfTemplateService,
                context.fichaSetter,
                context.activeStepSetter,
                context.mostrarDlgCedulaSetter);
    }

    public static class PdfCommandContext {
        public final ControllerActionTemplate controllerActionTemplate;
        public final PdfPreviewState pdfPreviewState;
        public final PdfSessionStore pdfSessionStore;
        public final CentroMedicoCtrl controllerRef;
        public final Logger log;
        public final String activeStep;
        public final Integer noPersonaSel;
        public final String cedulaBusqueda;
        public final Runnable asegurarPersonaAuxPersistida;
        public final Runnable syncCamposDesdeObjetosInternal;
        public final Runnable recalcularIMC;
        public final Supplier<Boolean> verificarFichaCompleta;
        public final Consumer<Date> setFechaEmision;
        public final int hRows;
        public final Consumer<FichaOcupacional> fichaSetter;
        public final Consumer<String> activeStepSetter;
        public final Consumer<Boolean> mostrarDlgCedulaSetter;

        public PdfCommandContext(ControllerActionTemplate controllerActionTemplate,
                                 PdfPreviewState pdfPreviewState,
                                 PdfSessionStore pdfSessionStore,
                                 CentroMedicoCtrl controllerRef,
                                 Logger log,
                                 String activeStep,
                                 Integer noPersonaSel,
                                 String cedulaBusqueda,
                                 Runnable asegurarPersonaAuxPersistida,
                                 Runnable syncCamposDesdeObjetosInternal,
                                 Runnable recalcularIMC,
                                 Supplier<Boolean> verificarFichaCompleta,
                                 Consumer<Date> setFechaEmision,
                                 int hRows,
                                 Consumer<FichaOcupacional> fichaSetter,
                                 Consumer<String> activeStepSetter,
                                 Consumer<Boolean> mostrarDlgCedulaSetter) {
            this.controllerActionTemplate = controllerActionTemplate;
            this.pdfPreviewState = pdfPreviewState;
            this.pdfSessionStore = pdfSessionStore;
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
            this.fichaSetter = fichaSetter;
            this.activeStepSetter = activeStepSetter;
            this.mostrarDlgCedulaSetter = mostrarDlgCedulaSetter;
        }
    }
}
