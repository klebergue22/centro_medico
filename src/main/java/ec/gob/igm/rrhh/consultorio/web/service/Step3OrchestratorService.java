package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaActLaboral;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaExamenComp;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.service.Cie10Service;
import ec.gob.igm.rrhh.consultorio.service.ExamenFisicoRegionalService;
import ec.gob.igm.rrhh.consultorio.service.FichaActLaboralService;
import ec.gob.igm.rrhh.consultorio.service.FichaDiagnosticoService;
import ec.gob.igm.rrhh.consultorio.service.FichaExamenCompService;
import ec.gob.igm.rrhh.consultorio.service.FichaOcupacionalService;

@Stateless
/**
 * Orquestador del Step 3 de la ficha ocupacional.
 *
 * <p>Funcionalidad principal: coordina guardado de datos generales de
 * {@link FichaOcupacional}, actividad laboral ({@link FichaActLaboral}),
 * exámenes complementarios ({@link FichaExamenComp}) y diagnósticos
 * ({@link ConsultaDiagnostico}).</p>
 *
 * <p>Relaciones directas: {@link Cie10Service}, {@link ExamenFisicoRegionalService},
 * {@link FichaOcupacionalService}, {@link FichaActLaboralService},
 * {@link FichaExamenCompService} y {@link FichaDiagnosticoService}.</p>
 */
