package ec.gob.igm.rrhh.consultorio.service.citas;

import ec.gob.igm.rrhh.consultorio.domain.model.CitCita;
import ec.gob.igm.rrhh.consultorio.domain.model.CitSlotAgenda;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.domain.model.UsuarioAuth;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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

        CitCita cita = new CitCita();
        cita.setSlot(em.getReference(CitSlotAgenda.class, idSlot));
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
        return em.merge(cita);
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

        cita.setSlot(em.getReference(CitSlotAgenda.class, idNuevoSlot));
        cita.setEstado("REPROGRAMADA");
        cita.setObservacion(observacion);
        cita.setUsrActualizacion(usuarioSesion);
        return em.merge(cita);
    }

    @Override
    public List<CitCita> listarCitasPorUsuario(Long idUsuarioPaciente) {
        if (idUsuarioPaciente == null) {
            return List.of();
        }

        return em.createQuery("""
                SELECT c
                FROM CitCita c
                JOIN FETCH c.slot s
                JOIN FETCH c.profesional p
                JOIN FETCH c.especialidad e
                WHERE c.usuarioPaciente.idUsuario = :idUsuario
                ORDER BY c.fechaInicio DESC
                """, CitCita.class)
                .setParameter("idUsuario", idUsuarioPaciente)
                .getResultList();
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
}
