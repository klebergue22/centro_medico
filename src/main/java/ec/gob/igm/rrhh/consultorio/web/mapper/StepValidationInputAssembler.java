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
import org.hibernate.Hibernate;

@Stateless
public class StepValidationInputAssembler {

    public Step1ValidationInput buildStep1Input(
            String apellido1,
            String apellido2,
            String nombre1,
            String nombre2,
            Date fechaAtencion,
            String sexo,
            String tipoEval,
            String paStr,
            Integer fc,
            Double peso,
            Double tallaCm,
            SignosVitales signos,
            FichaRiesgo fichaRiesgo,
            FichaOcupacional ficha,
            DatEmpleado empleadoSel,
            Integer noPersonaSel,
            PersonaAux personaAux) {
        Step1ValidationInput input = new Step1ValidationInput();
        populateBasicStep1Fields(input, apellido1, apellido2, nombre1, nombre2, fechaAtencion, sexo, tipoEval);
        populateClinicalStep1Fields(input, paStr, fc, peso, tallaCm, signos, fichaRiesgo);
        populatePatientStep1Fields(input, empleadoSel, noPersonaSel, personaAux, ficha);
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

    private void populateBasicStep1Fields(Step1ValidationInput input, String apellido1, String apellido2,
            String nombre1, String nombre2, Date fechaAtencion, String sexo, String tipoEval) {
        input.apellido1 = apellido1;
        input.apellido2 = apellido2;
        input.nombre1 = nombre1;
        input.nombre2 = nombre2;
        input.fechaAtencion = fechaAtencion;
        input.sexo = sexo;
        input.tipoEval = tipoEval;
    }

    private void populateClinicalStep1Fields(Step1ValidationInput input, String paStr, Integer fc, Double peso,
            Double tallaCm, SignosVitales signos, FichaRiesgo fichaRiesgo) {
        input.paStr = paStr;
        input.fc = fc;
        input.peso = peso;
        input.tallaCm = tallaCm;
        input.signos = signos;
        input.puestoTrabajoCiuo = resolvePuestoTrabajoCiuo(fichaRiesgo);
        input.fichaRiesgo = fichaRiesgo;
    }

    private String resolvePuestoTrabajoCiuo(FichaRiesgo fichaRiesgo) {
        if (fichaRiesgo == null) {
            return null;
        }

        String puestoTrabajo = fichaRiesgo.getPuestoTrabajo();
        if (!isBlank(puestoTrabajo)) {
            return puestoTrabajo;
        }

        FichaOcupacional ficha = fichaRiesgo.getFicha();
        if (ficha == null || !Hibernate.isInitialized(ficha)) {
            return null;
        }

        return ficha.getCiiu();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void populatePatientStep1Fields(Step1ValidationInput input, DatEmpleado empleadoSel, Integer noPersonaSel,
            PersonaAux personaAux, FichaOcupacional ficha) {
        input.empleadoSel = empleadoSel;
        input.noPersonaSel = noPersonaSel;
        input.personaAux = personaAux;
        input.ficha = ficha;
    }
}
