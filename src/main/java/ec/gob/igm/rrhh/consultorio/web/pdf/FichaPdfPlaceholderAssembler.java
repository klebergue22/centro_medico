package ec.gob.igm.rrhh.consultorio.web.pdf;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import jakarta.ejb.Stateless;

import org.slf4j.Logger;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfViewModelBuilder;
import ec.gob.igm.rrhh.consultorio.web.service.FichaPdfViewModelBuilder.FichaPdfViewModelContext;

@Stateless
/**
 * Class FichaPdfPlaceholderAssembler: gestiona la construcción y renderización de documentos PDF.
 */
public class FichaPdfPlaceholderAssembler implements Serializable {

    public Map<String, String> buildReemplazosFicha(FichaState state) {
        Map<String, String> rep = new LinkedHashMap<>();
        if (state == null) {
            return rep;
        }
        cargarLogos(rep, state.centroMedicoPdfFacade, state.log);
        cargarFechaActual(rep);
        cargarAntecedentes(rep, state.antClinicoQuirurgico, state.antFamiliares, state.antTerapeutica, state.antObs);
        cargarRiesgos(rep, state.riesgos);
        if (state.fichaPdfViewModelBuilder != null && state.fichaPdfViewModelContext != null) {
            rep.putAll(state.fichaPdfViewModelBuilder.buildReemplazosFicha(state.fichaPdfViewModelContext));
        }
        cargarActividadesLaboralesList(rep, state.actividadesLab);
        cargarMedidasPreventivasList(rep, state.medidasPreventivas);
        cargarHActividadLaboral(rep, state);
        cargarIActividadesExtra(rep, state.tipoAct, state.fechaAct, state.descAct, state.toDateParser);
        cargarJExamenes(rep, state.examNombre, state.examFecha, state.examResultado, state.obsJ, state.toDateParser);
        cargarKDiagnosticos(rep, state.listaDiag);
        cargarLAptitud(rep, state.aptitudSel, state.detalleObservaciones, state.fallbackObservacion);
        cargarNRetiro(rep, state.nRealizaEvaluacion, state.nRelacionTrabajo, state.nObsRetiro);
        cargarOProfesional(rep, state.medicoNombre, state.medicoCodigo);
        cargarAntecedentesCamelCase(rep, state.antClinicoQuirurgico, state.antFamiliares, state.antTerapeutica, state.antObs);
        corregirOtrosRiesgos(rep, state.otrosRiesgos);
        return rep;
    }

    private void cargarLogos(Map<String, String> rep, CentroMedicoPdfFacade facade, Logger log) {
        if (facade == null) {
            rep.put("LOGO_IGM_DATAURI", "");
            rep.put("LOGO_MIDENA_DATAURI", "");
            return;
        }
        try {
            rep.put("LOGO_IGM_DATAURI", facade.dataUriFromResource("images/LOGO_IGM_FULL_COLOR.png"));
        } catch (IOException ex) {
            warn(log, "[FICHA] No se pudo cargar LOGo IGM", ex);
            rep.put("LOGO_IGM_DATAURI", "");
        }
        try {
            rep.put("LOGO_MIDENA_DATAURI", facade.dataUriFromResource("images/LOGO_MIDENA.png"));
        } catch (IOException ex) {
            warn(log, "[FICHA] No se pudo cargar LOGo MIDENA", ex);
            rep.put("LOGO_MIDENA_DATAURI", "");
        }
    }

    private void cargarFechaActual(Map<String, String> rep) {
        rep.put("fechaActual", PdfTextUtil.safeDate(new java.util.Date()));
    }

    private void cargarAntecedentes(Map<String, String> rep, String antClinicoQuirurgico, String antFamiliares,
            String antTerapeutica, String antObs) {
        rep.put("ant_clinico_quirurgico", PdfTextUtil.safePdf(antClinicoQuirurgico));
        rep.put("ant_familiares", PdfTextUtil.safePdf(antFamiliares));
        rep.put("ant_terapeutica", PdfTextUtil.safePdf(antTerapeutica));
        rep.put("ant_obs", PdfTextUtil.safePdf(antObs));
    }

