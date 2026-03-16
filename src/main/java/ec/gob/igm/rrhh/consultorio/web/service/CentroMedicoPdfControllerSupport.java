package ec.gob.igm.rrhh.consultorio.web.service;

import static ec.gob.igm.rrhh.consultorio.web.util.CentroMedicoPdfValueUtil.safe;
import static ec.gob.igm.rrhh.consultorio.web.util.CentroMedicoViewUtils.isBlank;
import static ec.gob.igm.rrhh.consultorio.web.util.CentroMedicoViewUtils.isTrue;
import static ec.gob.igm.rrhh.consultorio.web.util.DateFormatUtil.fmtDate;
import static ec.gob.igm.rrhh.consultorio.web.util.ReflectionPropertyUtil.getFichaStringByReflection;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.CertificadoPdfTemplateService;
import ec.gob.igm.rrhh.consultorio.web.pdf.FichaPdfContextAssembler;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTextUtil;
import ec.gob.igm.rrhh.consultorio.web.util.CentroMedicoViewUtils;
import ec.gob.igm.rrhh.consultorio.web.util.DateFormatUtil;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PdfCertificadoViewData;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PdfFichaViewData;
import ec.gob.igm.rrhh.consultorio.web.viewstate.PdfPreviewState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

@ApplicationScoped
public class CentroMedicoPdfControllerSupport implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CentroMedicoPdfFacadeService centroMedicoPdfFacadeService;

    public PdfFichaViewData capturePdfFichaViewData(CapturePdfFichaInput input) {
        return new PdfFichaViewData(
                input.source,
                input.log,
                input.ficha,
                input.empleadoSel,
                input.personaAux,
                input.permitirIngresoManual,
                input.asegurarPersonaAuxPersistida,
                input.centroMedicoPdfFacade,
                input.pdfResourceResolver,
                input.syncCamposDesdeObjetos,
                () -> obtenerTipoEvaluacionPdf(input.tipoEval, input.tipoEvaluacion),
                input.recalcularIMC,
                rep -> cargarAtencionPrioritaria(input.ficha, input.apDiscapacidad, input.apCatastrofica,
                        input.apEmbarazada, input.apLactancia, input.apAdultoMayor, rep, input.log),
                rep -> cargarActividadLaboralArrays(input.hRows, input.actLabCentroTrabajo, input.actLabActividad,
                        input.actLabTiempo, input.actLabTrabajoAnterior, input.actLabTrabajoActual,
                        input.actLabIncidenteChk, input.actLabAccidenteChk, input.actLabEnfermedadChk,
                        input.iessSi, input.iessNo, input.iessFecha, input.iessEspecificar,
                        input.actLabObservaciones, rep),
                () -> getFichaStringByReflection(input.ficha,
                        "getDetalleObs",
                        "getDetalleObservaciones",
                        "getObservaciones",
                        "getObs",
                        "getObservacion"),
                CentroMedicoViewUtils::getSafe,
                DateFormatUtil::toDate);
    }

    public PdfCertificadoViewData capturePdfCertificadoViewData(CapturePdfCertificadoInput input) {
        return new PdfCertificadoViewData(
                input.ficha,
                input.verificarFichaCompleta,
                input.fechaEmisionSetter,
                input.centroMedicoPdfFacade,
                input.fechaEmision,
                input.aptitudSel,
                input.tipoEval,
                input.tipoEvaluacion,
                input.institucion,
                input.ruc,
                input.noHistoria,
                input.noArchivo,
                input.centroTrabajo,
                input.ciiu,
                input.apellido1,
                input.apellido2,
                input.nombre1,
                input.nombre2,
                input.sexo,
                input.detalleObservaciones,
                input.recomendaciones,
                input.medicoNombre,
                input.medicoCodigo,
                input.pdfResourceResolver,
                input.pdfTemplateEngine,
                input.certificadoPdfTemplateService);
    }

    public CentroMedicoPdfWorkflowService.PrepareFichaCommandData buildPrepareFichaCommand(
            CentroMedicoPdfTemplateCoordinator templateCoordinator,
            PdfFichaViewData viewData) {
        return templateCoordinator.buildPrepareFichaCommand(viewData);
    }

    public CentroMedicoPdfWorkflowService.PrepareCertificadoCommandData buildPrepareCertificadoCommand(
            CentroMedicoPdfTemplateCoordinator templateCoordinator,
            PdfCertificadoViewData viewData) {
        return templateCoordinator.buildPrepareCertificadoCommand(viewData);
    }

    public void onPrepararFichaPdfSuccess(CentroMedicoPdfWorkflowService.FichaFlowResult result,
            FacesContext ctx,
            PdfPreviewState pdfPreviewState,
            Consumer<FichaOcupacional> fichaSetter,
            Consumer<String> activeStepSetter,
            Consumer<Boolean> mostrarDlgCedulaSetter) {
        if (ctx == null || result == null) {
            return;
        }
        if (!result.listo) {
            showValidationMessage(ctx, "Validación antes de generar la ficha", result.errores);
            pdfPreviewState.setFichaPdfListo(false);
            pdfPreviewState.setPdfTokenFicha(null);
            return;
        }

        fichaSetter.accept(result.ficha);
        pdfPreviewState.setPdfTokenFicha(result.token);
        pdfPreviewState.setFichaPdfListo(true);
        activeStepSetter.accept("step4");
        mostrarDlgCedulaSetter.accept(false);

        ctx.addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "PDF Ficha listo",
                "Se generó la ficha para vista previa y descarga."));

        PrimeFaces.current().ajax().addCallbackParam("fichaListo", pdfPreviewState.isFichaPdfListo());
        PrimeFaces.current().ajax().update(":msgs", "@([id$=wdzFicha])");
    }

    public void applyPdfUiState(CentroMedicoPdfUiCoordinator uiCoordinator,
            CentroMedicoPdfUiCoordinator.PdfUiState state,
            PdfPreviewState pdfPreviewState,
            Consumer<FichaOcupacional> fichaSetter,
            Consumer<String> activeStepSetter,
            Consumer<Boolean> mostrarDlgCedulaSetter) {
        uiCoordinator.applyPdfUiState(
                state,
                pdfPreviewState,
                fichaSetter,
                pdfPreviewState::setFichaPdfListo,
                pdfPreviewState::setPdfTokenFicha,
                pdfPreviewState::setCertificadoListo,
                pdfPreviewState::setPdfTokenCertificado,
                pdfPreviewState::setPdfObjectUrl,
                activeStepSetter,
                mostrarDlgCedulaSetter);
    }

    public void resetStep4PdfState(CentroMedicoPdfUiCoordinator uiCoordinator,
            PdfPreviewState pdfPreviewState,
            Consumer<FichaOcupacional> fichaSetter,
            Consumer<String> activeStepSetter,
            Consumer<Boolean> mostrarDlgCedulaSetter) {
        applyPdfUiState(uiCoordinator, uiCoordinator.resetStep4PdfState(), pdfPreviewState, fichaSetter,
                activeStepSetter, mostrarDlgCedulaSetter);
    }

    public void applyStep4State(CentroMedicoPdfUiCoordinator uiCoordinator,
            CentroMedicoWizardNavigationCoordinator.Step4UiState step4State,
            PdfPreviewState pdfPreviewState,
            Consumer<FichaOcupacional> fichaSetter,
            Consumer<String> activeStepSetter,
            Consumer<Boolean> mostrarDlgCedulaSetter) {
        applyPdfUiState(uiCoordinator, uiCoordinator.applyStep4State(step4State, null), pdfPreviewState,
                fichaSetter, activeStepSetter, mostrarDlgCedulaSetter);
    }

    public void applyCleanupPdfPreviewState(CentroMedicoPdfUiCoordinator uiCoordinator,
            PdfPreviewState pdfPreviewState,
            Consumer<FichaOcupacional> fichaSetter,
            Consumer<String> activeStepSetter,
            Consumer<Boolean> mostrarDlgCedulaSetter) {
        applyPdfUiState(uiCoordinator, uiCoordinator.applyCleanupPdfPreviewState(null), pdfPreviewState,
                fichaSetter, activeStepSetter, mostrarDlgCedulaSetter);
    }

    public void syncCamposDesdeObjetosInternal(SyncCamposDesdeObjetosInput input) {
        FichaPdfMappedData data = input.fichaPdfContextAssembler.syncCamposDesdeObjetos(
                input.fichaPdfDataMapper,
                input.ficha,
                input.empleadoSel,
                input.fechaNacimiento);
        input.institucionSetter.accept(data.institucion);
        input.rucSetter.accept(data.ruc);
        input.centroTrabajoSetter.accept(data.centroTrabajo);
        input.ciiuSetter.accept(data.ciiu);
        input.noHistoriaSetter.accept(data.noHistoria);
        input.noArchivoSetter.accept(data.noArchivo);
        input.ginecoExamen1Setter.accept(data.ginecoExamen1);
        input.ginecoTiempo1Setter.accept(data.ginecoTiempo1);
        input.ginecoResultado1Setter.accept(data.ginecoResultado1);
        input.ginecoExamen2Setter.accept(data.ginecoExamen2);
        input.ginecoTiempo2Setter.accept(data.ginecoTiempo2);
        input.ginecoResultado2Setter.accept(data.ginecoResultado2);
        input.ginecoObservacionSetter.accept(data.ginecoObservacion);
        input.enfermedadActualSetter.accept(data.enfermedadActual);
        if (data.apellido1 != null) {
            input.apellido1Setter.accept(data.apellido1);
        }
        if (data.apellido2 != null) {
            input.apellido2Setter.accept(data.apellido2);
        }
        if (data.nombre1 != null) {
            input.nombre1Setter.accept(data.nombre1);
        }
        if (data.nombre2 != null) {
            input.nombre2Setter.accept(data.nombre2);
        }
        if (data.edad != null) {
            input.edadSetter.accept(data.edad);
        }
    }

    public void showValidationMessage(FacesContext ctx, String summary, List<String> errors) {
        centroMedicoPdfFacadeService.showValidationMessage(ctx, summary, errors);
    }

    public String obtenerTipoEvaluacionPdf(String tipoEval, String tipoEvaluacion) {
        String tipo = PdfTextUtil.trimToNull(tipoEval);
        if (isBlank(tipo)) {
            tipo = PdfTextUtil.trimToNull(tipoEvaluacion);
        }
        return tipo;
    }

    public void cargarAtencionPrioritaria(FichaOcupacional ficha,
            boolean apDiscapacidad,
            boolean apCatastrofica,
            boolean apEmbarazada,
            boolean apLactancia,
            boolean apAdultoMayor,
            Map<String, String> rep,
            Logger log) {
        rep.put("apCatastrofica_sn", apCatastrofica ? "SI" : "NO");
        rep.put("apDiscapacidad_sn", apDiscapacidad ? "SI" : "NO");
        rep.put("apEmbarazada_sn", apEmbarazada ? "SI" : "NO");
        rep.put("apLactancia_sn", apLactancia ? "SI" : "NO");
        rep.put("apAdultoMayor_sn", apAdultoMayor ? "SI" : "NO");

        boolean aplicaDis;
        boolean aplicaCat;

        if (ficha != null) {
            aplicaDis = "S".equalsIgnoreCase(safe(ficha.getApDiscapacidad()));
            aplicaCat = "S".equalsIgnoreCase(safe(ficha.getApCatastrofica()));
        } else {
            aplicaDis = apDiscapacidad;
            aplicaCat = apCatastrofica;
        }

        rep.put("apDiscapacidad_style", aplicaDis ? "" : "display:none;");
        rep.put("apCatastrofica_style", aplicaCat ? "" : "display:none;");

        rep.put("disTipo", safe(ficha != null ? ficha.getDisTipo() : null));
        rep.put("disDescripcion", safe(ficha != null ? ficha.getDisDescripcion() : null));

        Integer porc = (ficha != null ? ficha.getDisPorcentaje() : null);
        rep.put("disPorcentaje", porc == null ? "" : String.valueOf(porc));

        rep.put("catDiagnostico", safe(ficha != null ? ficha.getCatDiagnostico() : null));

        String catCal = safe(ficha != null ? ficha.getCatCalificada() : null);
        if ("S".equalsIgnoreCase(catCal) || "TRUE".equalsIgnoreCase(catCal)) {
            rep.put("catCalificada", "SI");
        } else if ("N".equalsIgnoreCase(catCal) || "FALSE".equalsIgnoreCase(catCal)) {
            rep.put("catCalificada", "NO");
        } else {
            rep.put("catCalificada", catCal);
        }
        log.info("[PDF] disTipo={} disDescripcion={} disPorcentaje={} catDiagnostico={} catCalificada={} styleDis={} styleCat={}",
                rep.get("disTipo"),
                rep.get("disDescripcion"),
                rep.get("disPorcentaje"),
                rep.get("catDiagnostico"),
                rep.get("catCalificada"),
                rep.get("apDiscapacidad_style"),
                rep.get("apCatastrofica_style"));
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
            Map<String, String> rep) {
        for (int i = 0; i < hRows; i++) {
            int idx = i + 1;
            rep.put("h_centro_" + idx, safe(getSafe(actLabCentroTrabajo, i)));
            rep.put("h_actividad_" + idx, safe(getSafe(actLabActividad, i)));
            rep.put("h_tiempo_" + idx, safe(getSafe(actLabTiempo, i)));
            rep.put("h_anterior_" + idx, isTrue(getSafe(actLabTrabajoAnterior, i)) ? "X" : "");
            rep.put("h_actual_" + idx, isTrue(getSafe(actLabTrabajoActual, i)) ? "X" : "");
            rep.put("h_incidente_" + idx, isTrue(getSafe(actLabIncidenteChk, i)) ? "X" : "");
            rep.put("h_accidente_" + idx, isTrue(getSafe(actLabAccidenteChk, i)) ? "X" : "");
            rep.put("h_enfermedad_" + idx, isTrue(getSafe(actLabEnfermedadChk, i)) ? "X" : "");
            rep.put("h_iess_si_" + idx, isTrue(getSafe(iessSi, i)) ? "X" : "");
            rep.put("h_iess_no_" + idx, isTrue(getSafe(iessNo, i)) ? "X" : "");
            rep.put("h_iess_fecha_" + idx, fmtDate(toDate(getSafe(iessFecha, i))));
            rep.put("h_iess_especificar_" + idx, safe(getSafe(iessEspecificar, i)));
            rep.put("h_obs_" + idx, safe(getSafe(actLabObservaciones, i)));
        }
    }

    public static final class CapturePdfFichaInput {
        public Object source;
        public Logger log;
        public FichaOcupacional ficha;
        public DatEmpleado empleadoSel;
        public PersonaAux personaAux;
        public boolean permitirIngresoManual;
        public Runnable asegurarPersonaAuxPersistida;
        public CentroMedicoPdfFacade centroMedicoPdfFacade;
        public PdfResourceResolver pdfResourceResolver;
        public Runnable syncCamposDesdeObjetos;
        public String tipoEval;
        public String tipoEvaluacion;
        public Runnable recalcularIMC;
        public boolean apDiscapacidad;
        public boolean apCatastrofica;
        public boolean apEmbarazada;
        public boolean apLactancia;
        public boolean apAdultoMayor;
        public int hRows;
        public List<String> actLabCentroTrabajo;
        public List<String> actLabActividad;
        public List<String> actLabTiempo;
        public List<Boolean> actLabTrabajoAnterior;
        public List<Boolean> actLabTrabajoActual;
        public List<Boolean> actLabIncidenteChk;
        public List<Boolean> actLabAccidenteChk;
        public List<Boolean> actLabEnfermedadChk;
        public List<Boolean> iessSi;
        public List<Boolean> iessNo;
        public List<?> iessFecha;
        public List<String> iessEspecificar;
        public List<String> actLabObservaciones;
    }

    public static final class CapturePdfCertificadoInput {
        public FichaOcupacional ficha;
        public Supplier<Boolean> verificarFichaCompleta;
        public Consumer<java.util.Date> fechaEmisionSetter;
        public CentroMedicoPdfFacade centroMedicoPdfFacade;
        public java.util.Date fechaEmision;
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

    public static final class SyncCamposDesdeObjetosInput {
        public FichaPdfContextAssembler fichaPdfContextAssembler;
        public FichaPdfDataMapper fichaPdfDataMapper;
        public FichaOcupacional ficha;
        public DatEmpleado empleadoSel;
        public java.util.Date fechaNacimiento;
        public Consumer<String> institucionSetter;
        public Consumer<String> rucSetter;
        public Consumer<String> centroTrabajoSetter;
        public Consumer<String> ciiuSetter;
        public Consumer<String> noHistoriaSetter;
        public Consumer<String> noArchivoSetter;
        public Consumer<String> ginecoExamen1Setter;
        public Consumer<String> ginecoTiempo1Setter;
        public Consumer<String> ginecoResultado1Setter;
        public Consumer<String> ginecoExamen2Setter;
        public Consumer<String> ginecoTiempo2Setter;
        public Consumer<String> ginecoResultado2Setter;
        public Consumer<String> ginecoObservacionSetter;
        public Consumer<String> enfermedadActualSetter;
        public Consumer<String> apellido1Setter;
        public Consumer<String> apellido2Setter;
        public Consumer<String> nombre1Setter;
        public Consumer<String> nombre2Setter;
        public Consumer<Integer> edadSetter;
    }
}
