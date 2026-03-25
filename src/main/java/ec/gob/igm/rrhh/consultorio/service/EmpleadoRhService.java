package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.dto.EmpleadoCargoDTO;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio de integracion con RRHH para consultar el cargo vigente de un empleado por cedula.
 */
@Stateless
public class EmpleadoRhService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;
    private static final Logger LOG = LoggerFactory.getLogger(EmpleadoRhService.class);
    private static final String SQL_CARGO_VIGENTE = """
        SELECT CARGO_DESCRIP
        FROM RH.VW_EMP_CONTRATO_VIGENTE
        WHERE NO_CEDULA = :cedula
        """;

    /**
     * Busca en la vista RH.VW_EMP_CONTRATO_VIGENTE por cedula.
     * Retorna null si no existe (o si no tiene contrato/cargo en la vista).
     */
    public EmpleadoCargoDTO buscarPorCedulaEnVista(String cedula) {
        String ced = cedula != null ? cedula.trim() : "";
        if (ced.isEmpty()) return null;

        try {
            Query q = em.createNativeQuery(SQL_CARGO_VIGENTE);
            q.setParameter("cedula", ced);
            q.setMaxResults(1);

            @SuppressWarnings("unchecked")
            List<Object> rows = q.getResultList();

            if (rows == null || rows.isEmpty() || rows.get(0) == null) return null;

            EmpleadoCargoDTO dto = new EmpleadoCargoDTO();
            dto.setCargoDescrip(rows.get(0).toString().trim());
            return dto;

        } catch (Exception e) {
            LOG.error("[RH] Error consultando VW_EMP_CONTRATO_VIGENTE para cedula=" + ced, e);
            return null;
        }
    }

    /**
     * Consulta directa y robusta: devuelve SOLO el cargo/ocupacion vigente (CARGO_DESCRIP).
     * Esto evita fallas si la vista cambia columnas/orden.
     */
    public String buscarCargoVigentePorCedula(String cedula) {
        final String ced = trimToNull(cedula);
        if (ced == null) {
            return null;
        }

        try {
            return extractTrimmedValue(querySingleColumn(ced));
        } catch (Exception e) {
            return null;
        }
    }

    private List<Object> querySingleColumn(String cedula) {
        Query q = em.createNativeQuery(SQL_CARGO_VIGENTE);
        q.setParameter("cedula", cedula);
        q.setMaxResults(1);
        @SuppressWarnings("unchecked")
        List<Object> rows = q.getResultList();
        return rows;
    }

    private String extractTrimmedValue(List<Object> rows) {
        if (rows == null || rows.isEmpty() || rows.get(0) == null) {
            return null;
        }
        return rows.get(0).toString().trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isBlank()) {
            return null;
        }
        return value.trim();
    }
}
