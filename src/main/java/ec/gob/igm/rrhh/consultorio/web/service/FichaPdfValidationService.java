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
/**
 * Class FichaPdfValidationService: orquesta la logica de presentacion y flujo web.
 */
public class FichaPdfValidationService implements Serializable {

    public List<String> validar(FichaOcupacional ficha, DatEmpleado empleadoSel, PersonaAux personaAux, boolean permitirIngresoManual) {
        List<String> errores = new ArrayList<>();

        if (ficha == null) {
            errores.add("No existe ficha en memoria.");
            return errores;
        }

        validarPacienteSeleccionado(errores, ficha, empleadoSel, personaAux, permitirIngresoManual);
        validarFichaPersistida(errores, ficha);
        return errores;
    }

    private boolean hasPersonaAux(PersonaAux personaAux, FichaOcupacional ficha) {
        if (hasCedulaLoaded(personaAux)) {
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

    private boolean hasCedulaLoaded(PersonaAux personaAux) {
        if (personaAux == null) {
            return false;
        }

        try {
            boolean loaded = Persistence.getPersistenceUtil().isLoaded(personaAux, "cedula");
            if (!loaded) {
                return true;
            }
            return !isBlank(personaAux.getCedula());
        } catch (RuntimeException ex) {
            return true;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void validarPacienteSeleccionado(List<String> errores, FichaOcupacional ficha, DatEmpleado empleadoSel,
            PersonaAux personaAux, boolean permitirIngresoManual) {
        boolean modoAux = permitirIngresoManual || personaAux != null || ficha.getPersonaAux() != null;
        boolean tieneEmpleado = empleadoSel != null || ficha.getEmpleado() != null;
        boolean tienePersonaAux = hasPersonaAux(personaAux, ficha);

        if (!tieneEmpleado && !tienePersonaAux) {
            errores.add("Debe seleccionar un empleado o registrar una persona auxiliar.");
            return;
        }
        if (modoAux && !tieneEmpleado && !tienePersonaAux) {
            errores.add("En modo ingreso manual: falta registrar la persona auxiliar.");
        } else if (!modoAux && !tieneEmpleado) {
            errores.add("Falta seleccionar el empleado.");
        }
    }

    private void validarFichaPersistida(List<String> errores, FichaOcupacional ficha) {
        if (ficha.getFechaEvaluacion() == null) {
            errores.add("Falta la fecha de evaluacion.");
        }
        if (isBlank(ficha.getTipoEvaluacion())) {
            errores.add("Falta el tipo de evaluacion.");
        }
        if (ficha.getSignos() == null) {
            errores.add("Falta registrar signos vitales (Step 3).");
        }
        if (ficha.getIdFicha() == null) {
            errores.add("La ficha aun no se ha guardado.");
        }
    }
}
