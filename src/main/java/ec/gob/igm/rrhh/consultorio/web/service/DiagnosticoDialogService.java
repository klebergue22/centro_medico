package ec.gob.igm.rrhh.consultorio.web.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.service.Cie10Service;

@Stateless
/**
 * Class DiagnosticoDialogService: orquesta la logica de presentacion y flujo web.
 */
public class DiagnosticoDialogService {

    @EJB
    private Cie10Service cie10Service;

    public DiagnosticoDialogState construirEstado(Integer idx, ConsultaDiagnostico row) {
        if (row == null) {
            return new DiagnosticoDialogState(idx, null, null);
        }
        return new DiagnosticoDialogState(idx, row.getCodigo(), row.getDescripcion());
    }

    public void aplicarSeleccion(ConsultaDiagnostico row, String codigoIngresado, String descripcionIngresada) {
        if (row == null) {
            return;
        }

        String codigo = trimToEmpty(codigoIngresado).toUpperCase();
        String descripcion = trimToEmpty(descripcionIngresada);
        if (codigo.isEmpty() && descripcion.isEmpty()) {
            clearSelection(row);
            return;
        }

        Cie10 cie = resolveCie10(codigo, descripcion);
        if (cie != null) {
            applyResolvedSelection(row, cie);
            return;
        }

        applyManualSelection(row, codigo, descripcion);
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    public record DiagnosticoDialogState(Integer idx, String codigo, String descripcion) {
    }

    private void clearSelection(ConsultaDiagnostico row) {
        row.setCodigo(null);
        row.setDescripcion(null);
        row.setCie10(null);
    }

    private Cie10 resolveCie10(String codigo, String descripcion) {
        Cie10 cie = null;
        if (!codigo.isEmpty()) {
            cie = cie10Service.buscarPorCodigo(codigo);
        }
        if (cie == null && !descripcion.isEmpty()) {
            cie = cie10Service.buscarPrimeroPorDescripcion(descripcion);
        }
        return cie;
    }

    private void applyResolvedSelection(ConsultaDiagnostico row, Cie10 cie) {
        row.setCodigo(cie.getCodigo());
        row.setDescripcion(cie.getDescripcion());
        row.setCie10(cie);
    }

    private void applyManualSelection(ConsultaDiagnostico row, String codigo, String descripcion) {
        row.setCodigo(codigo.isEmpty() ? null : codigo);
        row.setDescripcion(descripcion.isEmpty() ? null : descripcion);
        row.setCie10(null);
    }
}
