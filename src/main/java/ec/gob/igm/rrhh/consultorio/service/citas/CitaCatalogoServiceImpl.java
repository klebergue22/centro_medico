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
        if (idProfesional != null) {
            jpql.append(" AND s.profesional.idProfesional = :idProfesional");
        }
        jpql.append(" ORDER BY s.fechaInicio");

        var query = em.createQuery(jpql.toString(), CitSlotAgenda.class)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta);

        if (idEspecialidad != null) {
            query.setParameter("idEspecialidad", idEspecialidad);
        }
        if (idProfesional != null) {
            query.setParameter("idProfesional", idProfesional);
        }

        return query.getResultList();
    }
}
