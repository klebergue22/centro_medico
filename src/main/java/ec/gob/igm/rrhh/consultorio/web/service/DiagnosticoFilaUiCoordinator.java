package ec.gob.igm.rrhh.consultorio.web.service;

import java.util.List;
import java.util.Map;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.Dependent;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;

import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.service.Cie10Service;

@Dependent
public class DiagnosticoFilaUiCoordinator {

    private static final Logger LOG = LoggerFactory.getLogger(DiagnosticoFilaUiCoordinator.class);

    @EJB
    private Cie10Service cie10Service;

    @EJB
    private Cie10LookupService cie10LookupService;

    public void onCodigoSelect(SelectEvent<String> event, List<ConsultaDiagnostico> listaDiag) {
        UIComponent comp = event != null ? event.getComponent() : null;
        Integer idx = extraerIdx(comp);
        String selected = event != null ? event.getObject() : null;

        LOG.info(">>> [AC-K-COD] itemSelect idx=" + idx
                + " selected=[" + selected + "] clientId=" + (comp != null ? comp.getClientId() : "null"));

        ConsultaDiagnostico row = getDiagRow(listaDiag, idx, "AC-K-COD itemSelect");
        if (row == null) {
            return;
        }

        if (selected == null || selected.trim().isEmpty()) {
            LOG.info("<<< [AC-K-COD] itemSelect empty selection => no-op");
            return;
        }

        String codigo = selected.trim().toUpperCase();
        row.setCodigo(codigo);

        Cie10 cie = cie10Service.buscarPorCodigo(codigo);
        LOG.info("... [AC-K-COD] itemSelect buscarPorCodigo(" + codigo + ") => " + (cie != null ? cie.getCodigo() : "null"));

        if (cie != null) {
            row.setCodigo(cie.getCodigo());
            row.setDescripcion(cie.getDescripcion());
            row.setCie10(cie);
        } else {
            row.setDescripcion(null);
            row.setCie10(null);
        }

        LOG.info("<<< [AC-K-COD] itemSelect AFTER codigo=[" + row.getCodigo() + "] desc=[" + row.getDescripcion() + "]");
    }

    public void onCodigoBlur(AjaxBehaviorEvent event, List<ConsultaDiagnostico> listaDiag) {
        UIComponent comp = event != null ? event.getComponent() : null;
        Integer idx = extraerIdx(comp);

        String clientId = safeClientId(comp);
        String typed = getAutoCompleteTypedRobusto(comp);

        LOG.info(">>> [AC-K-COD] blur idx=" + idx + " clientId=" + clientId + " typed=[" + typed + "]");

        ConsultaDiagnostico row = getDiagRow(listaDiag, idx, "AC-K-COD blur");
        if (row == null) {
            return;
        }

        String codigo = typed != null ? typed.trim() : "";
        if (codigo.isEmpty()) {
            row.setCodigo(null);
            row.setDescripcion(null);
            row.setCie10(null);
            LOG.info("<<< [AC-K-COD] blur empty => cleared row");
            return;
        }

        String codigoUp = codigo.toUpperCase();

        if (codigoUp.length() < 3) {
            row.setCodigo(codigoUp);
            row.setDescripcion(null);
            row.setCie10(null);
            LOG.info("<<< [AC-K-COD] blur partial [" + codigoUp + "] => keep, no exact lookup");
            return;
        }

        Cie10 cie = cie10Service.buscarPorCodigo(codigoUp);

        if (cie == null) {
            List<Cie10> sugerencias = cie10Service.buscarJerarquiaPorTerm(codigoUp);
            if (sugerencias != null) {
                String codigoNorm = codigoUp.replaceAll("[^A-Z0-9]", "");
                for (Cie10 candidato : sugerencias) {
                    if (candidato == null || candidato.getCodigo() == null) {
                        continue;
                    }

                    String candidatoNorm = candidato.getCodigo().toUpperCase().replaceAll("[^A-Z0-9]", "");
                    if (candidatoNorm.equals(codigoNorm)) {
                        cie = candidato;
                        break;
                    }
                }
            }
        }

        LOG.debug("... [AC-K-COD] buscarPorCodigo(" + codigoUp + ") => "
                + (cie != null ? (cie.getCodigo() + " | " + cie.getDescripcion()) : "null"));
        if (cie != null) {
            row.setCodigo(cie.getCodigo());
            row.setDescripcion(cie.getDescripcion());
            row.setCie10(cie);
            LOG.info("<<< [AC-K-COD] blur AFTER MATCH codigo=[" + row.getCodigo() + "] desc=[" + row.getDescripcion() + "]");
            return;
        }

        row.setCodigo(codigoUp);
        row.setDescripcion(null);
        row.setCie10(null);
        LOG.info("<<< [AC-K-COD] blur AFTER NO-MATCH keep codigo=[" + row.getCodigo() + "]");
    }

