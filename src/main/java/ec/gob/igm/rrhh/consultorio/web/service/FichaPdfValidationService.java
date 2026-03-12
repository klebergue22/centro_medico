package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.persistence.Persistence;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;

@Stateless
public class FichaPdfValidationService implements Serializable {

    public List<String> validar(FichaOcupacional ficha, DatEmpleado empleadoSel, PersonaAux personaAux, boolean permitirIngresoManual) {
        List<String> errores = new ArrayList<>();

        if (ficha == null) {
            errores.add("No existe ficha en memoria.");
            return errores;
        }

        boolean modoAux = permitirIngresoManual || personaAux != null || ficha.getPersonaAux() != null;
        boolean tieneEmpleado = empleadoSel != null || ficha.getEmpleado() != null;
        boolean tienePersonaAux = hasPersonaAux(personaAux, ficha);

        if (!tieneEmpleado && !tienePersonaAux) {
            errores.add("Debe seleccionar un empleado o registrar una persona auxiliar.");
        } else if (modoAux && !tienePersonaAux) {
            errores.add("En modo ingreso manual: falta registrar la persona auxiliar.");
        } else if (!modoAux && !tieneEmpleado) {
            errores.add("Falta seleccionar el empleado.");
        }

        if (ficha.getFechaEvaluacion() == null) {
            errores.add("Falta la fecha de evaluación.");
        }
        if (isBlank(ficha.getTipoEvaluacion())) {
            errores.add("Falta el tipo de evaluación.");
        }
        if (ficha.getSignos() == null) {
            errores.add("Falta registrar signos vitales (Step 3).");
        }
        if (ficha.getIdFicha() == null) {
            errores.add("La ficha aún no se ha guardado.");
        }

        return errores;
    }

    private boolean hasPersonaAux(PersonaAux personaAux, FichaOcupacional ficha) {
        if (personaAux != null && !isBlank(personaAux.getCedula())) {
            return true;
        }
        if (ficha.getPersonaAux() == null) {
            return false;
        }

        try {
            boolean loaded = Persistence.getPersistenceUtil().isLoaded(ficha.getPersonaAux());
            if (!loaded) {
                return true;
            }
            return !isBlank(ficha.getPersonaAux().getCedula());
        } catch (RuntimeException ex) {
            return true;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
