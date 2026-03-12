package ec.gob.igm.rrhh.consultorio.web.validation;

import java.util.List;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;

public class Step2Validator {

    public ValidationResult validate(FichaRiesgo fichaRiesgo, List<String> actividadesLab, List<String> medidasPreventivas) {
        ValidationResult result = new ValidationResult();

        if (fichaRiesgo == null || isBlank(fichaRiesgo.getPuestoTrabajo())) {
            result.addError("Debe ingresar el puesto de trabajo.");
        }

        if (!hasAnyValue(actividadesLab)) {
            result.addError("Debe registrar al menos una actividad laboral.");
        }

        if (!hasAnyValue(medidasPreventivas)) {
            result.addError("Debe registrar al menos una medida preventiva.");
        }

        return result;
    }

    private boolean hasAnyValue(List<String> values) {
        if (values == null) {
            return false;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
