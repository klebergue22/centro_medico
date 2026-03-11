package ec.gob.igm.rrhh.consultorio.web.validation;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;
import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;

public class Step1Validator {

    public ValidationResult validate(String apellido1, String apellido2,
            String nombre1, String nombre2,
            String sexo, String tipoEval,
            String paStr, Integer fc, Double peso, Double tallaCm,
            SignosVitales signos, FichaRiesgo fichaRiesgo) {

        ValidationResult result = new ValidationResult();

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
            result.addError("Debe seleccionar el tipo de evaluación (Ingreso, Periódica, etc.).");
        }

        if (signos == null) {
            result.addError("Debe registrar los signos vitales.");
            return result;
        }

        if (isBlank(paStr)
                && (signos.getPaSistolica() == null || signos.getPaDiastolica() == null)) {
            result.addError("Debe ingresar la presión arterial completa (PA sistólica y diastólica).");
        }

        if (fc == null && signos.getFrecuenciaCard() == null) {
            result.addError("Debe ingresar la frecuencia cardíaca (FC).");
        }

        if (peso == null && signos.getPesoKg() == null) {
            result.addError("Debe ingresar el peso (kg).");
        }

        if (tallaCm == null && signos.getTallaM() == null) {
            result.addError("Debe ingresar la talla (en metros o convertir desde cm).");
        }

        return result;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
