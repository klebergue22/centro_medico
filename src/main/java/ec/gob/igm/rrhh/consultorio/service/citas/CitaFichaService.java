package ec.gob.igm.rrhh.consultorio.service.citas;

import ec.gob.igm.rrhh.consultorio.domain.dto.CitaPacienteDTO;

public interface CitaFichaService {

    Long asegurarFichaActiva(CitaPacienteDTO paciente, String usuarioSesion);
}