    public void onDescripcionSelect(SelectEvent<String> event, List<ConsultaDiagnostico> listaDiag) {
        String descripcion = event != null ? event.getObject() : null;
        UIComponent comp = event != null ? event.getComponent() : null;
        Integer idx = extraerIdx(comp);

        LOG.info(">>> [AC-K-DESC] itemSelect ENTER desc=[" + descripcion + "] idx=" + idx);

        ConsultaDiagnostico row = getDiagRow(listaDiag, idx, "AC-K-DESC itemSelect");
        if (row == null) {
            return;
        }

        row.setDescripcion(descripcion);

        if (descripcion != null && !descripcion.trim().isEmpty()) {
            Cie10 cie = cie10Service.buscarPrimeroPorDescripcion(descripcion.trim());
            LOG.info("... [AC-K-DESC] buscarPrimeroPorDescripcion => " + (cie != null ? cie.getCodigo() : "null"));

            if (cie != null) {
                row.setCodigo(cie.getCodigo());
                row.setCie10(cie);
            } else {
                row.setCodigo(null);
                row.setCie10(null);
            }
        }

        LOG.info("<<< [AC-K-DESC] itemSelect row AFTER idx=" + idx
                + " codigo=[" + row.getCodigo() + "] desc=[" + row.getDescripcion() + "]");
    }

    public void onDescripcionBlur(AjaxBehaviorEvent event, List<ConsultaDiagnostico> listaDiag) {
        UIComponent comp = event != null ? event.getComponent() : null;
        Integer idx = extraerIdx(comp);
        String clientId = safeClientId(comp);
        String typed = getAutoCompleteTypedRobusto(comp);

        LOG.info(">>> [AC-K-DESC] blur idx=" + idx + " clientId=" + clientId + " typed=[" + typed + "]");

        ConsultaDiagnostico row = getDiagRow(listaDiag, idx, "AC-K-DESC blur");
        if (row == null) {
            return;
        }

        String desc = typed != null ? typed.trim() : "";
        if (desc.isEmpty()) {
            row.setDescripcion(null);
            row.setCie10(null);
            LOG.info("<<< [AC-K-DESC] blur empty => cleared descripcion");
            return;
        }

        row.setDescripcion(desc);

        if (desc.length() < 4) {
            row.setCie10(null);
            LOG.info("<<< [AC-K-DESC] blur partial => keep desc only");
            return;
        }

        Cie10 cie = null;
        try {
            List<Cie10> candidatos = cie10Service.buscarPorDescripcionLike(desc, 20);
            LOG.info("... [AC-K-DESC] candidatos.size=" + (candidatos == null ? "null" : candidatos.size()));
            if (candidatos != null && !candidatos.isEmpty()) {
                cie = cie10LookupService.buscarMejorCoincidenciaPorDescripcion(candidatos, desc);
            }
        } catch (RuntimeException e) {
            LOG.error("!!! [AC-K-DESC] error: {}", e.getMessage(), e);
        }

        LOG.debug("... [AC-K-DESC] pickBest => "
                + (cie != null ? (cie.getCodigo() + " | " + cie.getDescripcion()) : "null"));

        if (cie != null) {
            row.setCodigo(cie.getCodigo());
            row.setDescripcion(cie.getDescripcion());
            row.setCie10(cie);
            LOG.info("<<< [AC-K-DESC] blur AFTER MATCH codigo=[" + row.getCodigo() + "] desc=[" + row.getDescripcion() + "]");
            return;
        }

        row.setCie10(null);
        LOG.info("<<< [AC-K-DESC] blur AFTER NO-MATCH keep desc=[" + row.getDescripcion() + "]");
    }

