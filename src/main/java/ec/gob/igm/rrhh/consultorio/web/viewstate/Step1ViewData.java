package ec.gob.igm.rrhh.consultorio.web.viewstate;

import java.util.Date;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;

/**
 * Class Step1ViewData: contiene la lógica de la aplicación.
 */
public class Step1ViewData {

    public final FichaOcupacional ficha;
    public final DatEmpleado empleadoSel;
    public final PersonaAux personaAux;
    public final SignosVitales signos;
    public final Integer noPersonaSel;
    public final Date fechaAtencion;
    public final String tipoEval;
    public final Date fecIngreso;
    public final Date fecReintegro;
    public final Date fecRetiro;
    public final String motivoObs;
    public final String paStr;
    public final Double temp;
    public final Integer fc;
    public final Integer fr;
    public final Integer satO2;
    public final Double peso;
    public final Double tallaCm;
    public final Double perimetroAbd;
    public final Boolean apEmbarazada;
    public final Boolean apDiscapacidad;
    public final Boolean apCatastrofica;
    public final Boolean apLactancia;
    public final Boolean apAdultoMayor;
    public final String antClinicoQuirurgico;
    public final String antFamiliares;
    public final String condicionEspecial;
    public final String autorizaTransfusion;
    public final String tratamientoHormonal;
    public final String tratamientoHormonalCual;
    public final String examenReproMasculino;
    public final Integer tiempoReproMasculino;
    public final String ginecoExamen1;
    public final String ginecoTiempo1;
    public final String ginecoResultado1;
    public final String ginecoExamen2;
    public final String ginecoTiempo2;
    public final String ginecoResultado2;
    public final String ginecoObservacion;
    public final Date fum;
    public final Integer gestas;
    public final Integer partos;
    public final Integer cesareas;
    public final Integer abortos;
    public final String planificacion;
    public final String planificacionCual;
    public final String discapTipo;
    public final String discapDesc;
    public final Integer discapPorc;
    public final String catasDiagnostico;
    public final Boolean catasCalificada;
    public final String nRealizaEvaluacion;
    public final String nRelacionTrabajo;
    public final String nObsRetiro;
    public final Integer[] consTiempoConsumoMeses;
    public final Boolean[] consExConsumidor;
    public final Integer[] consTiempoAbstinenciaMeses;
    public final Boolean[] consNoConsume;
    public final String consOtrasCual;
    public final String[] afCual;
    public final String[] afTiempo;
    public final String[] medCual;
    public final Integer[] medCant;
    public final String consumoVidaCondObs;
    public final String usuario;

    public Step1ViewData(FichaOcupacional ficha, DatEmpleado empleadoSel, PersonaAux personaAux, SignosVitales signos,
                         Integer noPersonaSel, Date fechaAtencion, String tipoEval, Date fecIngreso,
                         Date fecReintegro, Date fecRetiro, String motivoObs, String paStr, Double temp,
                         Integer fc, Integer fr, Integer satO2, Double peso, Double tallaCm, Double perimetroAbd,
                         Boolean apEmbarazada, Boolean apDiscapacidad, Boolean apCatastrofica, Boolean apLactancia,
                         Boolean apAdultoMayor, String antClinicoQuirurgico, String antFamiliares,
                         String condicionEspecial, String autorizaTransfusion, String tratamientoHormonal,
                         String tratamientoHormonalCual, String examenReproMasculino, Integer tiempoReproMasculino,
                         String ginecoExamen1, String ginecoTiempo1, String ginecoResultado1, String ginecoExamen2,
                         String ginecoTiempo2, String ginecoResultado2, String ginecoObservacion, Date fum,
                         Integer gestas, Integer partos, Integer cesareas, Integer abortos, String planificacion,
                         String planificacionCual, String discapTipo, String discapDesc, Integer discapPorc,
                         String catasDiagnostico, Boolean catasCalificada, String nRealizaEvaluacion,
                         String nRelacionTrabajo, String nObsRetiro, Integer[] consTiempoConsumoMeses,
                         Boolean[] consExConsumidor, Integer[] consTiempoAbstinenciaMeses, Boolean[] consNoConsume,
                         String consOtrasCual, String[] afCual, String[] afTiempo, String[] medCual,
                         Integer[] medCant, String consumoVidaCondObs, String usuario) {
        this.ficha = ficha;
        this.empleadoSel = empleadoSel;
        this.personaAux = personaAux;
        this.signos = signos;
        this.noPersonaSel = noPersonaSel;
        this.fechaAtencion = fechaAtencion;
        this.tipoEval = tipoEval;
        this.fecIngreso = fecIngreso;
        this.fecReintegro = fecReintegro;
        this.fecRetiro = fecRetiro;
        this.motivoObs = motivoObs;
        this.paStr = paStr;
        this.temp = temp;
        this.fc = fc;
        this.fr = fr;
        this.satO2 = satO2;
        this.peso = peso;
        this.tallaCm = tallaCm;
        this.perimetroAbd = perimetroAbd;
        this.apEmbarazada = apEmbarazada;
        this.apDiscapacidad = apDiscapacidad;
        this.apCatastrofica = apCatastrofica;
        this.apLactancia = apLactancia;
        this.apAdultoMayor = apAdultoMayor;
        this.antClinicoQuirurgico = antClinicoQuirurgico;
        this.antFamiliares = antFamiliares;
        this.condicionEspecial = condicionEspecial;
        this.autorizaTransfusion = autorizaTransfusion;
        this.tratamientoHormonal = tratamientoHormonal;
        this.tratamientoHormonalCual = tratamientoHormonalCual;
        this.examenReproMasculino = examenReproMasculino;
        this.tiempoReproMasculino = tiempoReproMasculino;
        this.ginecoExamen1 = ginecoExamen1;
        this.ginecoTiempo1 = ginecoTiempo1;
        this.ginecoResultado1 = ginecoResultado1;
        this.ginecoExamen2 = ginecoExamen2;
        this.ginecoTiempo2 = ginecoTiempo2;
        this.ginecoResultado2 = ginecoResultado2;
        this.ginecoObservacion = ginecoObservacion;
        this.fum = fum;
        this.gestas = gestas;
        this.partos = partos;
        this.cesareas = cesareas;
        this.abortos = abortos;
        this.planificacion = planificacion;
        this.planificacionCual = planificacionCual;
        this.discapTipo = discapTipo;
        this.discapDesc = discapDesc;
        this.discapPorc = discapPorc;
        this.catasDiagnostico = catasDiagnostico;
        this.catasCalificada = catasCalificada;
        this.nRealizaEvaluacion = nRealizaEvaluacion;
        this.nRelacionTrabajo = nRelacionTrabajo;
        this.nObsRetiro = nObsRetiro;
        this.consTiempoConsumoMeses = consTiempoConsumoMeses;
        this.consExConsumidor = consExConsumidor;
        this.consTiempoAbstinenciaMeses = consTiempoAbstinenciaMeses;
        this.consNoConsume = consNoConsume;
        this.consOtrasCual = consOtrasCual;
        this.afCual = afCual;
        this.afTiempo = afTiempo;
        this.medCual = medCual;
        this.medCant = medCant;
        this.consumoVidaCondObs = consumoVidaCondObs;
        this.usuario = usuario;
    }
}
