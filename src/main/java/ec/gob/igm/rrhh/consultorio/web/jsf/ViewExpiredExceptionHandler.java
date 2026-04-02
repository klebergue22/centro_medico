package ec.gob.igm.rrhh.consultorio.web.jsf;

import java.io.IOException;
import java.util.Iterator;
import jakarta.faces.FacesException;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;

/**
 * Captura ViewExpiredException y redirige a la misma vista con un parámetro
 * para mostrar un diálogo amigable en lugar del error técnico.
 */
public class ViewExpiredExceptionHandler extends ExceptionHandlerWrapper {

    private final ExceptionHandler wrapped;

    public ViewExpiredExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return wrapped;
    }

    @Override
    public void handle() {
        FacesContext context = FacesContext.getCurrentInstance();
        Iterator<ExceptionQueuedEvent> events = getUnhandledExceptionQueuedEvents().iterator();

        while (events.hasNext()) {
            ExceptionQueuedEvent event = events.next();
            ExceptionQueuedEventContext eventContext = (ExceptionQueuedEventContext) event.getSource();
            Throwable exception = eventContext.getException();
            ViewExpiredException viewExpiredException = extractViewExpiredException(exception);

            if (viewExpiredException == null) {
                continue;
            }

            events.remove();
            redirectToFreshView(context, viewExpiredException.getViewId());
            return;
        }

        wrapped.handle();
    }

    private ViewExpiredException extractViewExpiredException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ViewExpiredException) {
                return (ViewExpiredException) current;
            }
            current = current.getCause();
        }
        return null;
    }

    private void redirectToFreshView(FacesContext context, String viewId) {
        if (context == null || context.getExternalContext() == null) {
            throw new FacesException("No se pudo obtener FacesContext para manejar ViewExpiredException.");
        }

        ExternalContext externalContext = context.getExternalContext();
        String targetView = normalizeTargetView(viewId);
        String location = externalContext.getRequestContextPath() + targetView + "?expiredView=1";

        try {
            externalContext.redirect(location);
            context.responseComplete();
        } catch (IOException e) {
            throw new FacesException("No se pudo redirigir tras ViewExpiredException.", e);
        }
    }

    private String normalizeTargetView(String viewId) {
        if (viewId == null || viewId.isBlank()) {
            return "/index.xhtml";
        }

        if (viewId.endsWith(".jsf")) {
            return viewId.substring(0, viewId.length() - 4) + ".xhtml";
        }

        return viewId;
    }
}