    private void cargarRiesgos(Map<String, String> rep, Map<String, Boolean> riesgos) {
        if (riesgos == null) {
            return;
        }
        for (Map.Entry<String, Boolean> e : riesgos.entrySet()) {
            String key = normalizePlaceholderKey(e.getKey());
            if (key != null) {
                rep.put(key, Boolean.TRUE.equals(e.getValue()) ? "X" : "");
            }
        }
    }

    private void cargarActividadesLaboralesList(Map<String, String> rep, List<String> actividadesLab) {
        if (actividadesLab == null) {
            return;
        }
        for (int i = 0; i < actividadesLab.size() && i < 7; i++) {
            rep.put("actividad_" + (i + 1), PdfTextUtil.safePdf(actividadesLab.get(i)));
        }
    }

    private void cargarMedidasPreventivasList(Map<String, String> rep, List<String> medidasPreventivas) {
        if (medidasPreventivas == null) {
            return;
        }
        for (int i = 0; i < medidasPreventivas.size() && i < 7; i++) {
            rep.put("medida_" + (i + 1), PdfTextUtil.safePdf(medidasPreventivas.get(i)));
        }
    }

    private void cargarHActividadLaboral(Map<String, String> rep, FichaState s) {
        BiFunction<List<?>, Integer, Object> safeAccessor = s.getSafe != null ? s.getSafe : FichaPdfPlaceholderAssembler::getSafe;
        Function<Object, java.util.Date> dateParser = s.toDateParser != null ? s.toDateParser : unused -> null;
        for (int i = 0; i < 4; i++) {
            rep.put("act_lab_centro_" + i, safePdfObject(safeAccessor.apply(s.actLabCentroTrabajo, i)));
            rep.put("act_lab_actividad_" + i, safePdfObject(safeAccessor.apply(s.actLabActividad, i)));
            rep.put("act_lab_tiempo_" + i, safePdfObject(safeAccessor.apply(s.actLabTiempo, i)));
            rep.put("act_lab_anterior_" + i, truthy(safeAccessor.apply(s.actLabTrabajoAnterior, i)) ? "X" : "");
            rep.put("act_lab_actual_" + i, truthy(safeAccessor.apply(s.actLabTrabajoActual, i)) ? "X" : "");
            rep.put("act_lab_incidente_" + i, truthy(safeAccessor.apply(s.actLabIncidenteChk, i)) ? "X" : "");
            rep.put("act_lab_accidente_" + i, truthy(safeAccessor.apply(s.actLabAccidenteChk, i)) ? "X" : "");
            rep.put("act_lab_enfermedad_" + i, truthy(safeAccessor.apply(s.actLabEnfermedadChk, i)) ? "X" : "");
            rep.put("iess_si_" + i, truthy(safeAccessor.apply(s.iessSi, i)) ? "X" : "");
            rep.put("iess_no_" + i, truthy(safeAccessor.apply(s.iessNo, i)) ? "X" : "");
            rep.put("iess_fecha_" + i, PdfTextUtil.safeDate(dateParser.apply(safeAccessor.apply(s.iessFecha, i))));
            rep.put("iess_especificar_" + i, safePdfObject(safeAccessor.apply(s.iessEspecificar, i)));
            rep.put("act_lab_obs_" + i, safePdfObject(safeAccessor.apply(s.actLabObservaciones, i)));
        }
    }

    private static String safePdfObject(Object value) {
        return PdfTextUtil.safePdf(value == null ? null : String.valueOf(value));
    }

