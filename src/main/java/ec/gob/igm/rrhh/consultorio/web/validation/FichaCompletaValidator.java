package ec.gob.igm.rrhh.consultorio.web.validation;

import java.util.Date;
import java.util.function.Supplier;

import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import jakarta.persistence.Persistence;

/**
 * Class FichaCompletaValidator: valida datos y reglas del flujo de formularios.
 */
public class FichaCompletaValidator {

    public ValidationResult validate(FichaOcupacional ficha,
            boolean permitirIngresoManual,
            PersonaAux personaAux,
            DatEmpleado empleadoSel,
            String aptitudSel,
            Date fechaEmision,
            Supplier<Cie10> cie10InferidoSupplier) {

        ValidationResult result = new ValidationResult();

        if (ficha == null || ficha.getIdFicha() == null) {
            result.addError("La ficha ocupacional aún no se ha guardado (Steps 1 y 3).");
            return result;
        }

        boolean tieneEmpleado = (empleadoSel != null) || (ficha.getEmpleado() != null);
        boolean tienePersonaAux = hasPersonaAux(personaAux, ficha.getPersonaAux());
        boolean modoAux = !tieneEmpleado && (permitirIngresoManual || tienePersonaAux);

        if (modoAux) {
            if (hasPersonaAuxReference(personaAux)) {
                ficha.setPersonaAux(personaAux);
            }
            ficha.setEmpleado(null);
        } else if (empleadoSel != null) {
            ficha.setEmpleado(empleadoSel);
        }

        if (isBlank(ficha.getAptitudSel()) && !isBlank(aptitudSel)) {
            ficha.setAptitudSel(aptitudSel);
        }

        if (ficha.getFechaEmision() == null) {
            ficha.setFechaEmision(fechaEmision != null ? fechaEmision : new Date());
        }

        if (!tieneEmpleado && !tienePersonaAux) {
            result.addError("Debe seleccionar un empleado o registrar una persona auxiliar.");
        } else if (modoAux && !tieneEmpleado && !tienePersonaAux) {
            result.addError("En modo ingreso manual: falta registrar la persona auxiliar.");
        } else if (!modoAux && !tieneEmpleado) {
            result.addError("Falta seleccionar el empleado.");
        }

        if (ficha.getFechaEvaluacion() == null) {
            result.addError("Falta la fecha de evaluación.");
        }

        if (isBlank(ficha.getTipoEvaluacion())) {
            result.addError("Falta el tipo de evaluación (INGRESO/PERÍODICA/etc.).");
        }

        if (isBlank(ficha.getAptitudSel())) {
            result.addError("Debe seleccionar la aptitud médica.");
        }

        if (ficha.getCie10Principal() == null || isBlank(ficha.getCie10Principal().getCodigo())) {
            Cie10 inferido = cie10InferidoSupplier.get();
            if (inferido != null && !isBlank(inferido.getCodigo())) {
                ficha.setCie10Principal(inferido);
            }
        }

        if (ficha.getCie10Principal() == null || isBlank(ficha.getCie10Principal().getCodigo())) {
            result.addError("Debe registrar un diagnóstico CIE10 principal.");
        }

        if (ficha.getSignos() == null) {
            result.addError("Debe registrar signos vitales (peso/talla) en Step 3.");
        }

        if (ficha.getFechaEmision() == null) {
            result.addError("Falta la fecha de emisión del certificado.");
        }

        return result;
    }

    private boolean hasPersonaAux(PersonaAux personaAuxCtrl, PersonaAux personaAuxFicha) {
        if (hasPersonaAuxReference(personaAuxCtrl)) {
            return true;
        }

        if (personaAuxFicha != null) {
            try {
                boolean loaded = Persistence.getPersistenceUtil().isLoaded(personaAuxFicha, "cedula");
                if (loaded) {
                    return !isBlank(personaAuxFicha.getCedula());
                }
                return true;
            } catch (RuntimeException ex) {
                return true;
            }
        }

        return false;
    }

    private boolean hasPersonaAuxReference(PersonaAux personaAux) {
        if (personaAux == null) {
            return false;
        }
        if (hasCedulaLoaded(personaAux)) {
            return true;
        }
        try {
            return personaAux.getIdPersonaAux() != null;
        } catch (RuntimeException ex) {
            return true;
        }
    }

    private boolean hasCedulaLoaded(PersonaAux personaAux) {
        if (personaAux == null) {
            return false;
        }
        try {
            boolean loaded = Persistence.getPersistenceUtil().isLoaded(personaAux, "cedula");
            if (!loaded) {
                return false;
            }
            return !isBlank(personaAux.getCedula());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
