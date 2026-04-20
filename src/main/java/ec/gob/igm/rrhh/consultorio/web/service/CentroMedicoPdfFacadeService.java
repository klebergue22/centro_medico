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
/**
 * Class CentroMedicoPdfFacadeService: orquesta la lÃ³gica de presentaciÃ³n y flujo web.
 */
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
            Throwable cause = resolveMeaningfulCause(ex);
            cmd.log.error("[FICHA] Error cargando plantilla_ficha.html. Cadena de causas: {}", buildCauseChain(ex), cause);
            return "<html><body><h3>Error cargando plantilla_ficha.html</h3><pre>"
                    + safe(cause.getMessage()) + "</pre></body></html>";
        } catch (Exception e) {
            cmd.log.error("[FICHA] Error cargando plantilla_ficha.html", e);
            return "<html><body><h3>Error cargando plantilla_ficha.html</h3><pre>"
                    + safe(e.getMessage()) + "</pre></body></html>";
        }
    }

    private Throwable resolveMeaningfulCause(Throwable throwable) {
        Throwable current = throwable;
        Throwable previous = null;
        while (current != null && current != previous) {
            previous = current;
            current = current.getCause();
        }
        return previous != null ? previous : throwable;
    }

    private String buildCauseChain(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        Throwable current = throwable;
        int depth = 0;
        while (current != null && depth < 12) {
            if (depth > 0) {
                sb.append(" -> ");
            }
            sb.append(current.getClass().getSimpleName());
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                sb.append(": ").append(current.getMessage());
            }
            current = current.getCause();
            depth++;
        }
        if (current != null) {
            sb.append(" -> ...");
        }
        return sb.toString();
    }

    public String construirHtmlDesdePlantilla(CertificadoTemplateCommand cmd) throws IOException {
        String template = cmd.pdfResourceResolver.readPdfTemplate("plantilla_certificado.html");
        CertificadoPdfTemplateService.CertificadoTemplateData data = buildCertificadoTemplateData(
                cmd,
                template,
                resolveFechaEmision(cmd),
                buildAptitudChecks(cmd.aptitudSel),
                buildTipoEvaluacionChecks(resolveTipoEvaluacion(cmd)));
        return cmd.certificadoPdfTemplateService.construirHtmlDesdePlantilla(data);
    }

    private Date resolveFechaEmision(CertificadoTemplateCommand cmd) {
        if (cmd.ficha != null && cmd.ficha.getFechaEmision() != null) {
            return cmd.ficha.getFechaEmision();
        }
        return cmd.fechaEmision != null ? cmd.fechaEmision : new Date();
    }

    private String resolveTipoEvaluacion(CertificadoTemplateCommand cmd) {
        String tipoEvaluacion = cmd.tipoEvaluacion;
        if (cmd.tipoEval != null && (tipoEvaluacion == null || tipoEvaluacion.isEmpty())) {
            return cmd.tipoEval;
        }
        return tipoEvaluacion;
    }

    private AptitudChecks buildAptitudChecks(String aptitudSel) {
        if (aptitudSel == null) {
            return new AptitudChecks("", "", "", "");
        }
        switch (aptitudSel) {
            case "APTO":
                return new AptitudChecks("X", "", "", "");
            case "APTO_EN_OBS":
                return new AptitudChecks("", "X", "", "");
            case "APTO_LIMIT":
                return new AptitudChecks("", "", "X", "");
            case "NO_APTO":
                return new AptitudChecks("", "", "", "X");
            default:
                return new AptitudChecks("", "", "", "");
        }
    }

    private TipoEvaluacionChecks buildTipoEvaluacionChecks(String tipoEvaluacion) {
        if (tipoEvaluacion == null) {
            return new TipoEvaluacionChecks("", "", "", "");
        }
        switch (tipoEvaluacion.toUpperCase()) {
            case "INGRESO":
                return new TipoEvaluacionChecks("X", "", "", "");
            case "PERIODICO":
            case "PERIÃ“DICO":
                return new TipoEvaluacionChecks("", "X", "", "");
            case "REINTEGRO":
                return new TipoEvaluacionChecks("", "", "X", "");
            case "RETIRO":
                return new TipoEvaluacionChecks("", "", "", "X");
            default:
                return new TipoEvaluacionChecks("", "", "", "");
        }
    }

    private CertificadoPdfTemplateService.CertificadoTemplateData buildCertificadoTemplateData(
            CertificadoTemplateCommand cmd,
            String template,
            Date fechaEmision,
            AptitudChecks aptitudChecks,
            TipoEvaluacionChecks tipoEvaluacionChecks) {
        CertificadoPdfTemplateService.CertificadoTemplateData data = new CertificadoPdfTemplateService.CertificadoTemplateData();
        data.template = template;
        data.fechaEmision = fechaEmision;
        applyAptitudChecks(data, aptitudChecks);
        applyTipoEvaluacionChecks(data, tipoEvaluacionChecks);
        applyBrandingData(cmd, data);
        applyPacienteData(cmd, data);
        applyProfesionalData(cmd, data);
        data.templateEngine = cmd.pdfTemplateEngine;
        return data;
    }

    private void applyAptitudChecks(CertificadoPdfTemplateService.CertificadoTemplateData data, AptitudChecks checks) {
        data.apto = checks.apto;
        data.obs = checks.obs;
        data.lim = checks.lim;
        data.noApto = checks.noApto;
    }

    private void applyTipoEvaluacionChecks(
            CertificadoPdfTemplateService.CertificadoTemplateData data,
            TipoEvaluacionChecks checks) {
        data.chkIngreso = checks.ingreso;
        data.chkPeriodico = checks.periodico;
        data.chkReintegro = checks.reintegro;
        data.chkRetiro = checks.retiro;
    }

    private void applyBrandingData(
            CertificadoTemplateCommand cmd,
            CertificadoPdfTemplateService.CertificadoTemplateData data) {
        data.logoIgm = cmd.pdfResourceResolver.resolveImageUrl("LOGO_IGM_FULL_COLOR.png");
        data.logoMidena = cmd.pdfResourceResolver.resolveImageUrl("LOGO_MIDENA.png");
        data.institucion = safe(cmd.institucion);
        data.ruc = safe(cmd.ruc);
        data.noHistoria = safe(firstNotBlank(
                cmd.noHistoria,
                cmd.ficha != null ? cmd.ficha.getNoHistoriaClinica() : null));
        data.noArchivo = safe(firstNotBlank(
                cmd.noArchivo,
                cmd.ficha != null ? cmd.ficha.getNoArchivo() : null));
    }

    private void applyPacienteData(
            CertificadoTemplateCommand cmd,
            CertificadoPdfTemplateService.CertificadoTemplateData data) {
        data.centroTrabajo = safe(firstNotBlank(
                cmd.centroTrabajo,
                cmd.ficha != null ? cmd.ficha.getEstablecimientoCt() : null));
        data.ciiu = safe(firstNotBlank(
                cmd.ciiu,
                cmd.ficha != null ? cmd.ficha.getCiiu() : null,
                cmd.ficha != null ? cmd.ficha.getPuestoTrabajoTxt() : null,
                cmd.ficha != null ? cmd.ficha.getAreaTrabajo() : null));
        data.apellido1 = safe(cmd.apellido1);
        data.apellido2 = safe(cmd.apellido2);
        data.nombre1 = safe(cmd.nombre1);
        data.nombre2 = safe(cmd.nombre2);
        data.sexo = safe(cmd.sexo);
        data.detalleObservaciones = safe(cmd.detalleObservaciones);
        data.recomendaciones = safe(cmd.recomendaciones);
    }

    private void applyProfesionalData(
            CertificadoTemplateCommand cmd,
            CertificadoPdfTemplateService.CertificadoTemplateData data) {
        data.medicoNombre = safe(cmd.medicoNombre);
        data.medicoCodigo = safe(cmd.medicoCodigo);
    }

    private String firstNotBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return "";
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
        centroMedicoPdfWorkflowService.limpiarVistaPrevia(
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

    private static final class AptitudChecks {
        private final String apto;
        private final String obs;
        private final String lim;
        private final String noApto;

        private AptitudChecks(String apto, String obs, String lim, String noApto) {
            this.apto = apto;
            this.obs = obs;
            this.lim = lim;
            this.noApto = noApto;
        }
    }

    private static final class TipoEvaluacionChecks {
        private final String ingreso;
        private final String periodico;
        private final String reintegro;
        private final String retiro;

        private TipoEvaluacionChecks(String ingreso, String periodico, String reintegro, String retiro) {
            this.ingreso = ingreso;
            this.periodico = periodico;
            this.reintegro = reintegro;
            this.retiro = retiro;
        }
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
