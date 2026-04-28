package ec.gob.igm.rrhh.consultorio.service.citas;

import ec.gob.igm.rrhh.consultorio.domain.model.CitHorarioProfesional;
import ec.gob.igm.rrhh.consultorio.domain.model.CitEspecialidad;
import ec.gob.igm.rrhh.consultorio.domain.model.CitProfesional;
import ec.gob.igm.rrhh.consultorio.domain.model.UsuarioAuth;

import java.time.LocalDate;
import java.util.List;

public interface CitaHorarioAdminService {

    List<CitProfesional> listarProfesionalesActivos();
    List<CitProfesional> listarProfesionalesGestion();
    List<UsuarioAuth> listarUsuariosProfesionales();
    List<CitEspecialidad> listarEspecialidadesActivas();
    CitProfesional crearProfesional(Long idUsuario, String nombreProfesional, String codigoProfesional, String email, Long idEspecialidad, String activo, String usuarioSesion);
    CitProfesional actualizarProfesional(Long idProfesional, Long idUsuario, String nombreProfesional, String codigoProfesional, String email, Long idEspecialidad, String activo, String usuarioSesion);
    void eliminarProfesional(Long idProfesional, String usuarioSesion);
    boolean notificarAdministradorSinProfesionales(String usuarioSesion);

    CitHorarioProfesional guardarHorarioBase(Long idProfesional, Integer diaSemana, Integer duracionMin, String usuarioSesion);

    int generarSlotsParaFecha(Long idProfesional, LocalDate fecha, String usuarioSesion);

    int generarSlotsPeriodicos(Long idProfesional, LocalDate fechaBase, String periodicidad, Integer ciclos, String usuarioSesion);
}
