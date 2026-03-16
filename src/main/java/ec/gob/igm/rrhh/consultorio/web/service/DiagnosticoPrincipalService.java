package ec.gob.igm.rrhh.consultorio.web.service;

import java.util.List;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;

@Stateless
/**
 * Class DiagnosticoPrincipalService: orquesta la lógica de presentación y flujo web.
 */
public class DiagnosticoPrincipalService {

    @EJB
    private Cie10LookupService cie10LookupService;

    public DiagnosticoPrincipalData inferirPrincipal(List<ConsultaDiagnostico> diagnosticos,
            String codigoPrincipalActual,
            String descripcionPrincipalActual) {
        if (codigoPrincipalActual != null && !codigoPrincipalActual.trim().isEmpty()) {
            return DiagnosticoPrincipalData.of(codigoPrincipalActual, descripcionPrincipalActual);
        }

        ConsultaDiagnostico mejorDiagnostico = seleccionarMejorDiagnostico(diagnosticos);
        if (mejorDiagnostico == null) {
            return DiagnosticoPrincipalData.empty();
        }

        return DiagnosticoPrincipalData.of(mejorDiagnostico.getCodigo(), mejorDiagnostico.getDescripcion());
    }

    public DiagnosticoPrincipalData sincronizarCodigoYDescripcion(String codigo, String descripcion) {
        if (codigo != null && !codigo.trim().isEmpty()) {
            return DiagnosticoPrincipalData.of(codigo, cie10LookupService.buscarDescripcionPorCodigo(codigo));
        }

        if (descripcion != null && !descripcion.trim().isEmpty()) {
            return DiagnosticoPrincipalData.of(cie10LookupService.buscarCodigoPorDescripcion(descripcion), descripcion);
        }

        return DiagnosticoPrincipalData.empty();
    }

    public Cie10 inferirPrincipalCie10DesdeLista(List<ConsultaDiagnostico> diagnosticos) {
        return cie10LookupService.inferirPrincipalDesdeLista(diagnosticos);
    }

    public ConsultaDiagnostico seleccionarMejorDiagnostico(List<ConsultaDiagnostico> diagnosticos) {
        if (diagnosticos == null || diagnosticos.isEmpty()) {
            return null;
        }

        ConsultaDiagnostico best = null;
        for (ConsultaDiagnostico diagnostico : diagnosticos) {
            if (diagnostico == null) {
                continue;
            }

            String codigo = diagnostico.getCodigo() != null ? diagnostico.getCodigo().trim() : "";
            if (codigo.isEmpty()) {
                continue;
            }

            if ("D".equals(diagnostico.getTipoDiag())) {
                return diagnostico;
            }

            if (best == null) {
                best = diagnostico;
            }
        }

        return best;
    }

    public static final class DiagnosticoPrincipalData {

        private final String codigo;
        private final String descripcion;

        private DiagnosticoPrincipalData(String codigo, String descripcion) {
            this.codigo = codigo;
            this.descripcion = descripcion;
        }

        public static DiagnosticoPrincipalData of(String codigo, String descripcion) {
            return new DiagnosticoPrincipalData(codigo, descripcion);
        }

        public static DiagnosticoPrincipalData empty() {
            return new DiagnosticoPrincipalData(null, null);
        }

        public String getCodigo() {
            return codigo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public boolean hasCodigo() {
            return codigo != null && !codigo.trim().isEmpty();
        }
    }
}

