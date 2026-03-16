package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("centroMedicoViewLifecycleCoordinator")
@RequestScoped
/**
 * Class CentroMedicoViewLifecycleCoordinator: orquesta la lógica de presentación y flujo web.
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
            PacienteUiFlowCoordinator.UiFlowResult result = pacienteUiFlowCoordinator.ensureEmpleadoSelEnViewScope(
                    ctrl.isPermitirIngresoManual(),
                    ctrl.getEmpleadoSel(),
                    ctrl.getNoPersonaSel(),
                    ctrl.getFicha(),
                    ctrl.getPersonaAux());
            pacienteUiStateApplier.apply(pacienteViewBinder.forGeneralFlow(result), ctrl);

            ctrl.setMostrarDlgCedula("step1".equals(ctrl.getActiveStep()) && ctrl.getEmpleadoSel() == null);

            centroMedicoFormStateService.prepareStep3Collections(ctrl, H_ROWS, DIAG_ROWS, ACT_LAB_ROWS);
            if (!ctrl.isPreRenderDone()) {
                ctrl.setPreRenderDone(true);
            }

            LOG.info("GET? {} activeStep={} empleadoSel={} mostrarDlgCedula={}",
                    !fc.isPostback(),
                    ctrl.getActiveStep(),
                    (ctrl.getEmpleadoSel() == null),
                    ctrl.isMostrarDlgCedula());
        } catch (RuntimeException e) {
            LOG.error("preRender failed. activeStep={}, noPersonaSel={}, cedulaBusqueda={}",
                    ctrl.getActiveStep(), ctrl.getNoPersonaSel(), ctrl.getCedulaBusqueda(), e);
        }
    }
}
