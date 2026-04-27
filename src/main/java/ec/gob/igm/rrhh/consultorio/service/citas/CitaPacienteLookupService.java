package ec.gob.igm.rrhh.consultorio.service.citas;

import ec.gob.igm.rrhh.consultorio.domain.dto.CitaPacienteDTO;

public interface CitaPacienteLookupService {

    CitaPacienteDTO buscarPorCedula(String cedula);
}
