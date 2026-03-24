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
        DiagnosticoPrincipalData principalActual = resolverPrincipalActual(codigoPrincipalActual,
                descripcionPrincipalActual);
        if (principalActual.hasCodigo()) {
            return principalActual;
        }

        Cie10 principalDesdeLista = cie10LookupService.inferirPrincipalDesdeLista(diagnosticos);
        if (principalDesdeLista == null) {
            return DiagnosticoPrincipalData.empty();
        }

        return DiagnosticoPrincipalData.of(principalDesdeLista.getCodigo(), principalDesdeLista.getDescripcion());
    }

    public DiagnosticoPrincipalData sincronizarCodigoYDescripcion(String codigo, String descripcion) {
        if (codigo != null && !codigo.trim().isEmpty()) {
            String codigoNormalizado = codigo.trim().toUpperCase();
            String descripcionResuelta = cie10LookupService.buscarDescripcionPorCodigo(codigoNormalizado);
            if (descripcionResuelta == null) {
                return DiagnosticoPrincipalData.empty();
            }
            return DiagnosticoPrincipalData.of(codigoNormalizado, descripcionResuelta);
        }

        if (descripcion != null && !descripcion.trim().isEmpty()) {
            String descripcionNormalizada = descripcion.trim();
            String codigoResuelto = cie10LookupService.buscarCodigoPorDescripcion(descripcionNormalizada);
            if (codigoResuelto == null) {
                return DiagnosticoPrincipalData.empty();
            }
            return DiagnosticoPrincipalData.of(codigoResuelto, descripcionNormalizada);
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

    private DiagnosticoPrincipalData resolverPrincipalActual(String codigoPrincipalActual,
            String descripcionPrincipalActual) {
        DiagnosticoPrincipalData porCodigo = sincronizarCodigoYDescripcion(codigoPrincipalActual, null);
        if (porCodigo.hasCodigo()) {
            return porCodigo;
        }

        return sincronizarCodigoYDescripcion(null, descripcionPrincipalActual);
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

