package ec.gob.igm.rrhh.consultorio.service.citas;

import ec.gob.igm.rrhh.consultorio.domain.model.CitCita;

import java.util.List;

public interface CitaCommandService {

    CitCita agendar(Long idSlot, Long idFicha, Integer noPersona, Long idPersonaAux,
                    Long idUsuarioPaciente, String correoPaciente, String telefonoPaciente,
                    String motivoAtencion, String usuarioSesion);

    CitCita cancelar(Long idCita, String motivoCancelacion, String usuarioSesion);

    CitCita reprogramar(Long idCita, Long idNuevoSlot, String observacion, String usuarioSesion);

    List<CitCita> listarCitasPorUsuario(Long idUsuarioPaciente);
}
