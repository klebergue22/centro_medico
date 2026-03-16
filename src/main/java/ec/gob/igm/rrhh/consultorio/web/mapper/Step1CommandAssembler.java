package ec.gob.igm.rrhh.consultorio.web.mapper;

import ec.gob.igm.rrhh.consultorio.service.Step1FichaService;
import ec.gob.igm.rrhh.consultorio.web.viewstate.Step1ViewData;
import jakarta.ejb.Stateless;

@Stateless
/**
 * Class Step1CommandAssembler: mapea datos entre modelos de vista y comandos.
 */
public class Step1CommandAssembler {

    public Step1FichaService.Step1Command toCommand(Step1ViewData viewData) {
        return new Step1FichaService.Step1Command(
                viewData.ficha,
                viewData.empleadoSel,
                viewData.personaAux,
                viewData.signos,
                viewData.noPersonaSel,
                viewData.fechaAtencion,
                viewData.tipoEval,
                viewData.paStr,
                viewData.temp,
                viewData.fc,
                viewData.fr,
                viewData.satO2,
                viewData.peso,
                viewData.tallaCm,
                viewData.perimetroAbd,
                viewData.apEmbarazada,
                viewData.apDiscapacidad,
                viewData.apCatastrofica,
                viewData.apLactancia,
                viewData.apAdultoMayor,
                viewData.antClinicoQuirurgico,
                viewData.antFamiliares,
                viewData.condicionEspecial,
                viewData.autorizaTransfusion,
                viewData.tratamientoHormonal,
                viewData.tratamientoHormonalCual,
                viewData.examenReproMasculino,
                viewData.tiempoReproMasculino,
                viewData.ginecoExamen1,
                viewData.ginecoTiempo1,
                viewData.ginecoResultado1,
                viewData.ginecoExamen2,
                viewData.ginecoTiempo2,
                viewData.ginecoResultado2,
                viewData.ginecoObservacion,
                viewData.fum,
                viewData.gestas,
                viewData.partos,
                viewData.cesareas,
                viewData.abortos,
                viewData.planificacion,
                viewData.planificacionCual,
                viewData.discapTipo,
                viewData.discapDesc,
                viewData.discapPorc,
                viewData.catasDiagnostico,
                viewData.catasCalificada,
                viewData.nRealizaEvaluacion,
                viewData.nRelacionTrabajo,
                viewData.nObsRetiro,
                viewData.consTiempoConsumoMeses,
                viewData.consExConsumidor,
                viewData.consTiempoAbstinenciaMeses,
                viewData.consNoConsume,
                viewData.consOtrasCual,
                viewData.afCual,
                viewData.afTiempo,
                viewData.medCual,
                viewData.medCant,
                viewData.consumoVidaCondObs,
                viewData.usuario);
    }
}

