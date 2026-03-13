package ec.gob.igm.rrhh.consultorio.web.service;

import static ec.gob.igm.rrhh.consultorio.web.util.CentroMedicoViewUtils.safe;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import org.slf4j.Logger;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.CertificadoPdfTemplateService;
import ec.gob.igm.rrhh.consultorio.web.pdf.CentroMedicoPdfCommandFactory;
import ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfContextAssembler;
import ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfPlaceholderAssembler;
import ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfPlaceholderAssembler.FichaState;
import ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfTemplateService;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;
import ec.gob.igm.rrhh.consultorio.web.session.PdfSessionStore;

@Stateless
public class CentroMedicoPdfFacadeService implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private CentroMedicoPdfWorkflowService centroMedicoPdfWorkflowService;

    public CentroMedicoPdfWorkflowService.PrepareFichaCommandData buildPrepareFichaCommand(BuildPrepareFichaCommand cmd) {
        return new CentroMedicoPdfWorkflowService.PrepareFichaCommandData(
                cmd.ficha,
                cmd.empleadoSel,
                cmd.personaAux,
                cmd.permitirIngresoManual,
                cmd.asegurarPersonaAuxPersistida,
                cmd.htmlFichaSupplier,
                cmd.centroMedicoPdfFacade);
    }

    public CentroMedicoPdfWorkflowService.PrepareCertificadoCommandData buildPrepareCertificadoCommand(BuildPrepareCertificadoCommand cmd) {
        return new CentroMedicoPdfWorkflowService.PrepareCertificadoCommandData(
                cmd.ficha,
                cmd.verificarFichaCompleta,
                () -> centroMedicoPdfWorkflowService.construirHtmlDesdePlantillaUnchecked(cmd.htmlCertificadoSupplier),
                cmd.fechaEmisionSetter,
                cmd.centroMedicoPdfFacade);
    }

    public String construirHtmlFichaDesdePlantilla(FichaTemplateCommand cmd) {
        try {
            return cmd.fichaPdfTemplateService.construirHtmlFichaDesdePlantilla(
                    cmd.fichaPdfPlaceholderBuilder,
                    () -> cmd.pdfResourceResolver.readPdfTemplate("plantilla_ficha.html"),
                    cmd.syncCamposDesdeObjetos,
                    () -> buildReemplazosFichaInternal(cmd),
                    cmd.obtenerTipoEvaluacionPdf,
                    cmd.centroMedicoPdfFacade);
        } catch (RuntimeException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            cmd.log.error("[FICHA] Error cargando plantilla_ficha.html", cause);
            return "<html><body><h3>Error cargando plantilla_ficha.html</h3><pre>"
                    + safe(cause.getMessage()) + "</pre></body></html>";
        } catch (Exception e) {
            cmd.log.error("[FICHA] Error cargando plantilla_ficha.html", e);
            return "<html><body><h3>Error cargando plantilla_ficha.html</h3><pre>"
                    + safe(e.getMessage()) + "</pre></body></html>";
        }
    }

    public String construirHtmlDesdePlantilla(CertificadoTemplateCommand cmd) throws IOException {
        String template = cmd.pdfResourceResolver.readPdfTemplate("plantilla_certificado.html");

        Date f = (cmd.ficha != null && cmd.ficha.getFechaEmision() != null)
                ? cmd.ficha.getFechaEmision()
                : ((cmd.fechaEmision != null) ? cmd.fechaEmision : new Date());

        String aApto = "&nbsp;", aObs = "&nbsp;", aLim = "&nbsp;", aNo = "&nbsp;";
        if (cmd.aptitudSel != null) {
            switch (cmd.aptitudSel) {
                case "APTO":
                    aApto = "X";
                    break;
                case "APTO_EN_OBS":
                    aObs = "X";
                    break;
                case "APTO_LIMIT":
                    aLim = "X";
                    break;
                case "NO_APTO":
                    aNo = "X";
                    break;
                default:
                    break;
            }
        }

        String tipoEvaluacion = cmd.tipoEvaluacion;
        if (cmd.tipoEval != null && (tipoEvaluacion == null || tipoEvaluacion.isEmpty())) {
            tipoEvaluacion = cmd.tipoEval;
        }

        String chkIngreso = "&nbsp;", chkPeriodico = "&nbsp;", chkReintegro = "&nbsp;", chkRetiro = "&nbsp;";
        if (tipoEvaluacion != null) {
            switch (tipoEvaluacion.toUpperCase()) {
                case "INGRESO":
                    chkIngreso = "X";
                    break;
                case "PERIODICO":
                case "PERIÓDICO":
                    chkPeriodico = "X";
                    break;
                case "REINTEGRO":
                    chkReintegro = "X";
                    break;
                case "RETIRO":
                    chkRetiro = "X";
                    break;
                default:
                    break;
            }
        }

        CertificadoPdfTemplateService.CertificadoTemplateData data = new CertificadoPdfTemplateService.CertificadoTemplateData();
        data.template = template;
        data.fechaEmision = f;
        data.apto = aApto;
        data.obs = aObs;
        data.lim = aLim;
        data.noApto = aNo;
        data.chkIngreso = chkIngreso;
        data.chkPeriodico = chkPeriodico;
        data.chkReintegro = chkReintegro;
        data.chkRetiro = chkRetiro;
        data.logoIgm = cmd.pdfResourceResolver.resolveImageUrl("LOGO_IGM_FULL_COLOR.png");
        data.logoMidena = cmd.pdfResourceResolver.resolveImageUrl("LOGO_MIDENA.png");
        data.institucion = safe(cmd.institucion);
        data.ruc = safe(cmd.ruc);
        data.noHistoria = safe(cmd.noHistoria);
        data.noArchivo = safe(cmd.noArchivo);
        data.centroTrabajo = safe(cmd.centroTrabajo);
        data.ciiu = safe(cmd.ciiu);
        data.apellido1 = safe(cmd.apellido1);
        data.apellido2 = safe(cmd.apellido2);
        data.nombre1 = safe(cmd.nombre1);
        data.nombre2 = safe(cmd.nombre2);
        data.sexo = safe(cmd.sexo);
        data.detalleObservaciones = safe(cmd.detalleObservaciones);
        data.recomendaciones = safe(cmd.recomendaciones);
        data.medicoNombre = safe(cmd.medicoNombre);
        data.medicoCodigo = safe(cmd.medicoCodigo);
        data.templateEngine = cmd.pdfTemplateEngine;
        return cmd.certificadoPdfTemplateService.construirHtmlDesdePlantilla(data);
    }

    public void showValidationMessage(FacesContext ctx, String summary, List<String> errors) {
        if (ctx == null || errors == null || errors.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String error : errors) {
            sb.append("- ").append(error).append("\n");
        }
        ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, sb.toString()));
    }

    public void cleanupPdfPreview(FacesContext ctx, PdfSessionStore pdfSessionStore, String token) {
        centroMedicoPdfWorkflowService.cleanupPdfPreview(
                new CentroMedicoPdfWorkflowService.CleanupPdfPreviewCommand(ctx, pdfSessionStore, token));
    }

    public PdfPreviewState cleanupPdfPreviewState() {
        return new PdfPreviewState(false, null, null);
    }

    private Map<String, String> buildReemplazosFichaInternal(FichaTemplateCommand cmd) {
        final Map<String, String> snapshot = new LinkedHashMap<>();
        return cmd.fichaPdfTemplateService.buildReemplazosFicha(
                cmd.recalcularIMC,
                () -> cmd.cargarAtencionPrioritaria.accept(snapshot),
                () -> cmd.cargarActividadLaboralArrays.accept(snapshot),
                () -> snapshot,
                cmd.fichaPdfPlaceholderAssembler,
                () -> buildFichaStateInternal(cmd));
    }

    private FichaState buildFichaStateInternal(FichaTemplateCommand cmd) {
        return cmd.fichaPdfContextAssembler.buildFichaState(
                cmd.source,
                cmd.centroMedicoPdfFacade,
                cmd.log,
                cmd.fichaPdfViewModelBuilder,
                cmd.fichaPdfContextAssembler.buildFichaPdfViewModelContext(cmd.source),
                cmd.fallbackObservacionSupplier.get(),
                cmd.getSafe,
                cmd.toDate);
    }

    public static class BuildPrepareFichaCommand {
        public FichaOcupacional ficha;
        public DatEmpleado empleadoSel;
        public PersonaAux personaAux;
        public boolean permitirIngresoManual;
        public Runnable asegurarPersonaAuxPersistida;
        public Supplier<String> htmlFichaSupplier;
        public CentroMedicoPdfFacade centroMedicoPdfFacade;
    }

    public static class BuildPrepareCertificadoCommand {
        public FichaOcupacional ficha;
        public Supplier<Boolean> verificarFichaCompleta;
        public CentroMedicoPdfCommandFactory.ThrowingSupplier<String> htmlCertificadoSupplier;
        public Consumer<Date> fechaEmisionSetter;
        public CentroMedicoPdfFacade centroMedicoPdfFacade;
    }

    public static class FichaTemplateCommand {
        public Object source;
        public Logger log;
        public FichaPdfTemplateService fichaPdfTemplateService;
        public FichaPdfPlaceholderBuilder fichaPdfPlaceholderBuilder;
        public PdfResourceResolver pdfResourceResolver;
        public FichaPdfPlaceholderAssembler fichaPdfPlaceholderAssembler;
        public FichaPdfContextAssembler fichaPdfContextAssembler;
        public FichaPdfDataMapper fichaPdfDataMapper;
        public FichaPdfViewModelBuilder fichaPdfViewModelBuilder;
        public CentroMedicoPdfFacade centroMedicoPdfFacade;
        public Runnable syncCamposDesdeObjetos;
        public Supplier<String> obtenerTipoEvaluacionPdf;
        public Runnable recalcularIMC;
        public Consumer<Map<String, String>> cargarAtencionPrioritaria;
        public Consumer<Map<String, String>> cargarActividadLaboralArrays;
        public Supplier<String> fallbackObservacionSupplier;
        public java.util.function.BiFunction<List<?>, Integer, Object> getSafe;
        public Function<Object, Date> toDate;
    }

    public static class CertificadoTemplateCommand {
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

    public static class PdfPreviewState {
        public final boolean certificadoListo;
        public final String pdfTokenCertificado;
        public final String pdfObjectUrl;

        public PdfPreviewState(boolean certificadoListo, String pdfTokenCertificado, String pdfObjectUrl) {
            this.certificadoListo = certificadoListo;
            this.pdfTokenCertificado = pdfTokenCertificado;
            this.pdfObjectUrl = pdfObjectUrl;
        }
    }
}
