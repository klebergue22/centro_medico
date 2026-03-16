package ec.gob.igm.rrhh.consultorio.web.mapper;

import ec.gob.igm.rrhh.consultorio.web.service.Step3OrchestratorService;
import ec.gob.igm.rrhh.consultorio.web.viewstate.Step3ViewData;
import jakarta.ejb.Stateless;

@Stateless
public class Step3CommandAssembler {

    public Step3OrchestratorService.Step3SaveCommand toCommand(Step3ViewData viewData) {
        return new Step3OrchestratorService.Step3SaveCommand(
                viewData.ficha,
                viewData.codCie10Ppal,
                viewData.obsExamenFisico,
                viewData.aptitudSel,
                viewData.detalleObservaciones,
                viewData.recomendaciones,
                viewData.nObsRetiro,
                viewData.medicoNombre,
                viewData.medicoCodigo,
                viewData.fechaEmision,
                viewData.now,
                viewData.user,
                viewData.onEnsurePersonaAuxPersistida,
                viewData.onEnsureActLabSize,
                viewData.actLabCentroTrabajo,
                viewData.actLabActividad,
                viewData.actLabTiempo,
                viewData.actLabTrabajoAnterior,
                viewData.actLabTrabajoActual,
                viewData.actLabIncidenteChk,
                viewData.actLabAccidenteChk,
                viewData.actLabEnfermedadChk,
                viewData.iessFecha,
                viewData.iessEspecificar,
                viewData.actLabObservaciones,
                viewData.tipoAct,
                viewData.fechaAct,
                viewData.descAct,
                viewData.examNombre,
                viewData.examFecha,
                viewData.examResultado,
                viewData.listaDiag);
    }
}

