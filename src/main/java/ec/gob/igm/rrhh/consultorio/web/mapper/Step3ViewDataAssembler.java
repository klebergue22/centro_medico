package ec.gob.igm.rrhh.consultorio.web.mapper;

import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import ec.gob.igm.rrhh.consultorio.web.viewstate.Step3ViewData;
import jakarta.ejb.Stateless;
import java.util.Date;

@Stateless
public class Step3ViewDataAssembler {

    public Step3ViewData capture(CentroMedicoCtrl source, Date now, Runnable asegurarPersonaAuxPersistida,
            Runnable ensureActLabSize) {
        return new Step3ViewData(
                source.getFicha(), source.getCodCie10Ppal(), source.getObsExamenFisico(), source.getAptitudSel(),
                source.getDetalleObservaciones(), source.getRecomendaciones(), source.getNObsRetiro(),
                source.getMedicoNombre(), source.getMedicoCodigo(), source.getFechaEmision(), now,
                asegurarPersonaAuxPersistida,
                ensureActLabSize,
                source.getActLabCentroTrabajo(), source.getActLabActividad(), source.getActLabTiempo(), source.getActLabTrabajoAnterior(), source.getActLabTrabajoActual(),
                source.getActLabIncidenteChk(), source.getActLabAccidenteChk(), source.getActLabEnfermedadChk(), source.getIessFecha(), source.getIessEspecificar(),
                source.getActLabObservaciones(), source.getTipoAct(), source.getFechaAct(), source.getDescAct(), source.getExamNombre(), source.getExamFecha(), source.getExamResultado(),
                source.getListaDiag());
    }
}
