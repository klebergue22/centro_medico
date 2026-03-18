package ec.gob.igm.rrhh.consultorio.web.facade;

import java.io.Serializable;
import java.util.List;

import org.primefaces.event.SelectEvent;

import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import ec.gob.igm.rrhh.consultorio.web.service.DiagnosticoViewDelegate;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Inject;

@ApplicationScoped
public class DiagnosticoSectionFacade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private DiagnosticoViewDelegate diagnosticoViewDelegate;

    public void syncCie10PrincipalFromK(CentroMedicoCtrl ctrl) { diagnosticoViewDelegate.syncCie10PrincipalFromK(ctrl); }
    public void onCie10BlurCodigo(CentroMedicoCtrl ctrl, int index) { diagnosticoViewDelegate.onCie10BlurCodigo(ctrl, index); }
    public void onCie10FilaSelect(CentroMedicoCtrl ctrl, int idx) { diagnosticoViewDelegate.onCie10FilaSelect(ctrl, idx); }
    public List<Cie10> completarCie10(String query) { return diagnosticoViewDelegate.completarCie10(query); }
    public List<Cie10> completarCie10PorCodigo(String query) { return diagnosticoViewDelegate.completarCie10PorCodigo(query); }
    public List<Cie10> completarCie10PorDescripcion(String query) { return diagnosticoViewDelegate.completarCie10PorDescripcion(query); }
    public void onCie10CodigoSelect(CentroMedicoCtrl ctrl, SelectEvent event) { diagnosticoViewDelegate.onCie10CodigoSelect(ctrl, event); }
    public void onCie10CodigoBlur(CentroMedicoCtrl ctrl) { diagnosticoViewDelegate.onCie10CodigoBlur(ctrl); }
    public void onCie10DescripcionSelect(CentroMedicoCtrl ctrl, SelectEvent event) { diagnosticoViewDelegate.onCie10DescripcionSelect(ctrl, event); }
    public void onCie10DescripcionBlur(CentroMedicoCtrl ctrl) { diagnosticoViewDelegate.onCie10DescripcionBlur(ctrl); }
    public Cie10 inferCie10PrincipalFromListaK(CentroMedicoCtrl ctrl) { return diagnosticoViewDelegate.inferCie10PrincipalFromListaK(ctrl); }
    public List<String> completarCie10FilaPorCodigo(String query) { return diagnosticoViewDelegate.completarCie10FilaPorCodigo(query); }
    public List<String> completarCie10FilaPorDescripcion(String query) { return diagnosticoViewDelegate.completarCie10FilaPorDescripcion(query); }
    public void onKCieCodigoSelect(CentroMedicoCtrl ctrl, SelectEvent<String> event) { diagnosticoViewDelegate.onKCieCodigoSelect(ctrl, event); }
    public void onKCieCodigoBlur(CentroMedicoCtrl ctrl, AjaxBehaviorEvent event) { diagnosticoViewDelegate.onKCieCodigoBlur(ctrl, event); }
    public void onKDescSelect(CentroMedicoCtrl ctrl, SelectEvent<String> event) { diagnosticoViewDelegate.onKDescSelect(ctrl, event); }
    public void onKDescBlur(CentroMedicoCtrl ctrl, AjaxBehaviorEvent event) { diagnosticoViewDelegate.onKDescBlur(ctrl, event); }
    public void onKTipoChange(CentroMedicoCtrl ctrl, AjaxBehaviorEvent event) { diagnosticoViewDelegate.onKTipoChange(ctrl, event); }
    public void abrirDialogoDiagnostico(CentroMedicoCtrl ctrl, AjaxBehaviorEvent event) { diagnosticoViewDelegate.abrirDialogoDiagnostico(ctrl, event); }
    public void aceptarDialogoDiagnostico(CentroMedicoCtrl ctrl) { diagnosticoViewDelegate.aceptarDialogoDiagnostico(ctrl); }
    public void cerrarDialogoDiagnostico() { diagnosticoViewDelegate.cerrarDialogoDiagnostico(); }
    public List<String> completarKCieStrings(String query) {return diagnosticoViewDelegate.completarKCieStrings(query);}
    public List<String> completarKDescStrings(String query) {return diagnosticoViewDelegate.completarKDescStrings(query);}
}
