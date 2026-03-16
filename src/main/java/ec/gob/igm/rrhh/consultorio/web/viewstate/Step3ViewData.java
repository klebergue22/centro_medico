package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.util.Date;
import java.util.List;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;

public class Step3ViewData {

    public final FichaOcupacional ficha;
    public final String codCie10Ppal;
    public final String obsExamenFisico;
    public final String aptitudSel;
    public final String detalleObservaciones;
    public final String recomendaciones;
    public final String nObsRetiro;
    public final String medicoNombre;
    public final String medicoCodigo;
    public final Date fechaEmision;
    public final Date now;
    public final String user;
    public final Runnable onEnsurePersonaAuxPersistida;
    public final Runnable onEnsureActLabSize;
    public final List<String> actLabCentroTrabajo;
    public final List<String> actLabActividad;
    public final List<String> actLabTiempo;
    public final List<Boolean> actLabTrabajoAnterior;
    public final List<Boolean> actLabTrabajoActual;
    public final List<Boolean> actLabIncidenteChk;
    public final List<Boolean> actLabAccidenteChk;
    public final List<Boolean> actLabEnfermedadChk;
    public final List<Date> iessFecha;
    public final List<String> iessEspecificar;
    public final List<String> actLabObservaciones;
    public final List<String> tipoAct;
    public final List<Date> fechaAct;
    public final List<String> descAct;
    public final List<String> examNombre;
    public final List<Date> examFecha;
    public final List<String> examResultado;
    public final List<ConsultaDiagnostico> listaDiag;

    public Step3ViewData(FichaOcupacional ficha, String codCie10Ppal, String obsExamenFisico, String aptitudSel,
                         String detalleObservaciones, String recomendaciones, String nObsRetiro,
                         String medicoNombre, String medicoCodigo, Date fechaEmision, Date now, String user,
                         Runnable onEnsurePersonaAuxPersistida, Runnable onEnsureActLabSize,
                         List<String> actLabCentroTrabajo, List<String> actLabActividad, List<String> actLabTiempo,
                         List<Boolean> actLabTrabajoAnterior, List<Boolean> actLabTrabajoActual,
                         List<Boolean> actLabIncidenteChk, List<Boolean> actLabAccidenteChk,
                         List<Boolean> actLabEnfermedadChk, List<Date> iessFecha, List<String> iessEspecificar,
                         List<String> actLabObservaciones, List<String> tipoAct, List<Date> fechaAct,
                         List<String> descAct, List<String> examNombre, List<Date> examFecha,
                         List<String> examResultado, List<ConsultaDiagnostico> listaDiag) {
        this.ficha = ficha;
        this.codCie10Ppal = codCie10Ppal;
        this.obsExamenFisico = obsExamenFisico;
        this.aptitudSel = aptitudSel;
        this.detalleObservaciones = detalleObservaciones;
        this.recomendaciones = recomendaciones;
        this.nObsRetiro = nObsRetiro;
        this.medicoNombre = medicoNombre;
        this.medicoCodigo = medicoCodigo;
        this.fechaEmision = fechaEmision;
        this.now = now;
        this.user = user;
        this.onEnsurePersonaAuxPersistida = onEnsurePersonaAuxPersistida;
        this.onEnsureActLabSize = onEnsureActLabSize;
        this.actLabCentroTrabajo = actLabCentroTrabajo;
        this.actLabActividad = actLabActividad;
        this.actLabTiempo = actLabTiempo;
        this.actLabTrabajoAnterior = actLabTrabajoAnterior;
        this.actLabTrabajoActual = actLabTrabajoActual;
        this.actLabIncidenteChk = actLabIncidenteChk;
        this.actLabAccidenteChk = actLabAccidenteChk;
        this.actLabEnfermedadChk = actLabEnfermedadChk;
        this.iessFecha = iessFecha;
        this.iessEspecificar = iessEspecificar;
        this.actLabObservaciones = actLabObservaciones;
        this.tipoAct = tipoAct;
        this.fechaAct = fechaAct;
        this.descAct = descAct;
        this.examNombre = examNombre;
        this.examFecha = examFecha;
        this.examResultado = examResultado;
        this.listaDiag = listaDiag;
    }
}
