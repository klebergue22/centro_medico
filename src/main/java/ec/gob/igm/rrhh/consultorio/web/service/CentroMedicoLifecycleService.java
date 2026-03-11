package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
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
