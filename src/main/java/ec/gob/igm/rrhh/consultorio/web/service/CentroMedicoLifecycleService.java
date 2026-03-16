package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
/**
 * Class CentroMedicoLifecycleService: orquesta la lógica de presentación y flujo web.
 */
public class CentroMedicoLifecycleService implements Serializable {

    private static final long serialVersionUID = 1L;

    public LifecycleState resolve(String activeStep, boolean empleadoSeleccionado, boolean preRenderDone) {
        boolean mostrarDlgCedula = "step1".equals(activeStep) && !empleadoSeleccionado;
        boolean requiereInicializacion = !preRenderDone;
        return new LifecycleState(mostrarDlgCedula, requiereInicializacion);
    }

    public record LifecycleState(boolean mostrarDlgCedula, boolean requiereInicializacion) {
    }
}