    private void cargarIActividadesExtra(Map<String, String> rep, List<String> tipoAct, List<?> fechaAct, List<String> descAct,
            Function<Object, java.util.Date> toDateParser) {
        Function<Object, java.util.Date> dateParser = toDateParser != null ? toDateParser : unused -> null;
        for (int i = 0; i < 3; i++) {
            rep.put("tipo_act_" + i, PdfTextUtil.safePdf(getSafe(tipoAct, i)));
            rep.put("fecha_act_" + i, PdfTextUtil.safeDate(dateParser.apply(getSafe(fechaAct, i))));
            rep.put("desc_act_" + i, PdfTextUtil.safePdf(getSafe(descAct, i)));
        }
    }

    private void cargarJExamenes(Map<String, String> rep, List<String> examNombre, List<?> examFecha, List<String> examResultado,
            String obsJ, Function<Object, java.util.Date> toDateParser) {
        Function<Object, java.util.Date> dateParser = toDateParser != null ? toDateParser : unused -> null;
        for (int i = 0; i < 5; i++) {
            rep.put("exam_nombre_" + i, PdfTextUtil.safePdf(getSafe(examNombre, i)));
            rep.put("exam_fecha_" + i, PdfTextUtil.safeDate(dateParser.apply(getSafe(examFecha, i))));
            rep.put("exam_resultado_" + i, PdfTextUtil.safePdf(getSafe(examResultado, i)));
        }
        rep.put("obs_j", PdfTextUtil.safePdf(obsJ));
    }

    private void warn(Logger log, String message, Throwable throwable) {
        if (log != null) {
            log.warn(message, throwable);
        }
    }

    private void cargarKDiagnosticos(Map<String, String> rep, List<ConsultaDiagnostico> listaDiag) {
        if (listaDiag == null) {
            return;
        }
        for (int i = 0; i < listaDiag.size() && i < 6; i++) {
            ConsultaDiagnostico d = listaDiag.get(i);
            if (d == null) {
                continue;
            }
            rep.put("k_codigo_" + i, PdfTextUtil.safePdf(d.getCodigo()));
            rep.put("k_desc_" + i, PdfTextUtil.safePdf(d.getDescripcion()));
            rep.put("k_pre_" + i, "P".equals(d.getTipoDiag()) ? "X" : "");
            rep.put("k_def_" + i, "D".equals(d.getTipoDiag()) ? "X" : "");
        }
    }

    private void cargarLAptitud(Map<String, String> rep, String aptitudSel, String detalleObservaciones, String fallbackObs) {
        rep.put("l_apto", "APTO".equals(aptitudSel) ? "X" : "");
        rep.put("l_apto_obs", "APTO_EN_OBS".equals(aptitudSel) ? "X" : "");
        rep.put("l_apto_limit", "APTO_LIMIT".equals(aptitudSel) ? "X" : "");
        rep.put("l_no_apto", "NO_APTO".equals(aptitudSel) ? "X" : "");
        rep.put("l_observaciones", PdfTextUtil.safePdf(PdfTextUtil.trimToNull(detalleObservaciones) != null
                ? detalleObservaciones : fallbackObs));
    }

    private void cargarNRetiro(Map<String, String> rep, String nRealizaEvaluacion, String nRelacionTrabajo, String nObsRetiro) {
        rep.put("n_realiza_eval_si", isYes(nRealizaEvaluacion) ? "X" : "");
        rep.put("n_realiza_eval_no", isNo(nRealizaEvaluacion) ? "X" : "");
        rep.put("n_relacion_trabajo_si", isYes(nRelacionTrabajo) ? "X" : "");
        rep.put("n_relacion_trabajo_no", isNo(nRelacionTrabajo) ? "X" : "");
        rep.put("n_obs_retiro", PdfTextUtil.safePdf(nObsRetiro));
    }

    private void cargarOProfesional(Map<String, String> rep, String medicoNombre, String medicoCodigo) {
        rep.put("medico_nombre", PdfTextUtil.safePdf(medicoNombre));
        rep.put("medico_codigo", PdfTextUtil.safePdf(medicoCodigo));
    }