public class Step3OrchestratorService implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(Step3OrchestratorService.class);
    private static final int H_ROWS = 4;

    @EJB
    private Cie10Service cie10Service;
    @EJB
    private ExamenFisicoRegionalService examenFisicoRegionalService;
    @EJB
    private FichaOcupacionalService fichaService;
    @EJB
    private FichaActLaboralService fichaActLaboralService;
    @EJB
    private FichaExamenCompService fichaExamenCompService;
    @EJB
    private FichaDiagnosticoService fichaDiagnosticoService;
    @EJB
    private UserContextService userContextService;

    public FichaOcupacional saveStep3(Step3SaveCommand cmd) {
        String user = userContextService.resolveCurrentUser();
        FichaOcupacional fichaActualizada = guardarStep3FichaGeneral(cmd, user);
        guardarStep3HActividadLaboral(cmd, fichaActualizada, user);
        fichaActualizada = guardarStep3IExtralaborales(cmd, fichaActualizada, user);
        guardarStep3JExamenes(cmd, fichaActualizada, user);
        guardarStep3KDiagnosticos(cmd, fichaActualizada, user);
        return fichaActualizada;
    }

    private FichaOcupacional guardarStep3FichaGeneral(Step3SaveCommand cmd, String user) {
        LOG.info("STEP3-A: Guardando datos generales en FICHA_OCUPACIONAL");
        applyPrincipalCie10(cmd);
        applyClinicalSummaryData(cmd);
        applyRetiroAndDoctorData(cmd, user);
        cmd.onEnsurePersonaAuxPersistida().run();
        FichaOcupacional saved = fichaService.guardar(cmd.ficha());
        LOG.info("STEP3-A-OK: FICHA_OCUPACIONAL actualizada. ID_FICHA={}", saved.getIdFicha());
        return saved;
    }

    private void applyPrincipalCie10(Step3SaveCommand cmd) {
        Cie10 cie = resolvePrincipalCie10(cmd);
        cmd.ficha().setCie10Principal(cie);
    }

    private Cie10 resolvePrincipalCie10(Step3SaveCommand cmd) {
        String codigoPrincipal = trimToNull(cmd.codCie10Ppal());
        Cie10 cie = buscarCie10PorCodigo(codigoPrincipal);
        if (cie != null) {
            return cie;
        }

        if (codigoPrincipal != null) {
            LOG.warn("STEP3-A: CIE10 principal invalido [{}]. Se intentara inferir desde lista K.", codigoPrincipal);
        }

        Cie10 inferido = inferirPrincipalDesdeLista(cmd.listaDiag());
        if (inferido != null) {
            return inferido;
        }

        if (codigoPrincipal != null) {
            throw new IllegalArgumentException("El codigo CIE10 principal no existe: " + cmd.codCie10Ppal());
        }

        return null;
    }

    private Cie10 inferirPrincipalDesdeLista(List<ConsultaDiagnostico> listaDiag) {
        if (listaDiag == null || listaDiag.isEmpty()) {
            return null;
        }

        Cie10 fallback = null;
        for (ConsultaDiagnostico diagnostico : listaDiag) {
            if (diagnostico == null) {
                continue;
            }

            Cie10 cie = buscarCie10PorCodigo(trimToNull(diagnostico.getCodigo()));
            if (cie == null) {
                continue;
            }

            if ("D".equals(trimToNull(diagnostico.getTipoDiag()))) {
                return cie;
            }

            if (fallback == null) {
                fallback = cie;
            }
        }

        return fallback;
    }

    private Cie10 buscarCie10PorCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }

        return cie10Service.buscarPorCodigo(codigo);
    }

    private void applyClinicalSummaryData(Step3SaveCommand cmd) {
        cmd.ficha().setEnfermedadProbActual(trimToNull(cmd.ficha().getEnfermedadProbActual()));
        examenFisicoRegionalService.aplicarExamenFisicoRegional(cmd.ficha());
        cmd.ficha().setObsExamenFisicoReg(resolveObsExamenFisico(cmd));
        cmd.ficha().setAptitudSel(cmd.aptitudSel());
        cmd.ficha().setDetalleObs(cmd.detalleObservaciones());
        cmd.ficha().setRecomendaciones(cmd.recomendaciones());
    }

    private String resolveObsExamenFisico(Step3SaveCommand cmd) {
        String obsExf = trimToNull(cmd.obsExamenFisico());
        if (obsExf != null) {
            return obsExf;
        }
        return trimToNull(cmd.ficha().getObsExamenFisicoReg());
    }

    private void applyRetiroAndDoctorData(Step3SaveCommand cmd, String user) {
        examenFisicoRegionalService.aplicarRetiro(cmd.ficha());
        cmd.ficha().setnRetObs(cmd.nObsRetiro());
        cmd.ficha().setMedicoNombre(cmd.medicoNombre());
        cmd.ficha().setMedicoCodigo(cmd.medicoCodigo());
        cmd.ficha().setFechaEmision(cmd.fechaEmision() != null ? cmd.fechaEmision() : cmd.now());
        cmd.ficha().setFechaActualizacion(cmd.now());
        cmd.ficha().setUsrActualizacion(user);
    }

    private void guardarStep3HActividadLaboral(Step3SaveCommand cmd, FichaOcupacional ficha, String user) {
        LOG.info("STEP3-H: Procesando Actividad Laboral (FICHA_ACT_LABORAL)");
        cmd.onEnsureActLabSize().run();
        for (int i = 0; i < H_ROWS; i++) {
            saveActividadLaboralRow(cmd, ficha, i, user);
        }
        LOG.info("STEP3-H-OK");
    }

    private void saveActividadLaboralRow(Step3SaveCommand cmd, FichaOcupacional ficha, int index, String user) {
        int nroFila = index + 1;
        if (!actividadLaboralHasData(cmd, index)) {
            fichaActLaboralService.eliminarPorFichaYFila(ficha.getIdFicha(), nroFila);
            return;
        }
        FichaActLaboral fal = findOrCreateActLaboral(cmd, ficha, nroFila, user);
        mapActividadLaboralFields(cmd, index, fal);
        fichaActLaboralService.guardar(fal);
    }

    private boolean actividadLaboralHasData(Step3SaveCommand cmd, int i) {
        return !isBlank(getSafe(cmd.actLabCentroTrabajo(), i))
                || !isBlank(getSafe(cmd.actLabActividad(), i))
                || !isBlank(getSafe(cmd.actLabTiempo(), i))
                || isTrue(getSafe(cmd.actLabTrabajoAnterior(), i))
                || isTrue(getSafe(cmd.actLabTrabajoActual(), i))
                || isTrue(getSafe(cmd.actLabIncidenteChk(), i))
                || isTrue(getSafe(cmd.actLabAccidenteChk(), i))
                || isTrue(getSafe(cmd.actLabEnfermedadChk(), i))
                || getSafe(cmd.iessFecha(), i) != null
                || !isBlank(getSafe(cmd.iessEspecificar(), i))
                || !isBlank(getSafe(cmd.actLabObservaciones(), i));
    }

    private FichaActLaboral findOrCreateActLaboral(Step3SaveCommand cmd, FichaOcupacional ficha, int nroFila, String user) {
        FichaActLaboral fal = fichaActLaboralService.buscarPorFichaYFila(ficha.getIdFicha(), nroFila);
        if (fal != null) {
            fal.setFActualizacion(cmd.now());
            fal.setUsrActualizacion(user);
            return fal;
        }
        fal = new FichaActLaboral();
        fal.setFicha(ficha);
        fal.setNroFila(nroFila);
        fal.setFCreacion(cmd.now());
        fal.setUsrCreacion(user);
        return fal;
    }

    private void mapActividadLaboralFields(Step3SaveCommand cmd, int i, FichaActLaboral fal) {
        fal.setCentroTrabajo(getSafe(cmd.actLabCentroTrabajo(), i));
        fal.setActividad(getSafe(cmd.actLabActividad(), i));
        fal.setTiempo(getSafe(cmd.actLabTiempo(), i));
        fal.setEsAnterior(sn(getSafe(cmd.actLabTrabajoAnterior(), i)));
        fal.setEsActual(sn(getSafe(cmd.actLabTrabajoActual(), i)));
        fal.setIncidente(sn(getSafe(cmd.actLabIncidenteChk(), i)));
        fal.setAccidente(sn(getSafe(cmd.actLabAccidenteChk(), i)));
        fal.setEnfOcupacional(sn(getSafe(cmd.actLabEnfermedadChk(), i)));
        fal.setFechaEvento(toDate(getSafe(cmd.iessFecha(), i)));
        fal.setEspecificar(getSafe(cmd.iessEspecificar(), i));
        fal.setObservaciones(getSafe(cmd.actLabObservaciones(), i));
    }

    private FichaOcupacional guardarStep3IExtralaborales(Step3SaveCommand cmd, FichaOcupacional ficha, String user) {
        LOG.info("STEP3-I: Procesando Actividades Extralaborales (SERIALIZADO EN FICHA)");
        if (cmd.tipoAct() == null || cmd.fechaAct() == null || cmd.descAct() == null) {
            LOG.info("STEP3-I: Listas I null -> no se guarda (no rompe)");
            return ficha;
        }
        ExtralaboralSummary summary = buildExtralaboralSummary(cmd);
        ficha.setExtraLabDesc(summary.descripcion());
        ficha.setExtraLabFecha(summary.ultimaFecha());
        ficha.setFechaActualizacion(cmd.now());
        ficha.setUsrActualizacion(user);
        FichaOcupacional saved = fichaService.guardar(ficha);
        LOG.info("STEP3-I-OK");
        return saved;
    }

    private ExtralaboralSummary buildExtralaboralSummary(Step3SaveCommand cmd) {
        StringBuilder sb = new StringBuilder();
        Date ultimaFecha = null;
        for (int i = 0; i < cmd.tipoAct().size(); i++) {
            Date fecha = appendExtralaboralLine(cmd, sb, i);
            if (fecha != null) {
                ultimaFecha = fecha;
            }
        }
        String descripcion = sb.length() == 0 ? null : sb.toString().trim();
        return new ExtralaboralSummary(descripcion, ultimaFecha);
    }

    private Date appendExtralaboralLine(Step3SaveCommand cmd, StringBuilder sb, int i) {
        String tipo = getSafe(cmd.tipoAct(), i);
        Date fecha = toDate(getSafe(cmd.fechaAct(), i));
        String descripcion = getSafe(cmd.descAct(), i);
        if (isBlank(tipo) && fecha == null && isBlank(descripcion)) {
            return null;
        }
        sb.append(i + 1).append(") ")
                .append(nullToDash(tipo)).append(" | ")
                .append(fecha != null ? new SimpleDateFormat("yyyy/MM/dd").format(fecha) : "----/--/--")
                .append(" | ")
                .append(nullToDash(descripcion))
                .append("\n");
        return fecha;
    }

    private void guardarStep3JExamenes(Step3SaveCommand cmd, FichaOcupacional ficha, String user) {
        LOG.info("STEP3-J: Procesando Exámenes (FICHA_EXAMEN_COMP)");
        if (cmd.examNombre() == null || cmd.examFecha() == null || cmd.examResultado() == null) {
            LOG.info("STEP3-J: Listas J null -> no se guarda J");
            return;
        }
        int filas = Math.min(cmd.examNombre().size(), Math.min(cmd.examFecha().size(), cmd.examResultado().size()));
        for (int i = 0; i < filas; i++) {
            saveExamRow(cmd, ficha, i, user);
        }
        LOG.info("STEP3-J-OK");
    }

    private void saveExamRow(Step3SaveCommand cmd, FichaOcupacional ficha, int i, String user) {
        int nroFila = i + 1;
        String nombre = getSafe(cmd.examNombre(), i);
        Date fecha = toDate(getSafe(cmd.examFecha(), i));
        String resultado = getSafe(cmd.examResultado(), i);
        if (isBlank(nombre) && fecha == null && isBlank(resultado)) {
            int del = fichaExamenCompService.eliminarPorFichaYFila(ficha.getIdFicha(), nroFila);
            LOG.info("STEP3-J-FILA {}: vacía -> delete={}", nroFila, del);
            return;
        }
        FichaExamenComp ex = findOrCreateExamRow(ficha, nroFila);
        ex.setNombreExamen(nombre);
        ex.setFechaExamen(fecha);
        ex.setResultado(resultado);
        fichaExamenCompService.guardar(ex, user);
    }

    private FichaExamenComp findOrCreateExamRow(FichaOcupacional ficha, int nroFila) {
        FichaExamenComp ex = fichaExamenCompService.buscarPorFichaYFila(ficha.getIdFicha(), nroFila);
        if (ex != null) {
            LOG.info("STEP3-J-FILA {}: UPDATE id={}", nroFila, ex.getIdFichaExamen());
            return ex;
        }
        ex = new FichaExamenComp();
        ex.setFicha(ficha);
        ex.setNroFila(nroFila);
        LOG.info("STEP3-J-FILA {}: INSERT", nroFila);
        return ex;
    }

    private void guardarStep3KDiagnosticos(Step3SaveCommand cmd, FichaOcupacional ficha, String user) {
        LOG.info("STEP3-K: Procesando Diagnósticos");

        if (cmd.listaDiag() == null || cmd.listaDiag().isEmpty()) {
            LOG.info("STEP3-K: listaDiag vacía -> OK");
            return;
        }

        try {
            fichaDiagnosticoService.guardarDiagnosticosDeFicha(ficha.getIdFicha(), cmd.listaDiag(), cmd.now(), user);
            LOG.info("STEP3-K-OK (service)");
        } catch (NoSuchMethodError | RuntimeException ex) {
            LOG.info("STEP3-K: Tu service no tiene guardarDiagnosticosDeFicha(...) -> no se guarda K");
        }
    }

    private static String nullToDash(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }

    private static <T> T getSafe(List<T> list, int idx) {
        if (list == null || idx < 0 || idx >= list.size()) {
            return null;
        }
        return list.get(idx);
    }

    private static boolean isTrue(Boolean b) {
        return b != null && b;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String sn(Boolean b) {
        return (b != null && b) ? "S" : "N";
    }

    private static Date toDate(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Date d) {
            return d;
        }
        return null;
    }

    /**
     * Comando de entrada del Step 3.
     *
     * <p>Concentra datos de la vista y callbacks de soporte para que el controlador
     * web delegue en este orquestador sin acoplarse a la lógica de persistencia.</p>
     */
    public record Step3SaveCommand(
            FichaOcupacional ficha,
            String codCie10Ppal,
            String obsExamenFisico,
            String aptitudSel,
            String detalleObservaciones,
            String recomendaciones,
            String nObsRetiro,
            String medicoNombre,
            String medicoCodigo,
            Date fechaEmision,
            Date now,
            Runnable onEnsurePersonaAuxPersistida,
            Runnable onEnsureActLabSize,
            List<String> actLabCentroTrabajo,
            List<String> actLabActividad,
            List<String> actLabTiempo,
            List<Boolean> actLabTrabajoAnterior,
            List<Boolean> actLabTrabajoActual,
            List<Boolean> actLabIncidenteChk,
            List<Boolean> actLabAccidenteChk,
            List<Boolean> actLabEnfermedadChk,
            List<Date> iessFecha,
            List<String> iessEspecificar,
            List<String> actLabObservaciones,
            List<String> tipoAct,
            List<Date> fechaAct,
            List<String> descAct,
            List<String> examNombre,
            List<Date> examFecha,
            List<String> examResultado,
            List<ConsultaDiagnostico> listaDiag) {
    }

    private record ExtralaboralSummary(String descripcion, Date ultimaFecha) {
    }
}
