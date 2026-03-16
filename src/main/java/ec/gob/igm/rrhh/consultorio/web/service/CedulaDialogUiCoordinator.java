package ec.gob.igm.rrhh.consultorio.web.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.primefaces.PrimeFaces;

@ApplicationScoped
/**
 * Class CedulaDialogUiCoordinator: orquesta la lógica de presentación y flujo web.
 */
public class CedulaDialogUiCoordinator {

    private static final String CEDULA_MSG_CLIENT_ID = "dlgCedulaForm:cedulaBusqueda";

    public void onFound(CedulaSearchService.CedulaSearchResult result) {
        addMessage(FacesMessage.SEVERITY_INFO, "Búsqueda", "Información cargada desde RRHH.");
        pushCallbackParams(result);
        PrimeFaces.current().ajax().update(":wdzFicha", ":msgs");
        updateDialog();
    }

    public void onManualEnabled(CedulaSearchService.CedulaSearchResult result) {
        addMessage(FacesMessage.SEVERITY_WARN, "Búsqueda", "No se encontró la cédula. Puede ingresar los datos manualmente.");
        pushCallbackParams(result);
        PrimeFaces.current().ajax().update(":wdzFicha", ":msgs",
                ":dlgPersonaAuxForm:cedManual", ":dlgPersonaAuxForm:gridManual", ":dlgPersonaAuxForm:msgPersonaAux");
        updateDialog();
    }

    public void onValidationWarning(String detail) {
        addMessage(FacesMessage.SEVERITY_WARN, "Búsqueda", detail);
        pushCallbackParams(CedulaSearchService.CedulaSearchResult.notFoundNoManual());
        updateDialog();
        PrimeFaces.current().ajax().update(":msgs");
    }

    public void onSearchError() {
        addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al buscar la cédula.");
        pushCallbackParams(CedulaSearchService.CedulaSearchResult.notFoundNoManual());
        updateDialog();
        PrimeFaces.current().ajax().update(":msgs");
    }

    public void onRhError() {
        addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Ocurrió un error al consultar datos de RRHH.");
        PrimeFaces.current().ajax().update(":msgs");
    }

    public void showCargoMissing() {
        addMessage(FacesMessage.SEVERITY_WARN, "Cargo", "El empleado no registra cargo vigente en RRHH.");
    }

    public void refreshMainViews() {
        PrimeFaces.current().ajax().update(":msgs", "@([id$=wdzFicha])");
    }

    private void updateDialog() {
        PrimeFaces.current().ajax().update(":dlgCedulaForm:msgCedula", ":dlgCedulaForm:panelBtnManualWrap");
    }

    private void pushCallbackParams(CedulaSearchService.CedulaSearchResult result) {
        PrimeFaces.current().ajax().addCallbackParam("encontrado", result.isFound());
        PrimeFaces.current().ajax().addCallbackParam("mostrarManual", result.isShowManual());
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx != null) {
            ctx.addMessage(CEDULA_MSG_CLIENT_ID, new FacesMessage(severity, summary, detail));
        }
    }
}
