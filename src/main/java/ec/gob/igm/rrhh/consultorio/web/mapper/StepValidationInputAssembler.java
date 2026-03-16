package ec.gob.igm.rrhh.consultorio.web.mapper;

import java.util.Date;
import java.util.function.Supplier;

import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoValidationCoordinator.FichaCompletaValidationInput;
import ec.gob.igm.rrhh.consultorio.web.service.CentroMedicoValidationCoordinator.Step1ValidationInput;
import jakarta.ejb.Stateless;

@Stateless
public class StepValidationInputAssembler {

    public Step1ValidationInput buildStep1Input(
            String apellido1,
            String apellido2,
            String nombre1,
            String nombre2,
            String sexo,
            String tipoEval,
            String paStr,
            Integer fc,
            Double peso,
            Double tallaCm,
            SignosVitales signos,
            FichaRiesgo fichaRiesgo) {
        Step1ValidationInput input = new Step1ValidationInput();
        input.apellido1 = apellido1;
        input.apellido2 = apellido2;
        input.nombre1 = nombre1;
        input.nombre2 = nombre2;
        input.sexo = sexo;
        input.tipoEval = tipoEval;
        input.paStr = paStr;
        input.fc = fc;
        input.peso = peso;
        input.tallaCm = tallaCm;
        input.signos = signos;
        input.puestoTrabajoCiuo = fichaRiesgo != null ? fichaRiesgo.getPuestoTrabajo() : null;
        input.fichaRiesgo = fichaRiesgo;
        return input;
    }

    public FichaCompletaValidationInput buildFichaCompletaInput(
            FichaOcupacional ficha,
            boolean permitirIngresoManual,
            PersonaAux personaAux,
            DatEmpleado empleadoSel,
            String aptitudSel,
            Date fechaEmision,
            Supplier<Cie10> cie10PrincipalSupplier) {
        FichaCompletaValidationInput input = new FichaCompletaValidationInput();
        input.ficha = ficha;
        input.permitirIngresoManual = permitirIngresoManual;
        input.personaAux = personaAux;
        input.empleadoSel = empleadoSel;
        input.aptitudSel = aptitudSel;
        input.fechaEmision = fechaEmision;
        input.cie10PrincipalSupplier = cie10PrincipalSupplier;
        return input;
    }
}

