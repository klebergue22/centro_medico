package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.dto.EmpleadoCargoDTO;
import ec.gob.igm.rrhh.consultorio.web.ctrl.CentroMedicoCtrl;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private Object getSafe(Object[] row, int idx) {
        if (row == null || idx < 0 || idx >= row.length) return null;
        return row[idx];
    }

    private String toStr(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof BigDecimal bd) return bd.longValue();
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    private Date toDate(Object o) {
        if (o == null) return null;
        if (o instanceof Date d) return d;
        if (o instanceof Timestamp ts) return new Date(ts.getTime());
        return null;
    }
}