    private void cargarAntecedentesCamelCase(Map<String, String> rep, String antClinicoQuirurgico, String antFamiliares,
            String antTerapeutica, String antObs) {
        rep.put("antClinicoQuirurgico", PdfTextUtil.safePdf(antClinicoQuirurgico));
        rep.put("antFamiliares", PdfTextUtil.safePdf(antFamiliares));
        rep.put("antTerapeutica", PdfTextUtil.safePdf(antTerapeutica));
        rep.put("antObs", PdfTextUtil.safePdf(antObs));
    }

    private void corregirOtrosRiesgos(Map<String, String> rep, Map<String, String> otrosRiesgos) {
        if (otrosRiesgos == null) {
            return;
        }
        for (Map.Entry<String, String> e : otrosRiesgos.entrySet()) {
            if (e.getKey() == null) {
                continue;
            }
            String value = PdfTextUtil.safePdf(e.getValue());
            String ph = normalizePlaceholderKey(e.getKey());
            if (ph == null) {
                continue;
            }
            rep.put(ph, value);
            String alias = normalizeOtrosPlaceholder(ph);
            if (alias != null) {
                rep.put(alias, value);
            }
        }
    }

    private String normalizePlaceholderKey(String key) {
        String normalized = PdfTextUtil.trimToNull(key);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.toLowerCase();
        if (normalized.startsWith("{{") && normalized.endsWith("}}") && normalized.length() > 4) {
            normalized = normalized.substring(2, normalized.length() - 2).trim();
        }
        return PdfTextUtil.trimToNull(normalized);
    }

    private String normalizeOtrosPlaceholder(String keyLower) {
        if (PdfTextUtil.trimToNull(keyLower) == null) {
            return null;
        }
        String[] parts = keyLower.split("_");
        if (parts.length != 3) {
            return null;
        }
        if ("otros".equals(parts[0])) {
            return keyLower;
        }
        if ("otros".equals(parts[1])) {
            return "otros_" + parts[0] + "_" + parts[2];
        }
        return null;
    }

    private static <T> T getSafe(List<T> list, int i) {
        if (list == null || i < 0 || i >= list.size()) {
            return null;
        }
        return list.get(i);
    }

    private static boolean truthy(Object v) {
        if (v == null) {
            return false;
        }
        String s = String.valueOf(v).trim().toUpperCase();
        return "S".equals(s) || "SI".equals(s) || "TRUE".equals(s) || "1".equals(s) || "X".equals(s);
    }

    private static boolean isYes(Object v) {
        return truthy(v == null ? null : String.valueOf(v));
    }

    private static boolean isNo(Object v) {
        if (v == null) {
            return false;
        }
        String s = String.valueOf(v).trim().toUpperCase();
        return "N".equals(s) || "NO".equals(s) || "FALSE".equals(s) || "0".equals(s);
    }

    public static class FichaState {
        public CentroMedicoPdfFacade centroMedicoPdfFacade;
        public Logger log;
        public FichaPdfViewModelBuilder fichaPdfViewModelBuilder;
        public FichaPdfViewModelContext fichaPdfViewModelContext;
        public String antClinicoQuirurgico;
        public String antFamiliares;
        public String antTerapeutica;
        public String antObs;
        public Map<String, Boolean> riesgos;
        public List<String> actividadesLab;
        public List<String> medidasPreventivas;
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
        public List<String> tipoAct;
        public List<?> fechaAct;
        public List<String> descAct;
        public List<String> examNombre;
        public List<?> examFecha;
        public List<String> examResultado;
        public String obsJ;
        public List<ConsultaDiagnostico> listaDiag;
        public String aptitudSel;
        public String detalleObservaciones;
        public String fallbackObservacion;
        public String nRealizaEvaluacion;
        public String nRelacionTrabajo;
        public String nObsRetiro;
        public String medicoNombre;
        public String medicoCodigo;
        public Map<String, String> otrosRiesgos;
        public BiFunction<List<?>, Integer, Object> getSafe;
        public Function<Object, java.util.Date> toDateParser;
    }
}
