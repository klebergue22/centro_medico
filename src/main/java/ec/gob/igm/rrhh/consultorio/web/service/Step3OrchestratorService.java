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

    public FichaOcupacional saveStep3(Step3SaveCommand cmd) {
        FichaOcupacional fichaActualizada = guardarStep3FichaGeneral(cmd);
        guardarStep3HActividadLaboral(cmd, fichaActualizada);
        fichaActualizada = guardarStep3IExtralaborales(cmd, fichaActualizada);
        guardarStep3JExamenes(cmd, fichaActualizada);
        guardarStep3KDiagnosticos(cmd, fichaActualizada);
        return fichaActualizada;
    }

    private FichaOcupacional guardarStep3FichaGeneral(Step3SaveCommand cmd) {
        LOG.info("STEP3-A: Guardando datos generales en FICHA_OCUPACIONAL");

        if (!isBlank(cmd.codCie10Ppal())) {
            Cie10 cie = cie10Service.buscarPorCodigo(cmd.codCie10Ppal().trim());
            if (cie == null) {
                throw new IllegalArgumentException("El código CIE10 principal no existe: " + cmd.codCie10Ppal());
            }
            cmd.ficha().setCie10Principal(cie);
        } else {
            cmd.ficha().setCie10Principal(null);
        }

        cmd.ficha().setEnfermedadProbActual(trimToNull(cmd.ficha().getEnfermedadProbActual()));
        examenFisicoRegionalService.aplicarExamenFisicoRegional(cmd.ficha());

        String obsExf = trimToNull(cmd.obsExamenFisico());
        if (obsExf == null) {
            obsExf = trimToNull(cmd.ficha().getObsExamenFisicoReg());
        }
        cmd.ficha().setObsExamenFisicoReg(obsExf);

        cmd.ficha().setAptitudSel(cmd.aptitudSel());
        cmd.ficha().setDetalleObs(cmd.detalleObservaciones());
        cmd.ficha().setRecomendaciones(cmd.recomendaciones());

        examenFisicoRegionalService.aplicarRetiro(cmd.ficha());
        cmd.ficha().setnRetObs(cmd.nObsRetiro());
        cmd.ficha().setMedicoNombre(cmd.medicoNombre());
        cmd.ficha().setMedicoCodigo(cmd.medicoCodigo());
        cmd.ficha().setFechaEmision(cmd.fechaEmision() != null ? cmd.fechaEmision() : cmd.now());
        cmd.ficha().setFechaActualizacion(cmd.now());
        cmd.ficha().setUsrActualizacion(cmd.user());

        cmd.onEnsurePersonaAuxPersistida().run();

        FichaOcupacional saved = fichaService.guardar(cmd.ficha());
        LOG.info("STEP3-A-OK: FICHA_OCUPACIONAL actualizada. ID_FICHA={}", saved.getIdFicha());
        return saved;
    }

    private void guardarStep3HActividadLaboral(Step3SaveCommand cmd, FichaOcupacional ficha) {
        LOG.info("STEP3-H: Procesando Actividad Laboral (FICHA_ACT_LABORAL)");

        cmd.onEnsureActLabSize().run();

        for (int i = 0; i < H_ROWS; i++) {
            int nroFila = i + 1;

            boolean filaTieneDatos = !isBlank(getSafe(cmd.actLabCentroTrabajo(), i))
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

            if (!filaTieneDatos) {
                fichaActLaboralService.eliminarPorFichaYFila(ficha.getIdFicha(), nroFila);
                continue;
            }

            FichaActLaboral fal = fichaActLaboralService.buscarPorFichaYFila(ficha.getIdFicha(), nroFila);
            if (fal == null) {
                fal = new FichaActLaboral();
                fal.setFicha(ficha);
                fal.setNroFila(nroFila);
                fal.setFCreacion(cmd.now());
                fal.setUsrCreacion(cmd.user());
            } else {
                fal.setFActualizacion(cmd.now());
                fal.setUsrActualizacion(cmd.user());
            }

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

            fichaActLaboralService.guardar(fal);
        }

        LOG.info("STEP3-H-OK");
    }

    private FichaOcupacional guardarStep3IExtralaborales(Step3SaveCommand cmd, FichaOcupacional ficha) {
        LOG.info("STEP3-I: Procesando Actividades Extralaborales (SERIALIZADO EN FICHA)");

        if (cmd.tipoAct() == null || cmd.fechaAct() == null || cmd.descAct() == null) {
            LOG.info("STEP3-I: Listas I null -> no se guarda (no rompe)");
            return ficha;
        }

        StringBuilder sb = new StringBuilder();
        Date ultimaFecha = null;

        for (int i = 0; i < cmd.tipoAct().size(); i++) {
            String t = getSafe(cmd.tipoAct(), i);
            Date f = toDate(getSafe(cmd.fechaAct(), i));
            String d = getSafe(cmd.descAct(), i);

            boolean filaTieneDatos = !isBlank(t) || f != null || !isBlank(d);
            if (!filaTieneDatos) {
                continue;
            }

            sb.append(i + 1).append(") ")
                    .append(nullToDash(t)).append(" | ")
                    .append(f != null ? new SimpleDateFormat("yyyy/MM/dd").format(f) : "----/--/--")
                    .append(" | ")
                    .append(nullToDash(d))
                    .append("\n");

            if (f != null) {
                ultimaFecha = f;
            }
        }

        ficha.setExtraLabDesc(sb.length() == 0 ? null : sb.toString().trim());
        ficha.setExtraLabFecha(ultimaFecha);
        ficha.setFechaActualizacion(cmd.now());
        ficha.setUsrActualizacion(cmd.user());

        FichaOcupacional saved = fichaService.guardar(ficha);
        LOG.info("STEP3-I-OK");
        return saved;
    }

    private void guardarStep3JExamenes(Step3SaveCommand cmd, FichaOcupacional ficha) {
        LOG.info("STEP3-J: Procesando Exámenes (FICHA_EXAMEN_COMP)");

        if (cmd.examNombre() == null || cmd.examFecha() == null || cmd.examResultado() == null) {
            LOG.info("STEP3-J: Listas J null -> no se guarda J");
            return;
        }

        int filas = Math.min(cmd.examNombre().size(), Math.min(cmd.examFecha().size(), cmd.examResultado().size()));

        for (int i = 0; i < filas; i++) {
            int nroFila = i + 1;

            String nombre = getSafe(cmd.examNombre(), i);
            Date fecha = toDate(getSafe(cmd.examFecha(), i));
            String resultado = getSafe(cmd.examResultado(), i);

            boolean filaTieneDatos = !isBlank(nombre) || fecha != null || !isBlank(resultado);
            if (!filaTieneDatos) {
                int del = fichaExamenCompService.eliminarPorFichaYFila(ficha.getIdFicha(), nroFila);
                LOG.info("STEP3-J-FILA {}: vacía -> delete={}", nroFila, del);
                continue;
            }

            FichaExamenComp ex = fichaExamenCompService.buscarPorFichaYFila(ficha.getIdFicha(), nroFila);
            if (ex == null) {
                ex = new FichaExamenComp();
                ex.setFicha(ficha);
                ex.setNroFila(nroFila);
                LOG.info("STEP3-J-FILA {}: INSERT", nroFila);
            } else {
                LOG.info("STEP3-J-FILA {}: UPDATE id={}", nroFila, ex.getIdFichaExamen());
            }

            ex.setNombreExamen(nombre);
            ex.setFechaExamen(fecha);
            ex.setResultado(resultado);
            fichaExamenCompService.guardar(ex, cmd.user());
        }

        LOG.info("STEP3-J-OK");
    }

    private void guardarStep3KDiagnosticos(Step3SaveCommand cmd, FichaOcupacional ficha) {
        LOG.info("STEP3-K: Procesando Diagnósticos");

        if (cmd.listaDiag() == null || cmd.listaDiag().isEmpty()) {
            LOG.info("STEP3-K: listaDiag vacía -> OK");
            return;
        }

        try {
            fichaDiagnosticoService.guardarDiagnosticosDeFicha(ficha.getIdFicha(), cmd.listaDiag(), cmd.now(), cmd.user());
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
            String user,
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
}
