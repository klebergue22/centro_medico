package ec.gob.igm.rrhh.consultorio.service.citas;

import ec.gob.igm.rrhh.consultorio.domain.model.CitHorarioProfesional;
import ec.gob.igm.rrhh.consultorio.domain.model.CitProfesional;

import java.time.LocalDate;
import java.util.List;

public interface CitaHorarioAdminService {

    List<CitProfesional> listarProfesionalesActivos();

    CitHorarioProfesional guardarHorarioBase(Long idProfesional, Integer diaSemana, Integer duracionMin, String usuarioSesion);

    int generarSlotsParaFecha(Long idProfesional, LocalDate fecha, String usuarioSesion);
}
