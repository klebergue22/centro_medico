package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("centroMedicoViewLifecycleCoordinator")
@RequestScoped
/**
 * Class CentroMedicoViewLifecycleCoordinator: orquesta la logica de presentacion y flujo web.
 */
public class CentroMedicoViewLifecycleCoordinator implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(CentroMedicoViewLifecycleCoordinator.class);
    private static final int H_ROWS = 4;
    private static final int DIAG_ROWS = 6;
    private static final int ACT_LAB_ROWS = 5;

    @Inject
    private transient PacienteUiFlowCoordinator pacienteUiFlowCoordinator;
    @Inject
    private transient PacienteViewBinder pacienteViewBinder;
    @Inject
    private transient PacienteUiStateApplier pacienteUiStateApplier;
    @Inject
    private transient CentroMedicoFormStateService centroMedicoFormStateService;

    public void preRender(CentroMedicoCtrl ctrl) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null || ctrl == null) {
            return;
        }

        try {
            runPreRenderFlow(fc, ctrl);
        } catch (RuntimeException e) {
            LOG.error("preRender failed. activeStep={}, noPersonaSel={}, cedulaBusqueda={}",
                    ctrl.getActiveStep(), ctrl.getNoPersonaSel(), ctrl.getCedulaBusqueda(), e);
        }
    }

    private boolean shouldSkipForAutocompleteAjax(FacesContext fc) {
        if (fc == null) {
            return false;
        }

        PartialViewContext pvc = fc.getPartialViewContext();
        if (pvc == null || !pvc.isAjaxRequest()) {
            return false;
        }

        Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
        return params != null
                && !params.isEmpty()
                && (hasAutocompleteAjaxEvent(params)
                || hasAutocompleteSourceParam(params)
                || hasAutocompleteQueryParam(params));
    }

    private boolean isAutocompleteSource(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        return value.contains(":kCie")
                || value.contains(":kDesc")
                || value.endsWith("kCie")
                || value.endsWith("kDesc");
    }

    private void runPreRenderFlow(FacesContext fc, CentroMedicoCtrl ctrl) {
        if (shouldSkipForAutocompleteAjax(fc)) {
            LOG.debug("Skip preRender for autocomplete ajax request. activeStep={}", ctrl.getActiveStep());
            return;
        }

        PacienteUiFlowCoordinator.UiFlowResult result = pacienteUiFlowCoordinator.ensureEmpleadoSelEnViewScope(
                ctrl.isPermitirIngresoManual(),
                ctrl.getEmpleadoSel(),
                ctrl.getNoPersonaSel(),
                ctrl.getFicha(),
                ctrl.getPersonaAux());
        pacienteUiStateApplier.apply(pacienteViewBinder.forGeneralFlow(result), ctrl);
        ctrl.setMostrarDlgCedula("step1".equals(ctrl.getActiveStep()) && ctrl.getEmpleadoSel() == null);
        centroMedicoFormStateService.prepareStep3Collections(ctrl, H_ROWS, DIAG_ROWS, ACT_LAB_ROWS);
        if (!ctrl.isPreRenderDone()) ctrl.setPreRenderDone(true);
        logPreRenderState(fc, ctrl);
    }

    private boolean hasAutocompleteAjaxEvent(Map<String, String> params) {
        String partialEvent = params.get("javax.faces.partial.event");
        return "query".equalsIgnoreCase(partialEvent);
    }

    private boolean hasAutocompleteSourceParam(Map<String, String> params) {
        return isAutocompleteSource(params.get("javax.faces.source"))
                || isAutocompleteSource(params.get("javax.faces.partial.execute"));
    }

    private boolean hasAutocompleteQueryParam(Map<String, String> params) {
        for (String key : params.keySet()) {
            if (key != null && (key.endsWith("_query") || key.endsWith("_input"))) {
                return true;
            }
        }
        return false;
    }

    private void logPreRenderState(FacesContext fc, CentroMedicoCtrl ctrl) {
        LOG.info("GET? {} activeStep={} empleadoSel={} mostrarDlgCedula={}",
                !fc.isPostback(),
                ctrl.getActiveStep(),
                (ctrl.getEmpleadoSel() == null),
                ctrl.isMostrarDlgCedula());
    }
}
