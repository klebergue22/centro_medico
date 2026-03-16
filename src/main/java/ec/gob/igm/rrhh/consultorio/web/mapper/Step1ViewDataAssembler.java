package ec.gob.igm.rrhh.consultorio.web.mapper;

import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import ec.gob.igm.rrhh.consultorio.web.viewstate.Step1ViewData;
import jakarta.ejb.Stateless;

@Stateless
public class Step1ViewDataAssembler {

    public Step1ViewData capture(CentroMedicoCtrl source, String usuario) {
        return new Step1ViewData(
                source.getFicha(), source.getEmpleadoSel(), source.getPersonaAux(), source.getSignos(), source.getNoPersonaSel(),
                source.getFechaAtencion(), source.getTipoEval(), source.getPaStr(), source.getTemp(), source.getFc(), source.getFr(), source.getSatO2(),
                source.getPeso(), source.getTallaCm(), source.getPerimetroAbd(), source.isApEmbarazada(), source.isApDiscapacidad(), source.isApCatastrofica(), source.isApLactancia(), source.isApAdultoMayor(),
                source.getAntClinicoQuirurgico(), source.getAntFamiliares(), source.getCondicionEspecial(), source.getAutorizaTransfusion(), source.getTratamientoHormonal(),
                source.getTratamientoHormonalCual(), source.getExamenReproMasculino(), source.getTiempoReproMasculino(), source.getGinecoExamen1(), source.getGinecoTiempo1(),
                source.getGinecoResultado1(), source.getGinecoExamen2(), source.getGinecoTiempo2(), source.getGinecoResultado2(), source.getGinecoObservacion(), source.getFum(), source.getGestas(),
                source.getPartos(), source.getCesareas(), source.getAbortos(), source.getPlanificacion(), source.getPlanificacionCual(), source.getDiscapTipo(), source.getDiscapDesc(), source.getDiscapPorc(),
                source.getCatasDiagnostico(), source.getCatasCalificada(), source.getNRealizaEvaluacion(), source.getNRelacionTrabajo(), source.getNObsRetiro(),
                source.getConsTiempoConsumoMeses(), source.getConsExConsumidor(), source.getConsTiempoAbstinenciaMeses(), source.getConsNoConsume(), source.getConsOtrasCual(),
                source.getAfCual(), source.getAfTiempo(), source.getMedCual(), source.getMedCant(), source.getConsumoVidaCondObs(), usuario);
    }
}
