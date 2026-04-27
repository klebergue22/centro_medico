package ec.gob.igm.rrhh.consultorio.service.citas;

import ec.gob.igm.rrhh.consultorio.domain.model.CitEspecialidad;
import ec.gob.igm.rrhh.consultorio.domain.model.CitProfesional;
import ec.gob.igm.rrhh.consultorio.domain.model.CitSlotAgenda;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Stateless
public class CitaCatalogoServiceImpl implements CitaCatalogoService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    @Override
    public List<CitEspecialidad> listarEspecialidadesActivas() {
        List<CitEspecialidad> activas = em.createQuery("""
                SELECT e
                FROM CitEspecialidad e
                WHERE UPPER(TRIM(COALESCE(e.activo, 'S'))) = 'S'
                ORDER BY e.nombre
                """, CitEspecialidad.class)
                .getResultList();

        if (!activas.isEmpty()) {
            return activas;
        }

        return em.createQuery("""
                SELECT e
                FROM CitEspecialidad e
                ORDER BY e.nombre
                """, CitEspecialidad.class)
                .getResultList();
    }

    @Override
    public List<CitProfesional> listarProfesionalesActivos(Long idEspecialidad) {
        if (idEspecialidad == null) {
            return List.of();
        }

        List<CitProfesional> activos = em.createQuery("""
                SELECT p
                FROM CitProfesional p
                WHERE UPPER(TRIM(COALESCE(p.activo, 'S'))) = 'S'
                  AND p.especialidad.idEspecialidad = :idEspecialidad
                ORDER BY p.nombreProfesional
                """, CitProfesional.class)
                .setParameter("idEspecialidad", idEspecialidad)
                .getResultList();

        if (!activos.isEmpty()) {
            return activos;
        }

        return em.createQuery("""
                SELECT p
                FROM CitProfesional p
                WHERE p.especialidad.idEspecialidad = :idEspecialidad
                ORDER BY p.nombreProfesional
                """, CitProfesional.class)
                .setParameter("idEspecialidad", idEspecialidad)
                .getResultList();
    }

    @Override
    public List<CitSlotAgenda> listarSlotsDisponibles(Long idEspecialidad, Long idProfesional, Date fecha) {
        if (fecha == null) {
            return List.of();
        }

        LocalDate localDate = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Date desde = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date hasta = Date.from(localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        Long idProfesionalFiltrado = idProfesional;
        if (idProfesionalFiltrado == null && idEspecialidad != null && !esEspecialidadOdontologia(idEspecialidad)) {
            idProfesionalFiltrado = seleccionarProfesionalConMenorCarga(idEspecialidad, desde, hasta);
        }

        StringBuilder jpql = new StringBuilder("""
                SELECT s
                FROM CitSlotAgenda s
                WHERE s.estado = 'DISPONIBLE'
                  AND s.fechaInicio >= :desde
                  AND s.fechaInicio < :hasta
                """);

        if (idEspecialidad != null) {
            jpql.append(" AND s.especialidad.idEspecialidad = :idEspecialidad");
        }
        if (idProfesionalFiltrado != null) {
            jpql.append(" AND s.profesional.idProfesional = :idProfesional");
        }
        jpql.append(" ORDER BY s.fechaInicio");

        var query = em.createQuery(jpql.toString(), CitSlotAgenda.class)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta);

        if (idEspecialidad != null) {
            query.setParameter("idEspecialidad", idEspecialidad);
        }
        if (idProfesionalFiltrado != null) {
            query.setParameter("idProfesional", idProfesionalFiltrado);
        }

        return query.getResultList();
    }

    private boolean esEspecialidadOdontologia(Long idEspecialidad) {
        String codigo = em.createQuery("""
                SELECT e.codigo
                FROM CitEspecialidad e
                WHERE e.idEspecialidad = :idEspecialidad
                """, String.class)
                .setParameter("idEspecialidad", idEspecialidad)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
        return codigo != null && "ODONTO".equalsIgnoreCase(codigo.trim());
    }

    private Long seleccionarProfesionalConMenorCarga(Long idEspecialidad, Date desde, Date hasta) {
        List<Long> candidatos = em.createQuery("""
                SELECT DISTINCT s.profesional.idProfesional
                FROM CitSlotAgenda s
                WHERE s.estado = 'DISPONIBLE'
                  AND s.especialidad.idEspecialidad = :idEspecialidad
                  AND s.fechaInicio >= :desde
                  AND s.fechaInicio < :hasta
                """, Long.class)
                .setParameter("idEspecialidad", idEspecialidad)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();

        if (candidatos.isEmpty()) {
            return null;
        }

        return candidatos.stream()
                .map(idPro -> new ProfesionalCarga(idPro, contarCitasActivas(idPro, desde, hasta), obtenerPrimerInicioDisponible(idPro, desde, hasta)))
                .sorted((a, b) -> {
                    int byCarga = Long.compare(a.carga, b.carga);
                    if (byCarga != 0) {
                        return byCarga;
                    }
                    if (a.primerInicio == null && b.primerInicio == null) {
                        return Long.compare(a.idProfesional, b.idProfesional);
                    }
                    if (a.primerInicio == null) {
                        return 1;
                    }
                    if (b.primerInicio == null) {
                        return -1;
                    }
                    int byFecha = a.primerInicio.compareTo(b.primerInicio);
                    if (byFecha != 0) {
                        return byFecha;
                    }
                    return Long.compare(a.idProfesional, b.idProfesional);
                })
                .map(item -> item.idProfesional)
                .findFirst()
                .orElse(null);
    }

    private long contarCitasActivas(Long idProfesional, Date desde, Date hasta) {
        Long count = em.createQuery("""
                SELECT COUNT(c)
                FROM CitCita c
                WHERE c.profesional.idProfesional = :idProfesional
                  AND c.fechaInicio >= :desde
                  AND c.fechaInicio < :hasta
                  AND c.estado IN ('PROGRAMADA', 'REPROGRAMADA')
                """, Long.class)
                .setParameter("idProfesional", idProfesional)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getSingleResult();
        return Optional.ofNullable(count).orElse(0L);
    }

    private Date obtenerPrimerInicioDisponible(Long idProfesional, Date desde, Date hasta) {
        return em.createQuery("""
                SELECT MIN(s.fechaInicio)
                FROM CitSlotAgenda s
                WHERE s.estado = 'DISPONIBLE'
                  AND s.profesional.idProfesional = :idProfesional
                  AND s.fechaInicio >= :desde
                  AND s.fechaInicio < :hasta
                """, Date.class)
                .setParameter("idProfesional", idProfesional)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getSingleResult();
    }

    private static class ProfesionalCarga {
        private final Long idProfesional;
        private final long carga;
        private final Date primerInicio;

        private ProfesionalCarga(Long idProfesional, long carga, Date primerInicio) {
            this.idProfesional = idProfesional;
            this.carga = carga;
            this.primerInicio = primerInicio;
        }
    }
}
