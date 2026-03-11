package ec.gob.igm.rrhh.consultorio.web.validation;

import java.util.List;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;

public class Step3Validator {

    public ValidationResult validate(List<ConsultaDiagnostico> listaDiag,
            String aptitudSel,
            String recomendaciones,
            String medicoNombre,
            String medicoCodigo) {

        ValidationResult result = new ValidationResult();

        if (!hasDiagnostico(listaDiag)) {
            result.addError("Debe registrar al menos un diagnóstico (CIE10).");
        }

        if (isBlank(aptitudSel)) {
            result.addError("Debe seleccionar la aptitud médica.");
        }

        if (isBlank(recomendaciones)) {
            result.addError("Debe ingresar al menos una recomendación.");
        }

        if (isBlank(medicoNombre)) {
            result.addError("Debe ingresar el nombre del profesional.");
        }

        if (isBlank(medicoCodigo)) {
            result.addError("Debe ingresar el código del médico.");
        }

        return result;
    }

    private boolean hasDiagnostico(List<ConsultaDiagnostico> diagnosticos) {
        if (diagnosticos == null) {
            return false;
        }
        for (ConsultaDiagnostico d : diagnosticos) {
            if (d == null) {
                continue;
            }
            if (!isBlank(d.getCodigo()) || !isBlank(d.getDescripcion()) || d.getCie10() != null) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
