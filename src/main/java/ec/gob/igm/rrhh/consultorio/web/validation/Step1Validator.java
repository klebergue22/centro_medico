package ec.gob.igm.rrhh.consultorio.web.validation;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;

import java.util.Date;

/**
 * Class Step1Validator: valida datos y reglas del flujo de formularios.
 */
public class Step1Validator {

    public ValidationResult validate(String apellido1, String apellido2,
            String nombre1, String nombre2,
            Date fechaAtencion,
            String sexo, String tipoEval,
            String paStr, Integer fc, Double peso, Double tallaCm,
            SignosVitales signos, String puestoTrabajoCiuo, FichaRiesgo fichaRiesgo,
            DatEmpleado empleadoSel, Integer noPersonaSel, PersonaAux personaAux) {

        ValidationResult result = new ValidationResult();
        PersistedVitalSigns persistedVitalSigns = resolvePersistedVitalSigns(signos);

        validateBasicFields(result, apellido1, apellido2, nombre1, nombre2, fechaAtencion, sexo, tipoEval);
        validateVitalSigns(result, paStr, fc, peso, tallaCm, persistedVitalSigns);
        validatePuestoTrabajo(result, puestoTrabajoCiuo, fichaRiesgo);
        validatePatientSelection(result, empleadoSel, noPersonaSel, personaAux);
        return result;
    }

    private void validateBasicFields(ValidationResult result, String apellido1, String apellido2, String nombre1,
            String nombre2, Date fechaAtencion, String sexo, String tipoEval) {
        if (fechaAtencion == null) {
            result.addError("Debe ingresar la fecha de atenciÃ³n.");
        }
        if (isBlank(apellido1) && isBlank(apellido2)) {
            result.addError("Debe ingresar al menos un apellido.");
        }
        if (isBlank(nombre1) && isBlank(nombre2)) {
            result.addError("Debe ingresar al menos un nombre.");
        }
        if (isBlank(sexo)) {
            result.addError("Debe seleccionar el sexo del paciente.");
        }
        if (isBlank(tipoEval)) {
            result.addError("Debe seleccionar el tipo de evaluaciÃ³n (Ingreso, PeriÃ³dica, etc.).");
        }
    }

    private PersistedVitalSigns resolvePersistedVitalSigns(SignosVitales signos) {
        return new PersistedVitalSigns(
                signos != null && signos.getPaSistolica() != null && signos.getPaDiastolica() != null,
                signos != null && signos.getFrecuenciaCard() != null,
                signos != null && signos.getPesoKg() != null,
                signos != null && signos.getTallaM() != null);
    }

    private void validateVitalSigns(ValidationResult result, String paStr, Integer fc, Double peso, Double tallaCm,
            PersistedVitalSigns persistedVitalSigns) {
        if (isBlank(paStr) && !persistedVitalSigns.tienePaPersistida()) {
            result.addError("Debe ingresar la presiÃ³n arterial completa (PA sistÃ³lica y diastÃ³lica).");
        }
        if (fc == null && !persistedVitalSigns.tieneFcPersistida()) {
            result.addError("Debe ingresar la frecuencia cardÃ­aca (FC).");
        }
        if (peso == null && !persistedVitalSigns.tienePesoPersistido()) {
            result.addError("Debe ingresar el peso (kg).");
        }
        if (tallaCm == null && !persistedVitalSigns.tieneTallaPersistida()) {
            result.addError("Debe ingresar la talla (en metros o convertir desde cm).");
        }
    }

    private void validatePuestoTrabajo(ValidationResult result, String puestoTrabajoCiuo, FichaRiesgo fichaRiesgo) {
        boolean tienePuestoTrabajo = !isBlank(puestoTrabajoCiuo)
                || (fichaRiesgo != null && !isBlank(fichaRiesgo.getPuestoTrabajo()));
        if (!tienePuestoTrabajo) {
            result.addError("Debe ingresar el puesto de trabajo.");
        }
    }

    private void validatePatientSelection(ValidationResult result, DatEmpleado empleadoSel, Integer noPersonaSel,
            PersonaAux personaAux) {
        if (empleadoSel != null || noPersonaSel != null) {
            return;
        }
        if (personaAux == null || isBlank(personaAux.getCedula())) {
            result.addError("Debe seleccionar un empleado de RRHH o registrar una persona auxiliar (cÃ©dula obligatoria).");
        }
        boolean faltanDatosPersonaAux = personaAux == null
                || isBlank(personaAux.getApellido1())
                || isBlank(personaAux.getNombre1())
                || isBlank(personaAux.getSexo());
        if (faltanDatosPersonaAux) {
            result.addError("En Persona Auxiliar: primer apellido, primer nombre y sexo son obligatorios.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private record PersistedVitalSigns(boolean tienePaPersistida, boolean tieneFcPersistida,
            boolean tienePesoPersistido, boolean tieneTallaPersistida) {
    }
}
