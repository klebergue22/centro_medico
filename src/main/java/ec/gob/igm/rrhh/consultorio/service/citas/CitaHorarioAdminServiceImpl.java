package ec.gob.igm.rrhh.consultorio.service.citas;

import ec.gob.igm.rrhh.consultorio.domain.model.CitHorarioProfesional;
import ec.gob.igm.rrhh.consultorio.domain.model.CitProfesional;
import ec.gob.igm.rrhh.consultorio.domain.model.CitSlotAgenda;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Stateless
public class CitaHorarioAdminServiceImpl implements CitaHorarioAdminService {

    private static final int MINUTOS_INICIO_JORNADA = 8 * 60;
    private static final int MINUTOS_FIN_JORNADA = 16 * 60;
    private static final int MINUTOS_INICIO_ALMUERZO = (12 * 60) + 30;
    private static final int MINUTOS_FIN_ALMUERZO = (13 * 60) + 30;

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    @Override
    public List<CitProfesional> listarProfesionalesActivos() {
        List<CitProfesional> activos = em.createQuery("""
                SELECT p
                FROM CitProfesional p
                WHERE UPPER(TRIM(COALESCE(p.activo, 'S'))) = 'S'
                ORDER BY p.nombreProfesional
                """, CitProfesional.class).getResultList();

        if (!activos.isEmpty()) {
            return activos;
        }

        return em.createQuery("""
                SELECT p
                FROM CitProfesional p
                ORDER BY p.nombreProfesional
                """, CitProfesional.class).getResultList();
    }

    @Override
    public CitHorarioProfesional guardarHorarioBase(Long idProfesional, Integer diaSemana, Integer duracionMin, String usuarioSesion) {
        validarEntrada(idProfesional, diaSemana, duracionMin);

        CitHorarioProfesional horario = em.createQuery("""
                SELECT h
                FROM CitHorarioProfesional h
                WHERE h.profesional.idProfesional = :idProfesional
                  AND h.diaSemana = :diaSemana
                ORDER BY h.idHorario DESC
                """, CitHorarioProfesional.class)
                .setParameter("idProfesional", idProfesional)
                .setParameter("diaSemana", diaSemana)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElseGet(CitHorarioProfesional::new);

        if (horario.getIdHorario() == null) {
            horario.setProfesional(em.getReference(CitProfesional.class, idProfesional));
            horario.setUsrCreacion(usuarioSesion);
        } else {
            horario.setUsrActualizacion(usuarioSesion);
        }

        horario.setDiaSemana(diaSemana);
        horario.setHoraInicioMin(MINUTOS_INICIO_JORNADA);
        horario.setHoraFinMin(MINUTOS_FIN_JORNADA);
        horario.setDuracionSlotMin(duracionMin);
        horario.setActivo("S");
        horario.setObservacion("Jornada 08:00-16:00 / Almuerzo 12:30-13:30");

        if (horario.getIdHorario() == null) {
            em.persist(horario);
            return horario;
        }
        return em.merge(horario);
    }

    @Override
    public int generarSlotsParaFecha(Long idProfesional, LocalDate fecha, String usuarioSesion) {
        if (idProfesional == null || fecha == null) {
            throw new IllegalArgumentException("Profesional y fecha son obligatorios.");
        }

        int diaSemana = mapDiaSemana(fecha.getDayOfWeek());
        CitHorarioProfesional horario = em.createQuery("""
                SELECT h
                FROM CitHorarioProfesional h
                WHERE h.profesional.idProfesional = :idProfesional
                  AND h.diaSemana = :diaSemana
                  AND h.activo = 'S'
                ORDER BY h.idHorario DESC
                """, CitHorarioProfesional.class)
                .setParameter("idProfesional", idProfesional)
                .setParameter("diaSemana", diaSemana)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe horario activo para el profesional en el día seleccionado."));

        validarDuracion(horario.getDuracionSlotMin());

        List<LocalDateTime> inicios = construirInicios(horario.getDuracionSlotMin(), fecha);
        int creados = 0;
        for (LocalDateTime inicio : inicios) {
            if (slotExiste(idProfesional, inicio)) {
                continue;
            }
            CitSlotAgenda slot = new CitSlotAgenda();
            slot.setHorario(horario);
            slot.setProfesional(horario.getProfesional());
            slot.setEspecialidad(horario.getProfesional().getEspecialidad());
            slot.setFechaInicio(toDate(inicio));
            slot.setFechaFin(toDate(inicio.plusMinutes(horario.getDuracionSlotMin())));
            slot.setEstado("DISPONIBLE");
            slot.setUsrCreacion(usuarioSesion);
            em.persist(slot);
            creados++;
        }
        return creados;
    }

    private void validarEntrada(Long idProfesional, Integer diaSemana, Integer duracionMin) {
        if (idProfesional == null) {
            throw new IllegalArgumentException("Debe seleccionar un profesional.");
        }
        if (diaSemana == null || diaSemana < 1 || diaSemana > 7) {
            throw new IllegalArgumentException("El día de la semana debe estar entre 1 y 7.");
        }
        validarDuracion(duracionMin);
    }

    private void validarDuracion(Integer duracionMin) {
        if (duracionMin == null || duracionMin < 20 || duracionMin > 30) {
            throw new IllegalArgumentException("La duración por atención debe estar entre 20 y 30 minutos.");
        }
    }

    private List<LocalDateTime> construirInicios(int duracionMin, LocalDate fecha) {
        List<LocalDateTime> inicios = new ArrayList<>();
        agregarBloque(inicios, fecha, MINUTOS_INICIO_JORNADA, MINUTOS_INICIO_ALMUERZO, duracionMin);
        agregarBloque(inicios, fecha, MINUTOS_FIN_ALMUERZO, MINUTOS_FIN_JORNADA, duracionMin);
        return inicios;
    }

    private void agregarBloque(List<LocalDateTime> inicios, LocalDate fecha, int inicioMin, int finMin, int duracionMin) {
        for (int m = inicioMin; m + duracionMin <= finMin; m += duracionMin) {
            inicios.add(LocalDateTime.of(fecha, LocalTime.MIN).plusMinutes(m));
        }
    }

    private boolean slotExiste(Long idProfesional, LocalDateTime inicio) {
        Long count = em.createQuery("""
                SELECT COUNT(s)
                FROM CitSlotAgenda s
                WHERE s.profesional.idProfesional = :idProfesional
                  AND s.fechaInicio = :fechaInicio
                """, Long.class)
                .setParameter("idProfesional", idProfesional)
                .setParameter("fechaInicio", toDate(inicio))
                .getSingleResult();
        return count != null && count > 0;
    }

    private int mapDiaSemana(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> 1;
            case TUESDAY -> 2;
            case WEDNESDAY -> 3;
            case THURSDAY -> 4;
            case FRIDAY -> 5;
            case SATURDAY -> 6;
            case SUNDAY -> 7;
        };
    }

    private Date toDate(LocalDateTime value) {
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }
}
