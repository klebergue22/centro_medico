package ec.gob.igm.rrhh.consultorio.web.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.service.Cie10Service;

@Stateless
/**
 * Class DiagnosticoDialogService: orquesta la lógica de presentación y flujo web.
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
            row.setCodigo(null);
            row.setDescripcion(null);
            row.setCie10(null);
            return;
        }

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
            return;
        }

        row.setCodigo(codigo.isEmpty() ? null : codigo);
        row.setDescripcion(descripcion.isEmpty() ? null : descripcion);
        row.setCie10(null);
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    public record DiagnosticoDialogState(Integer idx, String codigo, String descripcion) {
    }
}
