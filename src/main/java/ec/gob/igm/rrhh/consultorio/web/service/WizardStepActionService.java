package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.function.BooleanSupplier;

import org.slf4j.Logger;

import ec.gob.igm.rrhh.consultorio.web.ctrl.ControllerActionTemplate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
/**
 * Coordina la ejecución de acciones de guardado del wizard para evitar
 * wrappers repetitivos en el controlador.
 */
public class WizardStepActionService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ControllerActionTemplate controllerActionTemplate;

    public void execute(ExecuteStepActionCommand cmd) {
        controllerActionTemplate.execute(
                cmd.actionName,
                () -> {
                    if (cmd.preValidation != null && !cmd.preValidation.getAsBoolean()) {
                        return false;
                    }
                    cmd.saveAction.run();
                    if (cmd.postSaveAction != null) {
                        cmd.postSaveAction.run();
                    }
                    return true;
                },
                cmd.onSuccess,
                cmd.logger,
                cmd.activeStep,
                cmd.noPersonaSel,
                cmd.cedulaBusqueda);
    }

    public static class ExecuteStepActionCommand {
        public final String actionName;
        public final BooleanSupplier preValidation;
        public final Runnable saveAction;
        public final Runnable postSaveAction;
        public final Runnable onSuccess;
        public final Logger logger;
        public final String activeStep;
        public final Integer noPersonaSel;
        public final String cedulaBusqueda;

        public ExecuteStepActionCommand(
                String actionName,
                BooleanSupplier preValidation,
                Runnable saveAction,
                Runnable postSaveAction,
                Runnable onSuccess,
                Logger logger,
                String activeStep,
                Integer noPersonaSel,
                String cedulaBusqueda) {
            this.actionName = actionName;
            this.preValidation = preValidation;
            this.saveAction = saveAction;
            this.postSaveAction = postSaveAction;
            this.onSuccess = onSuccess;
            this.logger = logger;
            this.activeStep = activeStep;
            this.noPersonaSel = noPersonaSel;
            this.cedulaBusqueda = cedulaBusqueda;
        }
    }
}
