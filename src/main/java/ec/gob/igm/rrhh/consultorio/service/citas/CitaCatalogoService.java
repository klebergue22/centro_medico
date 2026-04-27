package ec.gob.igm.rrhh.consultorio.service.citas;

import ec.gob.igm.rrhh.consultorio.domain.model.CitEspecialidad;
import ec.gob.igm.rrhh.consultorio.domain.model.CitProfesional;
import ec.gob.igm.rrhh.consultorio.domain.model.CitSlotAgenda;

import java.util.Date;
import java.util.List;

public interface CitaCatalogoService {

    List<CitEspecialidad> listarEspecialidadesActivas();

    List<CitProfesional> listarProfesionalesActivos(Long idEspecialidad);

    List<CitSlotAgenda> listarSlotsDisponibles(Long idEspecialidad, Long idProfesional, Date fecha);
}
