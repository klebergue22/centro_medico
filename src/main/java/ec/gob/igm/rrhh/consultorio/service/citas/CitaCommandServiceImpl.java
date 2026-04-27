package ec.gob.igm.rrhh.consultorio.service.citas;

import ec.gob.igm.rrhh.consultorio.domain.model.CitCita;
import ec.gob.igm.rrhh.consultorio.domain.model.CitNotificacionCorreo;
import ec.gob.igm.rrhh.consultorio.domain.model.CitSlotAgenda;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.domain.model.UsuarioAuth;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Stateless
public class CitaCommandServiceImpl implements CitaCommandService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    @Override
    public CitCita agendar(Long idSlot, Long idFicha, Integer noPersona, Long idPersonaAux,
            Long idUsuarioPaciente, String correoPaciente, String telefonoPaciente,
            String motivoAtencion, String usuarioSesion) {

        validarIds(idSlot, idFicha, idUsuarioPaciente);
        CitSlotAgenda slot = validarSlotDisponibleYDuracion(idSlot);

        CitCita cita = new CitCita();
        cita.setSlot(slot);
        cita.setFicha(em.getReference(FichaOcupacional.class, idFicha));
        cita.setUsuarioPaciente(em.getReference(UsuarioAuth.class, idUsuarioPaciente));
        cita.setNoPersona(noPersona);
        if (idPersonaAux != null) {
            cita.setPersonaAux(em.getReference(PersonaAux.class, idPersonaAux));
        }

        cita.setCorreoPaciente(correoPaciente);
        cita.setTelefonoPaciente(telefonoPaciente);
        cita.setMotivoAtencion(motivoAtencion);
        cita.setEstado("PROGRAMADA");
        cita.setOrigen("WEB");
        cita.setReqNotificar("S");
        cita.setUsrCreacion(usuarioSesion);

        em.persist(cita);
        em.flush();
        crearNotificacionProfesional(cita, "CITA_GENERADA", usuarioSesion);
        return cita;
    }

    @Override
    public CitCita cancelar(Long idCita, String motivoCancelacion, String usuarioSesion) {
        CitCita cita = em.find(CitCita.class, idCita);
        if (cita == null) {
            throw new IllegalArgumentException("No existe la cita seleccionada.");
        }

        cita.setEstado("CANCELADA");
        cita.setMotivoCancelacion(motivoCancelacion);
        cita.setUsrActualizacion(usuarioSesion);
        CitCita actualizada = em.merge(cita);
        em.flush();
        crearNotificacionProfesional(actualizada, "CITA_CANCELADA", usuarioSesion);
        return actualizada;
    }

    @Override
    public CitCita reprogramar(Long idCita, Long idNuevoSlot, String observacion, String usuarioSesion) {
        CitCita cita = em.find(CitCita.class, idCita);
        if (cita == null) {
            throw new IllegalArgumentException("No existe la cita seleccionada.");
        }

        if (idNuevoSlot == null) {
            throw new IllegalArgumentException("Debe seleccionar un nuevo horario.");
        }

        cita.setSlot(validarSlotDisponibleYDuracion(idNuevoSlot));
        cita.setEstado("REPROGRAMADA");
        cita.setObservacion(observacion);
        cita.setUsrActualizacion(usuarioSesion);
        CitCita actualizada = em.merge(cita);
        em.flush();
        crearNotificacionProfesional(actualizada, "CITA_REPROGRAMADA", usuarioSesion);
        return actualizada;
    }


    @Override
    public int gestionarPermisoProfesional(Long idProfesionalAusente, Long idProfesionalRespaldo,
                                           LocalDate fechaInicio, LocalDate fechaFin,
                                           String motivoPermiso, String usuarioSesion) {
        if (idProfesionalAusente == null || idProfesionalRespaldo == null) {
            throw new IllegalArgumentException("Debe seleccionar profesional ausente y profesional respaldo.");
        }
        if (fechaInicio == null || fechaFin == null || fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException("El rango de fechas del permiso es inválido.");
        }
        if (idProfesionalAusente.equals(idProfesionalRespaldo)) {
            throw new IllegalArgumentException("El profesional respaldo debe ser diferente al ausente.");
        }

        Date desde = toDate(fechaInicio.atStartOfDay());
        Date hasta = toDate(fechaFin.plusDays(1).atStartOfDay());

        List<CitCita> citasAfectadas = em.createQuery("""
                SELECT c
                FROM CitCita c
                WHERE c.profesional.idProfesional = :idProfesionalAusente
                  AND c.fechaInicio >= :desde
                  AND c.fechaInicio < :hasta
                  AND c.estado IN ('PROGRAMADA', 'REPROGRAMADA', 'REASIGNADA')
                ORDER BY c.fechaInicio
                """, CitCita.class)
                .setParameter("idProfesionalAusente", idProfesionalAusente)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();

        int procesadas = 0;
        for (CitCita cita : citasAfectadas) {
            boolean esOdontologia = cita.getEspecialidad() != null
                    && cita.getEspecialidad().getCodigo() != null
                    && "ODONTO".equalsIgnoreCase(cita.getEspecialidad().getCodigo().trim());

            CitSlotAgenda nuevoSlot = buscarSlotRespaldo(
                    idProfesionalRespaldo,
                    cita.getEspecialidad() == null ? null : cita.getEspecialidad().getIdEspecialidad(),
                    cita.getFechaInicio());

            if (nuevoSlot == null) {
                continue;
            }

            cita.setSlot(nuevoSlot);
            cita.setProfesional(nuevoSlot.getProfesional());
            cita.setEspecialidad(nuevoSlot.getEspecialidad());
            cita.setFechaInicio(nuevoSlot.getFechaInicio());
            cita.setFechaFin(nuevoSlot.getFechaFin());
            cita.setEstado(esOdontologia ? "REPROGRAMADA" : "REASIGNADA");
            cita.setObservacion(construirObservacionPermiso(cita.getObservacion(), motivoPermiso));
            cita.setUsrActualizacion(usuarioSesion);
            em.merge(cita);

            if (esOdontologia) {
                crearNotificacionPaciente(cita, "CITA_REPROGRAMADA_ODONTO", usuarioSesion);
            }
            crearNotificacionProfesional(cita, "CITA_REPROGRAMADA", usuarioSesion);
            procesadas++;
        }
        em.flush();
        return procesadas;
    }

    @Override
    public List<CitCita> listarCitasPorUsuario(Long idUsuarioPaciente) {
        if (idUsuarioPaciente == null) {
            return List.of();
        }

        var graph = em.createEntityGraph(CitCita.class);
        graph.addAttributeNodes("slot", "profesional", "especialidad");

        return em.createQuery("""
                SELECT c
                FROM CitCita c
                WHERE c.usuarioPaciente.idUsuario = :idUsuario
                ORDER BY c.fechaInicio DESC
                """, CitCita.class)
                .setParameter("idUsuario", idUsuarioPaciente)
                .setHint("jakarta.persistence.fetchgraph", graph)
                .getResultList();
    }


    private CitSlotAgenda buscarSlotRespaldo(Long idProfesionalRespaldo, Long idEspecialidad, Date fechaReferencia) {
        if (idEspecialidad == null) {
            return null;
        }

        List<CitSlotAgenda> candidatos = em.createQuery("""
                SELECT s
                FROM CitSlotAgenda s
                WHERE s.profesional.idProfesional = :idProfesionalRespaldo
                  AND s.especialidad.idEspecialidad = :idEspecialidad
                  AND s.estado = 'DISPONIBLE'
                  AND s.fechaInicio >= :fechaReferencia
                ORDER BY s.fechaInicio
                """, CitSlotAgenda.class)
                .setParameter("idProfesionalRespaldo", idProfesionalRespaldo)
                .setParameter("idEspecialidad", idEspecialidad)
                .setParameter("fechaReferencia", fechaReferencia)
                .setMaxResults(1)
                .getResultList();

        if (candidatos.isEmpty()) {
            return null;
        }
        return candidatos.get(0);
    }

    private String construirObservacionPermiso(String observacionActual, String motivoPermiso) {
        String detalle = (motivoPermiso == null || motivoPermiso.trim().isEmpty())
                ? "Reprogramación automática por permiso del profesional."
                : "Reprogramación automática por permiso: " + motivoPermiso.trim();

        if (observacionActual == null || observacionActual.isBlank()) {
            return detalle;
        }
        return observacionActual + " | " + detalle;
    }

    private Date toDate(LocalDateTime value) {
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    private void validarIds(Long idSlot, Long idFicha, Long idUsuarioPaciente) {
        if (idSlot == null) {
            throw new IllegalArgumentException("Debe seleccionar un horario disponible.");
        }
        if (idFicha == null) {
            throw new IllegalArgumentException("El paciente debe tener ficha activa.");
        }
        if (idUsuarioPaciente == null) {
            throw new IllegalArgumentException("No se pudo resolver el usuario autenticado.");
        }
    }

    private CitSlotAgenda validarSlotDisponibleYDuracion(Long idSlot) {
        CitSlotAgenda slot = em.find(CitSlotAgenda.class, idSlot);
        if (slot == null) {
            throw new IllegalArgumentException("No existe el slot seleccionado.");
        }
        if (!"DISPONIBLE".equalsIgnoreCase(slot.getEstado())) {
            throw new IllegalArgumentException("El profesional no tiene disponibilidad en el horario seleccionado.");
        }
        long duracion = (slot.getFechaFin().getTime() - slot.getFechaInicio().getTime()) / 60000L;
        if (duracion < 20 || duracion > 30) {
            throw new IllegalArgumentException("La atención debe durar entre 20 y 30 minutos.");
        }
        return slot;
    }

    private void crearNotificacionProfesional(CitCita cita, String evento, String usuarioSesion) {
        if (cita == null) {
            return;
        }
        String correoProfesional = null;
        if (cita.getProfesional() != null && cita.getProfesional().getEmail() != null) {
            correoProfesional = cita.getProfesional().getEmail().trim();
        } else if (cita.getSlot() != null
                && cita.getSlot().getProfesional() != null
                && cita.getSlot().getProfesional().getEmail() != null) {
            correoProfesional = cita.getSlot().getProfesional().getEmail().trim();
        }
        if (correoProfesional == null) {
            return;
        }
        if (correoProfesional.isEmpty()) {
            return;
        }

        CitNotificacionCorreo notif = new CitNotificacionCorreo();
        notif.setCita(cita);
        notif.setTipoEvento(evento);
        notif.setDestinatario(correoProfesional);
        notif.setAsunto(buildAsuntoEvento(evento));
        notif.setCuerpo(buildCuerpoEvento(cita, evento));
        notif.setEstado("PENDIENTE");
        notif.setIntentos(0);
        notif.setFechaProgramada(new java.util.Date());
        notif.setUsrCreacion(usuarioSesion);
        em.persist(notif);
    }


    private void crearNotificacionPaciente(CitCita cita, String evento, String usuarioSesion) {
        if (cita == null || cita.getCorreoPaciente() == null || cita.getCorreoPaciente().trim().isEmpty()) {
            return;
        }

        CitNotificacionCorreo notif = new CitNotificacionCorreo();
        notif.setCita(cita);
        notif.setTipoEvento(evento);
        notif.setDestinatario(cita.getCorreoPaciente().trim());
        notif.setAsunto(buildAsuntoEvento(evento));
        notif.setCuerpo(buildCuerpoEvento(cita, evento));
        notif.setEstado("PENDIENTE");
        notif.setIntentos(0);
        notif.setFechaProgramada(new Date());
        notif.setUsrCreacion(usuarioSesion);
        em.persist(notif);
    }

    private String buildAsuntoEvento(String evento) {
        return switch (evento) {
            case "CITA_GENERADA" -> "Nueva cita asignada";
            case "CITA_REPROGRAMADA" -> "Cita reprogramada";
            case "CITA_CANCELADA" -> "Cita cancelada";
            case "CITA_REPROGRAMADA_ODONTO" -> "Cita odontológica reprogramada";
            default -> "Actualización de cita";
        };
    }

    private String buildCuerpoEvento(CitCita cita, String evento) {
        String nombrePaciente = cita.getPersonaAux() != null
                ? ((cita.getPersonaAux().getApellidos() == null ? "" : cita.getPersonaAux().getApellidos())
                + " "
                + (cita.getPersonaAux().getNombres() == null ? "" : cita.getPersonaAux().getNombres())).trim()
                : "Paciente NO_PERSONA " + (cita.getNoPersona() == null ? "N/D" : cita.getNoPersona());

        java.util.Date fechaInicio = cita.getFechaInicio() != null
                ? cita.getFechaInicio()
                : (cita.getSlot() != null ? cita.getSlot().getFechaInicio() : null);
        java.util.Date fechaFin = cita.getFechaFin() != null
                ? cita.getFechaFin()
                : (cita.getSlot() != null ? cita.getSlot().getFechaFin() : null);

        return "Evento: " + evento
                + ". Paciente: " + nombrePaciente
                + ". Fecha inicio: " + fechaInicio
                + ". Fecha fin: " + fechaFin
                + ". Estado: " + cita.getEstado() + ".";
    }
}
