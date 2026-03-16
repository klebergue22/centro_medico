package ec.gob.igm.rrhh.consultorio.web.service;

import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;

@Dependent
/**
 * Class DiagnosticoDialogControllerSupport: orquesta la lógica de presentación y flujo web.
 */
public class DiagnosticoDialogControllerSupport {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticoDialogControllerSupport.class);

    public DiagnosticoFilaUiCoordinator.DiagnosticoDialogState abrirDialogo(
            AjaxBehaviorEvent event,
            List<ConsultaDiagnostico> listaDiag,
            DiagnosticoFilaUiCoordinator diagnosticoFilaUiCoordinator) {
        return diagnosticoFilaUiCoordinator.abrirDialogo(event, listaDiag);
    }

    public boolean aceptarDialogo(
            Integer dialogDiagnosticoIdx,
            String codCie10Ppal,
            String descCie10Ppal,
            List<ConsultaDiagnostico> listaDiag,
            DiagnosticoFilaUiCoordinator diagnosticoFilaUiCoordinator) {
        return diagnosticoFilaUiCoordinator.aceptarDialogo(dialogDiagnosticoIdx, codCie10Ppal, descCie10Ppal, listaDiag);
    }

    public void mostrarDialogo() {
        PrimeFaces.current().executeScript("PF('kDiagDialogWv').show();");
    }

    public void cerrarDialogo() {
        PrimeFaces.current().executeScript("PF('kDiagDialogWv').hide();");
    }

    public void onKTipoChange(AjaxBehaviorEvent event, List<ConsultaDiagnostico> listaDiag) {
        UIComponent comp = event != null ? event.getComponent() : null;
        Integer idx = extraerIdx(comp);
        String clientId = safeClientId(comp);

        LOG.info(">>> [K-TIPO] change ENTER idx={} clientId={}", idx, clientId);

        ConsultaDiagnostico row = (idx == null || listaDiag == null || idx < 0 || idx >= listaDiag.size())
                ? null
                : listaDiag.get(idx);
        if (row == null) {
            return;
        }

        LOG.info("<<< [K-TIPO] AFTER idx={} codigo=[{}] desc=[{}] tipo=[{}]",
                idx,
                row.getCodigo(),
                row.getDescripcion(),
                row.getTipoDiag());
    }

    private Integer extraerIdx(UIComponent comp) {
        if (comp == null) {
            return null;
        }

        Object idxObj = comp.getAttributes().get("idx");
        if (idxObj == null) {
            return null;
        }

        try {
            return Integer.parseInt(idxObj.toString());
        } catch (NumberFormatException e) {
            LOG.info("... [K-TIPO] idx parse ERROR idxAttr={} ex={}", idxObj, e.toString());
            return null;
        }
    }

    private String safeClientId(UIComponent comp) {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            return (fc != null && comp != null) ? comp.getClientId(fc) : "null";
        } catch (RuntimeException e) {
            return "err:" + e.getMessage();
        }
    }
}