    public DiagnosticoDialogState abrirDialogo(AjaxBehaviorEvent event, List<ConsultaDiagnostico> listaDiag) {
        UIComponent comp = event != null ? event.getComponent() : null;
        Integer idx = extraerIdx(comp);
        ConsultaDiagnostico row = getDiagRow(listaDiag, idx, "K-DLG abrir");
        if (row == null) {
            return DiagnosticoDialogState.invalid();
        }

        return DiagnosticoDialogState.valid(idx, row.getCodigo(), row.getDescripcion());
    }

    public boolean aceptarDialogo(Integer dialogDiagnosticoIdx,
            String codCie10Ppal,
            String descCie10Ppal,
            List<ConsultaDiagnostico> listaDiag) {
        ConsultaDiagnostico row = getDiagRow(listaDiag, dialogDiagnosticoIdx, "K-DLG aceptar");
        if (row == null) {
            return false;
        }

        String codigo = codCie10Ppal != null ? codCie10Ppal.trim().toUpperCase() : "";
        String descripcion = descCie10Ppal != null ? descCie10Ppal.trim() : "";

        Cie10 cie = null;
        if (!codigo.isEmpty()) {
            cie = cie10Service.buscarPorCodigo(codigo);
        }
        if (cie == null && !descripcion.isEmpty()) {
            cie = cie10Service.buscarPrimeroPorDescripcion(descripcion);
        }

        if (cie != null) {
            row.setCodigo(cie.getCodigo());
            row.setDescripcion(cie.getDescripcion());
            row.setCie10(cie);
        } else {
            row.setCodigo(codigo.isEmpty() ? null : codigo);
            row.setDescripcion(descripcion.isEmpty() ? null : descripcion);
            row.setCie10(null);
        }
        return true;
    }

    private Integer extraerIdx(UIComponent comp) {
        if (comp == null) {
            LOG.info("... [IDX] extraerIdx comp=null");
            return null;
        }
        Object idxObj = comp.getAttributes().get("idx");

        String clientId;
        try {
            clientId = comp.getClientId(FacesContext.getCurrentInstance());
        } catch (RuntimeException e) {
            clientId = String.valueOf(comp.getId());
        }

        LOG.debug("... [IDX] extraerIdx compId=" + comp.getId()
                + " clientId=" + clientId
                + " idxAttr=" + idxObj);

        if (idxObj == null) {
            return null;
        }
        try {
            return Integer.parseInt(idxObj.toString());
        } catch (NumberFormatException e) {
            LOG.info("... [IDX] extraerIdx parse ERROR idxAttr={} ex={}", idxObj, e.toString());
            return null;
        }
    }

    private ConsultaDiagnostico getDiagRow(List<ConsultaDiagnostico> listaDiag, Integer idx, String contexto) {
        if (idx == null || listaDiag == null || idx < 0 || idx >= listaDiag.size()) {
            LOG.info("<<< [{}] idx INVALID => {}", contexto, idx);
            return null;
        }
        return listaDiag.get(idx);
    }

    private String getAutoCompleteTypedRobusto(UIComponent comp) {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc == null || comp == null) {
                return null;
            }

            String base = comp.getClientId(fc);
            Map<String, String> params = fc.getExternalContext().getRequestParameterMap();

            String[] keys = new String[] {
                    base + "_input",
                    base,
                    base + "_hinput",
                    base + "_query"
            };

            for (String k : keys) {
                String v = params.get(k);
                if (v != null) {
                    LOG.info("... [REQ] AC typed key=" + k + " => [" + v + "]");
                    return v;
                }
            }

            LOG.warn("!!! [REQ] AC typed NOT FOUND for base=" + base
                    + " (tried _input, base, _hinput, _query)");
            return null;

        } catch (RuntimeException e) {
            LOG.error("!!! [REQ] getAutoCompleteTypedRobusto ERROR: {}", e.getMessage(), e);
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

    public static final class DiagnosticoDialogState {
        private final boolean valid;
        private final Integer idx;
        private final String codigo;
        private final String descripcion;

        private DiagnosticoDialogState(boolean valid, Integer idx, String codigo, String descripcion) {
            this.valid = valid;
            this.idx = idx;
            this.codigo = codigo;
            this.descripcion = descripcion;
        }

        public static DiagnosticoDialogState valid(Integer idx, String codigo, String descripcion) {
            return new DiagnosticoDialogState(true, idx, codigo, descripcion);
        }

        public static DiagnosticoDialogState invalid() {
            return new DiagnosticoDialogState(false, null, null, null);
        }

        public boolean isValid() {
            return valid;
        }

        public Integer getIdx() {
            return idx;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}
