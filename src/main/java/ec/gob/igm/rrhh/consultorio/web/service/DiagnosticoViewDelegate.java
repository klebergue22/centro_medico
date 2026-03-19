package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Inject;

@ApplicationScoped
/**
 * Class DiagnosticoViewDelegate: centraliza el flujo de UI de CIE10/diagnóstico para el controller.
 */
public class DiagnosticoViewDelegate implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticoViewDelegate.class);

    @Inject
    private transient Cie10LookupService cie10LookupService;
    @Inject
    private transient DiagnosticoFilaUiCoordinator diagnosticoFilaUiCoordinator;
    @Inject
    private transient DiagnosticoPrincipalService diagnosticoPrincipalService;
    @Inject
    private transient DiagnosticoDialogControllerSupport diagnosticoDialogControllerSupport;
    @Inject
    private transient CentroMedicoFormStateService centroMedicoFormStateService;

    public void syncCie10PrincipalFromK(CentroMedicoCtrl ctrl) {
        DiagnosticoPrincipalService.DiagnosticoPrincipalData principal = diagnosticoPrincipalService.inferirPrincipal(
                ctrl.getListaDiag(),
                ctrl.getCodCie10Ppal(),
                ctrl.getDescCie10Ppal());
        if (!principal.hasCodigo()) {
            return;
        }

        ctrl.setCodCie10Ppal(principal.getCodigo());
        ctrl.setDescCie10Ppal(principal.getDescripcion());
    }

    public void onCie10BlurCodigo(CentroMedicoCtrl ctrl, int index) {
        ConsultaDiagnostico diag = centroMedicoFormStateService.ensureDiag(ctrl, index);
        if (diag == null) {
            return;
        }
        cie10LookupService.completarDiagnosticoPorCodigo(diag);
    }

    public void onCie10FilaSelect(CentroMedicoCtrl ctrl, int idx) {
        if (ctrl.getListaDiag() == null || idx < 0 || idx >= ctrl.getListaDiag().size()) {
            return;
        }

        cie10LookupService.sincronizarFilaSeleccionada(ctrl.getListaDiag().get(idx));
    }

    public List<Cie10> completarCie10(String query) {
        return cie10LookupService.completarPorCodigoODescripcion(query, 20);
    }

    public List<Cie10> completarCie10PorCodigo(String query) {
        return cie10LookupService.completarPorCodigo(query);
    }

    public List<Cie10> completarCie10PorDescripcion(String query) {
        return cie10LookupService.completarPorDescripcion(query, 20);
    }

    public void onCie10CodigoSelect(CentroMedicoCtrl ctrl, SelectEvent event) {
        String codigo = (String) event.getObject();
        DiagnosticoPrincipalService.DiagnosticoPrincipalData principal = diagnosticoPrincipalService
                .sincronizarCodigoYDescripcion(codigo, null);
        ctrl.setCodCie10Ppal(principal.getCodigo());
        ctrl.setDescCie10Ppal(principal.getDescripcion());
    }

    public void onCie10CodigoBlur(CentroMedicoCtrl ctrl) {
        DiagnosticoPrincipalService.DiagnosticoPrincipalData principal = diagnosticoPrincipalService
                .sincronizarCodigoYDescripcion(ctrl.getCodCie10Ppal(), null);
        ctrl.setCodCie10Ppal(principal.getCodigo());
        ctrl.setDescCie10Ppal(principal.getDescripcion());
    }

    public void onCie10DescripcionSelect(CentroMedicoCtrl ctrl, SelectEvent event) {
        String descripcion = (String) event.getObject();
        DiagnosticoPrincipalService.DiagnosticoPrincipalData principal = diagnosticoPrincipalService
                .sincronizarCodigoYDescripcion(null, descripcion);
        ctrl.setCodCie10Ppal(principal.getCodigo());
        ctrl.setDescCie10Ppal(principal.getDescripcion());
    }

    public void onCie10DescripcionBlur(CentroMedicoCtrl ctrl) {
        DiagnosticoPrincipalService.DiagnosticoPrincipalData principal = diagnosticoPrincipalService
                .sincronizarCodigoYDescripcion(null, ctrl.getDescCie10Ppal());
        ctrl.setCodCie10Ppal(principal.getCodigo());
        ctrl.setDescCie10Ppal(principal.getDescripcion());
    }

    public Cie10 inferCie10PrincipalFromListaK(CentroMedicoCtrl ctrl) {
        return diagnosticoPrincipalService.inferirPrincipalCie10DesdeLista(ctrl.getListaDiag());
    }

    public List<String> completarCie10FilaPorCodigo(String query) {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            String viewId = (fc != null && fc.getViewRoot() != null) ? fc.getViewRoot().getViewId() : "null";
            LOG.info(">>> [AC-K-COD] complete ENTER query=[{}] viewId={}", query, viewId);

            List<String> out = cie10LookupService.completarFilaPorCodigo(query);

            LOG.info("<<< [AC-K-COD] RETURN out.size={}{}",
                    out.size(),
                    out.isEmpty() ? "" : " first=[" + out.get(0) + "]");
            return out;

        } catch (Exception e) {
            LOG.error("!!! [AC-K-COD] ERROR {} : {}", e.getClass().getName(), e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<String> completarCie10FilaPorDescripcion(String query) {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            String viewId = (fc != null && fc.getViewRoot() != null) ? fc.getViewRoot().getViewId() : "null";
            LOG.info(">>> [AC-K-DESC] complete ENTER query=[{}] viewId={}", query, viewId);

            List<String> out = cie10LookupService.completarFilaPorDescripcion(query, 20);

            LOG.info("<<< [AC-K-DESC] RETURN out.size={}{}",
                    out.size(),
                    out.isEmpty() ? "" : " first=[" + out.get(0) + "]");
            return out;

        } catch (Exception e) {
            LOG.error("!!! [AC-K-DESC] ERROR {} : {}", e.getClass().getName(), e.getMessage(), e);
            return new ArrayList<>();
        }
    }


    public void onKCieCodigoSelect(CentroMedicoCtrl ctrl, SelectEvent<String> event) {
        diagnosticoFilaUiCoordinator.onCodigoSelect(event, ctrl.getListaDiag());
    }

    public void onKCieCodigoBlur(CentroMedicoCtrl ctrl, AjaxBehaviorEvent event) {
        diagnosticoFilaUiCoordinator.onCodigoBlur(event, ctrl.getListaDiag());
    }

    public void onKDescSelect(CentroMedicoCtrl ctrl, SelectEvent<String> event) {
        diagnosticoFilaUiCoordinator.onDescripcionSelect(event, ctrl.getListaDiag());
        syncCie10PrincipalFromK(ctrl);
    }

    public void onKDescBlur(CentroMedicoCtrl ctrl, AjaxBehaviorEvent event) {
        diagnosticoFilaUiCoordinator.onDescripcionBlur(event, ctrl.getListaDiag());
    }

    public void onKTipoChange(CentroMedicoCtrl ctrl, AjaxBehaviorEvent event) {
        diagnosticoDialogControllerSupport.onKTipoChange(event, ctrl.getListaDiag());
    }

    public void abrirDialogoDiagnostico(CentroMedicoCtrl ctrl, AjaxBehaviorEvent event) {
        DiagnosticoFilaUiCoordinator.DiagnosticoDialogState state = diagnosticoDialogControllerSupport
                .abrirDialogo(event, ctrl.getListaDiag(), diagnosticoFilaUiCoordinator);
        if (!state.isValid()) {
            return;
        }

        ctrl.setDialogDiagnosticoIdx(state.getIdx());
        ctrl.setCodCie10Ppal(state.getCodigo());
        ctrl.setDescCie10Ppal(state.getDescripcion());
        diagnosticoDialogControllerSupport.mostrarDialogo();
    }

    public void aceptarDialogoDiagnostico(CentroMedicoCtrl ctrl) {
        boolean accepted = diagnosticoDialogControllerSupport.aceptarDialogo(
                ctrl.getDialogDiagnosticoIdx(),
                ctrl.getCodCie10Ppal(),
                ctrl.getDescCie10Ppal(),
                ctrl.getListaDiag(),
                diagnosticoFilaUiCoordinator);
        if (!accepted) {
            return;
        }

        syncCie10PrincipalFromK(ctrl);
        diagnosticoDialogControllerSupport.cerrarDialogo();
    }

    public void cerrarDialogoDiagnostico() {
        diagnosticoDialogControllerSupport.cerrarDialogo();
    }
    public List<String> completarKCieStrings(String query) {
    try {
        FacesContext fc = FacesContext.getCurrentInstance();
        String viewId = (fc != null && fc.getViewRoot() != null) ? fc.getViewRoot().getViewId() : "null";
        LOG.info(">>> [AC-K-COD-STR] complete ENTER query=[{}] viewId={}", query, viewId);

        List<String> out = cie10LookupService.completarFilaPorCodigo(query);

        LOG.info("<<< [AC-K-COD-STR] RETURN out.size={}{}",
                out.size(),
                out.isEmpty() ? "" : " first=[" + out.get(0) + "]");
        return out;
    } catch (Exception e) {
        LOG.error("!!! [AC-K-COD-STR] ERROR {} : {}", e.getClass().getName(), e.getMessage(), e);
        return new ArrayList<>();
    }
}

public List<String> completarKDescStrings(String query) {
    try {
        FacesContext fc = FacesContext.getCurrentInstance();
        String viewId = (fc != null && fc.getViewRoot() != null) ? fc.getViewRoot().getViewId() : "null";
        LOG.info(">>> [AC-K-DESC-STR] complete ENTER query=[{}] viewId={}", query, viewId);

        List<String> out = cie10LookupService.completarFilaPorDescripcion(query, 20);

        LOG.info("<<< [AC-K-DESC-STR] RETURN out.size={}{}",
                out.size(),
                out.isEmpty() ? "" : " first=[" + out.get(0) + "]");
        return out;
    } catch (Exception e) {
        LOG.error("!!! [AC-K-DESC-STR] ERROR {} : {}", e.getClass().getName(), e.getMessage(), e);
        return new ArrayList<>();
    }
}
}
