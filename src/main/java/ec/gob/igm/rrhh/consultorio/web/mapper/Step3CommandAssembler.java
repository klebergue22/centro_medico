package ec.gob.igm.rrhh.consultorio.web.mapper;

import java.util.Date;
import java.util.List;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.web.service.Step3OrchestratorService;
import jakarta.ejb.Stateless;

@Stateless
public class Step3CommandAssembler {

    public Step3OrchestratorService.Step3SaveCommand toCommand(
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
        return new Step3OrchestratorService.Step3SaveCommand(
                ficha,
                codCie10Ppal,
                obsExamenFisico,
                aptitudSel,
                detalleObservaciones,
                recomendaciones,
                nObsRetiro,
                medicoNombre,
                medicoCodigo,
                fechaEmision,
                now,
                user,
                onEnsurePersonaAuxPersistida,
                onEnsureActLabSize,
                actLabCentroTrabajo,
                actLabActividad,
                actLabTiempo,
                actLabTrabajoAnterior,
                actLabTrabajoActual,
                actLabIncidenteChk,
                actLabAccidenteChk,
                actLabEnfermedadChk,
                iessFecha,
                iessEspecificar,
                actLabObservaciones,
                tipoAct,
                fechaAct,
                descAct,
                examNombre,
                examFecha,
                examResultado,
                listaDiag);
    }
}
