package ec.gob.igm.rrhh.consultorio.service.citas;

import ec.gob.igm.rrhh.consultorio.domain.model.CitHorarioProfesional;
import ec.gob.igm.rrhh.consultorio.domain.model.CitEspecialidad;
import ec.gob.igm.rrhh.consultorio.domain.model.CitProfesional;
import ec.gob.igm.rrhh.consultorio.domain.model.CitSlotAgenda;
import ec.gob.igm.rrhh.consultorio.domain.model.UsuarioAuth;
import ec.gob.igm.rrhh.consultorio.service.EmailNotificationService;
import ec.gob.igm.rrhh.consultorio.service.MailConfigResolver;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.mail.MessagingException;
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
    private static final String DEFAULT_ADMIN_EMAIL = "kleber.guerra@geograficomilitar.gob.ec";

    @EJB
    private EmailNotificationService emailNotificationService;

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    @Override
    public List<CitProfesional> listarProfesionalesActivos() {
        List<CitProfesional> activos = em.createQuery("""
                SELECT p
                FROM CitProfesional p
                LEFT JOIN FETCH p.usuario
                WHERE UPPER(TRIM(COALESCE(p.activo, 'S'))) = 'S'
                ORDER BY p.nombreProfesional, COALESCE(p.usuario.nombreVisible, ''), p.idProfesional
                """, CitProfesional.class).getResultList();

        if (!activos.isEmpty()) {
            return completarNombreVisible(activos);
        }

        List<CitProfesional> todos = em.createQuery("""
                SELECT p
                FROM CitProfesional p
                LEFT JOIN FETCH p.usuario
                ORDER BY p.nombreProfesional, COALESCE(p.usuario.nombreVisible, ''), p.idProfesional
                """, CitProfesional.class).getResultList();

        return completarNombreVisible(todos);
    }

    @Override
    public List<CitProfesional> listarProfesionalesGestion() {
        return em.createQuery("""
                SELECT p
                FROM CitProfesional p
                LEFT JOIN FETCH p.especialidad
                LEFT JOIN FETCH p.usuario
                ORDER BY p.nombreProfesional, p.idProfesional
                """, CitProfesional.class).getResultList();
    }

    @Override
    public List<UsuarioAuth> listarUsuariosProfesionales() {
        return em.createQuery("""
                SELECT DISTINCT u
                FROM UsuarioAuth u, SegUsuarioRol ur
                WHERE ur.idUsuario = u.idUsuario
                  AND UPPER(TRIM(COALESCE(u.activo, 'S'))) = 'S'
                  AND UPPER(TRIM(COALESCE(ur.activo, 'S'))) = 'S'
                  AND ur.idRol IN (1, 5)
                ORDER BY u.nombreVisible
                """, UsuarioAuth.class).getResultList();
    }

    @Override
    public List<CitEspecialidad> listarEspecialidadesActivas() {
        return em.createQuery("""
                SELECT e
                FROM CitEspecialidad e
                WHERE UPPER(TRIM(COALESCE(e.activo, 'S'))) = 'S'
                ORDER BY e.nombre
                """, CitEspecialidad.class).getResultList();
    }

    @Override
    public CitProfesional crearProfesional(Long idUsuario, String nombreProfesional, String codigoProfesional, String email,
                                           Long idEspecialidad, String activo, String usuarioSesion) {
        CitProfesional profesional = new CitProfesional();
        profesional.setUsrCreacion(usuarioSesion);
        aplicarDatosProfesional(profesional, idUsuario, nombreProfesional, codigoProfesional, email, idEspecialidad, activo);
        em.persist(profesional);
        return profesional;
    }

    @Override
    public CitProfesional actualizarProfesional(Long idProfesional, Long idUsuario, String nombreProfesional, String codigoProfesional,
                                                String email, Long idEspecialidad, String activo, String usuarioSesion) {
        if (idProfesional == null) {
            throw new IllegalArgumentException("Debe seleccionar un profesional para editar.");
        }
        CitProfesional profesional = em.find(CitProfesional.class, idProfesional);
        if (profesional == null) {
            throw new IllegalArgumentException("No existe el profesional seleccionado.");
        }
        profesional.setUsrActualizacion(usuarioSesion);
        aplicarDatosProfesional(profesional, idUsuario, nombreProfesional, codigoProfesional, email, idEspecialidad, activo);
        return em.merge(profesional);
    }

    @Override
    public void eliminarProfesional(Long idProfesional, String usuarioSesion) {
        if (idProfesional == null) {
            throw new IllegalArgumentException("Debe seleccionar un profesional para eliminar.");
        }
        CitProfesional profesional = em.find(CitProfesional.class, idProfesional);
        if (profesional == null) {
            throw new IllegalArgumentException("No existe el profesional seleccionado.");
        }
        profesional.setActivo("N");
        profesional.setUsrActualizacion(usuarioSesion);
        em.merge(profesional);
    }

    @Override
    public boolean notificarAdministradorSinProfesionales(String usuarioSesion) {
        String adminEmail = MailConfigResolver.resolve("consultorio.mail.admin",
                "CONSULTORIO_MAIL_ADMIN", DEFAULT_ADMIN_EMAIL);
        String destinatario = MailConfigResolver.normalize(adminEmail);
        if (destinatario == null) {
            return false;
        }
        String asunto = "Alerta: no existen profesionales configurados";
        String cuerpo = "Se detectó que no existen profesionales activos en CONSULTORIO.CIT_PROFESIONAL.\n\n"
                + "Usuario que detectó la alerta: " + (usuarioSesion == null ? "sistema" : usuarioSesion) + "\n"
                + "Fecha: " + new Date() + "\n\n"
                + "Acción requerida: ingresar al módulo de administración de citas y registrar profesionales.";
        try {
            emailNotificationService.send(destinatario, asunto, cuerpo);
            return true;
        } catch (MessagingException e) {
            return false;
        }
    }

    private List<CitProfesional> completarNombreVisible(List<CitProfesional> profesionales) {
        for (CitProfesional profesional : profesionales) {
            if (profesional == null) {
                continue;
            }
            String nombre = profesional.getNombreProfesional();
            if (nombre != null && !nombre.isBlank()) {
                continue;
            }
            UsuarioAuth usuario = profesional.getUsuario();
            if (usuario != null && usuario.getNombreVisible() != null && !usuario.getNombreVisible().isBlank()) {
                profesional.setNombreProfesional(usuario.getNombreVisible().trim());
            }
        }
        return profesionales;
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
    public int generarSlotsPeriodicos(Long idProfesional, LocalDate fechaBase, String periodicidad, Integer ciclos, String usuarioSesion) {
        if (idProfesional == null || fechaBase == null) {
            throw new IllegalArgumentException("Profesional y fecha base son obligatorios.");
        }
        if (ciclos == null || ciclos < 1) {
            throw new IllegalArgumentException("El número de ciclos debe ser mayor o igual a 1.");
        }

        String modo = periodicidad == null ? "SEMANAL" : periodicidad.trim().toUpperCase();
        if (!"SEMANAL".equals(modo) && !"MENSUAL".equals(modo)) {
            throw new IllegalArgumentException("La periodicidad debe ser SEMANAL o MENSUAL.");
        }

        int total = 0;
        LocalDate fecha = fechaBase;
        for (int i = 0; i < ciclos; i++) {
            total += generarSlotsParaFecha(idProfesional, fecha, usuarioSesion);
            fecha = siguienteFecha(fecha, modo);
        }
        return total;
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
        limpiarSlotsOdontologiaNoTomados(idProfesional, fecha);

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


    private LocalDate siguienteFecha(LocalDate fechaBase, String periodicidad) {
        if ("MENSUAL".equals(periodicidad)) {
            return fechaBase.plusMonths(1);
        }
        return fechaBase.plusWeeks(1);
    }

    private void limpiarSlotsOdontologiaNoTomados(Long idProfesional, LocalDate fecha) {
        if (!esOdontologia(idProfesional)) {
            return;
        }

        Date desde = toDate(fecha.atStartOfDay());
        Date hasta = toDate(fecha.plusDays(1).atStartOfDay());

        em.createQuery("""
                DELETE FROM CitSlotAgenda s
                WHERE s.profesional.idProfesional = :idProfesional
                  AND s.fechaInicio >= :desde
                  AND s.fechaInicio < :hasta
                  AND s.estado = 'DISPONIBLE'
                """)
                .setParameter("idProfesional", idProfesional)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .executeUpdate();
    }

    private boolean esOdontologia(Long idProfesional) {
        String codigo = em.createQuery("""
                SELECT p.especialidad.codigo
                FROM CitProfesional p
                WHERE p.idProfesional = :idProfesional
                """, String.class)
                .setParameter("idProfesional", idProfesional)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
        return codigo != null && "ODONTO".equalsIgnoreCase(codigo.trim());
    }

    private void validarEntrada(Long idProfesional, Integer diaSemana, Integer duracionMin) {
        if (idProfesional == null) {
            throw new IllegalArgumentException("Debe seleccionar un profesional.");
        }
        if (diaSemana == null || diaSemana < 1 || diaSemana > 5) {
            throw new IllegalArgumentException("El día de la semana debe estar entre lunes y viernes.");
        }
        validarDuracion(duracionMin);
    }

    private void aplicarDatosProfesional(CitProfesional profesional, Long idUsuario, String nombreProfesional, String codigoProfesional,
                                         String email, Long idEspecialidad, String activo) {
        UsuarioAuth usuario = null;
        if (idUsuario != null) {
            usuario = em.find(UsuarioAuth.class, idUsuario);
            if (usuario == null) {
                throw new IllegalArgumentException("El usuario seleccionado no existe.");
            }
            profesional.setUsuario(usuario);
            profesional.setNoPersona(usuario.getNoPersona());
        } else {
            profesional.setUsuario(null);
        }

        String nombre = usuario != null ? normalizarTexto(usuario.getNombreVisible()) : normalizarTexto(nombreProfesional);
        if (nombre == null) {
            throw new IllegalArgumentException("El nombre del profesional es obligatorio.");
        }
        String emailNormalizado = usuario != null ? normalizarEmail(usuario.getEmail()) : normalizarEmail(email);
        if (idEspecialidad == null) {
            throw new IllegalArgumentException("La especialidad es obligatoria.");
        }
        CitEspecialidad especialidad = em.find(CitEspecialidad.class, idEspecialidad);
        if (especialidad == null) {
            throw new IllegalArgumentException("La especialidad seleccionada no existe.");
        }

        profesional.setNombreProfesional(nombre);
        profesional.setCodigoProfesional(normalizarTexto(codigoProfesional));
        profesional.setEmail(emailNormalizado);
        profesional.setEspecialidad(especialidad);
        profesional.setActivo("N".equalsIgnoreCase(normalizarTexto(activo)) ? "N" : "S");
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private String normalizarEmail(String valor) {
        String limpio = normalizarTexto(valor);
        if (limpio == null) {
            return null;
        }
        return limpio.toLowerCase();
    }

    private void validarDuracion(Integer duracionMin) {
        if (duracionMin == null || duracionMin != 30) {
            throw new IllegalArgumentException("La duración por atención debe ser de 30 minutos.");
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
