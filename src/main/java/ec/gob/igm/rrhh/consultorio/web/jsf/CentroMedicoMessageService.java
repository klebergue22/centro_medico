package ec.gob.igm.rrhh.consultorio.web.jsf;

import java.io.Serializable;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import org.slf4j.Logger;

import ec.gob.igm.rrhh.consultorio.web.validation.ValidationResult;

@ApplicationScoped
public class CentroMedicoMessageService implements Serializable {

    private static final long serialVersionUID = 1L;

    public void warn(String msg) {
        addMsg(FacesMessage.SEVERITY_WARN, "Step 1", msg);
    }

    public void info(String msg) {
        addMsg(FacesMessage.SEVERITY_INFO, "Step 1", msg);
    }

    public void error(String msg) {
        addMsg(FacesMessage.SEVERITY_ERROR, "Error", msg);
    }

    public void addMsg(FacesMessage.Severity sev, String summary, String detail) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx != null) {
            ctx.addMessage(null, new FacesMessage(sev, summary, detail));
        }
    }

    public void addValidationMessages(String step, ValidationResult result) {
        if (result == null || result.isValid()) {
            return;
        }
        for (String error : result.getErrors()) {
            addMsg(FacesMessage.SEVERITY_ERROR, step, error);
        }
    }

    public void handleUnexpected(Logger logger, String action, Throwable t,
                                 String activeStep, Integer noPersonaSel, String cedulaBusqueda) {
        logger.error("Unexpected error during {}. activeStep={}, noPersonaSel={}, cedulaBusqueda={}",
                action, activeStep, noPersonaSel, cedulaBusqueda, t);
        error("Ocurrió un error inesperado al " + action + ". Revise el LOG o contacte a soporte.");
    }
}

