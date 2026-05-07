package ec.gob.igm.rrhh.consultorio.service.citas;

import ec.gob.igm.rrhh.consultorio.domain.dto.ReporteAtrasoCitaDTO;
import ec.gob.igm.rrhh.consultorio.domain.model.CitCita;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

@Stateless
public class CitaReporteService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    public List<ReporteAtrasoCitaDTO> listarAtrasos(Date desde, Date hasta, Long idProfesional) {
        Date fechaHasta = hasta != null ? hasta : new Date();
        Date fechaDesde = desde;

        StringBuilder jpql = new StringBuilder("""
            SELECT c
            FROM CitCita c
            LEFT JOIN FETCH c.ficha f
            LEFT JOIN FETCH f.empleado e
            LEFT JOIN FETCH c.personaAux p
            LEFT JOIN FETCH c.especialidad esp
            LEFT JOIN FETCH c.profesional pro
            WHERE c.fechaInicio < :ahora
              AND c.fechaInicio <= :hasta
              AND UPPER(TRIM(c.estado)) IN ('PROGRAMADA', 'REPROGRAMADA', 'REASIGNADA')
            """);

        if (fechaDesde != null) {
            jpql.append(" AND c.fechaInicio >= :desde");
        }
        if (idProfesional != null) {
            jpql.append(" AND c.profesional.idProfesional = :idProfesional");
        }
        jpql.append(" ORDER BY c.fechaInicio DESC");

        var query = em.createQuery(jpql.toString(), CitCita.class)
                .setParameter("ahora", new Date())
                .setParameter("hasta", fechaHasta);

        if (fechaDesde != null) {
            query.setParameter("desde", fechaDesde);
        }
        if (idProfesional != null) {
            query.setParameter("idProfesional", idProfesional);
        }

        Date ahora = new Date();
        return query.getResultList().stream()
                .map(c -> toDto(c, ahora))
                .toList();
    }

    private ReporteAtrasoCitaDTO toDto(CitCita cita, Date ahora) {
        String cedula = "";
        String paciente = "SIN NOMBRE REGISTRADO";

        if (cita.getFicha() != null && cita.getFicha().getEmpleado() != null) {
            var empleado = cita.getFicha().getEmpleado();
            cedula = safe(empleado.getNoCedula());
            paciente = join(empleado.getPriApellido(), empleado.getSegApellido(), empleado.getNombres());
        } else if (cita.getPersonaAux() != null) {
            cedula = safe(cita.getPersonaAux().getCedula());
            paciente = join(cita.getPersonaAux().getApellidos(), cita.getPersonaAux().getNombres(), null);
        }

        long minutos = 0L;
        if (cita.getFechaInicio() != null && ahora.after(cita.getFechaInicio())) {
            minutos = (ahora.getTime() - cita.getFechaInicio().getTime()) / 60000L;
        }

        return new ReporteAtrasoCitaDTO(
                cita.getIdCita(),
                cedula,
                paciente,
                cita.getEspecialidad() == null ? "" : safe(cita.getEspecialidad().getNombre()),
                cita.getProfesional() == null ? "" : safe(cita.getProfesional().getNombreProfesional()),
                cita.getFechaInicio(),
                safe(cita.getEstado()),
                minutos
        );
    }

    private String join(String a, String b, String c) {
        return (safe(a) + " " + safe(b) + " " + safe(c)).trim().replaceAll("\\s+", " ");
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
