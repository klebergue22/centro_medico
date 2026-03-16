package ec.gob.igm.rrhh.consultorio.web.ctrl;

import java.io.Serializable;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;

import org.slf4j.Logger;

import ec.gob.igm.rrhh.consultorio.web.jsf.CentroMedicoMessageService;
import jakarta.inject.Inject;

import java.util.function.Consumer;
import java.util.function.Supplier;

@ApplicationScoped
/**
 * Class ControllerActionTemplate: controla las acciones de la interfaz web.
 */
public class ControllerActionTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CentroMedicoMessageService messageService;

    public void execute(String actionName,
                        Supplier<Boolean> action,
                        Runnable onSuccess,
                        Logger logger,
                        String activeStep,
                        Integer noPersonaSel,
                        String cedulaBusqueda) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            boolean completed = action.get();
            if (completed && onSuccess != null) {
                onSuccess.run();
            }
        } catch (CentroMedicoCtrl.BusinessValidationException ex) {
            messageService.warn(ex.getMessage());
            markValidationFailed(ctx);
        } catch (RuntimeException ex) {
            messageService.handleUnexpected(logger, actionName, ex, activeStep, noPersonaSel, cedulaBusqueda);
            markValidationFailed(ctx);
        }
    }

    public <T> T executeWithResult(String actionName,
                                   Supplier<T> action,
                                   Consumer<T> onSuccess,
                                   Logger logger,
                                   String activeStep,
                                   Integer noPersonaSel,
                                   String cedulaBusqueda) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        try {
            T result = action.get();
            if (onSuccess != null) {
                onSuccess.accept(result);
            }
            return result;
        } catch (CentroMedicoCtrl.BusinessValidationException ex) {
            messageService.warn(ex.getMessage());
            markValidationFailed(ctx);
        } catch (RuntimeException ex) {
            messageService.handleUnexpected(logger, actionName, ex, activeStep, noPersonaSel, cedulaBusqueda);
            markValidationFailed(ctx);
        }
        return null;
    }

    private void markValidationFailed(FacesContext ctx) {
        if (ctx != null) {
            ctx.validationFailed();
        }
    }
}
