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
 * Servicio de integración con RRHH para consultar el cargo vigente de un empleado por cédula.
 */
@Stateless
public class EmpleadoRhService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;
    private static final Logger LOG = LoggerFactory.getLogger(EmpleadoRhService.class);

    /**
     * Busca en la vista RH.VW_EMP_CONTRATO_VIGENTE por cédula.
     * Retorna null si no existe (o si no tiene contrato/cargo en la vista).
     */
    public EmpleadoCargoDTO buscarPorCedulaEnVista(String cedula) {
    String ced = cedula != null ? cedula.trim() : "";
    if (ced.isEmpty()) return null;

    String sql = """
        SELECT CARGO_DESCRIP
        FROM RH.VW_EMP_CONTRATO_VIGENTE
        WHERE NO_CEDULA = :cedula
        """;

    try {
        Query q = em.createNativeQuery(sql);
        q.setParameter("cedula", ced);
        q.setMaxResults(1);

        @SuppressWarnings("unchecked")
        List<Object> rows = q.getResultList();

        if (rows == null || rows.isEmpty() || rows.get(0) == null) return null;

        EmpleadoCargoDTO dto = new EmpleadoCargoDTO();
        dto.setCargoDescrip(rows.get(0).toString().trim());
        return dto;

    } catch (Exception e) {
        // IMPORTANTE: loguea para ver ORA-00942 / ORA-00904
        LOG.error("[RH] Error consultando VW_EMP_CONTRATO_VIGENTE para cedula=" + ced, e);
        return null;
    }
}

    /**
     * Consulta directa y robusta: devuelve SOLO el cargo/ocupación vigente (CARGO_DESCRIP).
     * Esto evita fallas si la vista cambia columnas/orden.
     */
    public String buscarCargoVigentePorCedula(String cedula) {
        final String ced = cedula == null ? null : cedula.trim();
        if (ced == null || ced.isBlank()) {
            return null;
        }

        final String sql = """
            SELECT CARGO_DESCRIP
            FROM RH.VW_EMP_CONTRATO_VIGENTE
            WHERE NO_CEDULA = :cedula
            """;

        try {
            Query q = em.createNativeQuery(sql);
            q.setParameter("cedula", ced);
            q.setMaxResults(1);

            @SuppressWarnings("unchecked")
            List<Object> rows = q.getResultList();

            if (rows == null || rows.isEmpty()) {
                return null;
            }

            Object v = rows.get(0);
            String cargo = v == null ? null : v.toString();
            return cargo == null ? null : cargo.trim();

        } catch (Exception e) {
            return null;
        }
    }
}
