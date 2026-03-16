package ec.gob.igm.rrhh.consultorio.web.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.primefaces.PrimeFaces;

@ApplicationScoped
/**
 * Class PersonaAuxDialogUiCoordinator: orquesta la lógica de presentación y flujo web.
 */
public class PersonaAuxDialogUiCoordinator {

    public void onGuardarSuccess(PacienteUiFlowCoordinator.UiFlowResult result) {
        for (String update : result.getUpdates()) {
            PrimeFaces.current().ajax().update(update);
        }
        PrimeFaces.current().ajax().addCallbackParam("validationFailed", false);
        for (String script : result.getScripts()) {
            PrimeFaces.current().executeScript(script);
        }
        addMessage(FacesMessage.SEVERITY_INFO,
                "Datos guardados",
                "Se guardó la persona auxiliar y se cargaron los datos en la ficha.");
    }

    public void onValidationFailure(String detail) {
        addMessage(FacesMessage.SEVERITY_WARN, "Datos incompletos", detail);
        PrimeFaces.current().ajax().addCallbackParam("validationFailed", true);
    }

    public void onTechnicalFailure() {
        addMessage(FacesMessage.SEVERITY_ERROR,
                "Error",
                "Ocurrió un error al procesar y guardar los datos.");
        PrimeFaces.current().ajax().addCallbackParam("validationFailed", true);
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx != null) {
            ctx.addMessage(null, new FacesMessage(severity, summary, detail));
        }
    }
}
