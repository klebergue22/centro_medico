package ec.gob.igm.rrhh.consultorio.service.citas;

import ec.gob.igm.rrhh.consultorio.domain.dto.CitaPacienteDTO;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;
import ec.gob.igm.rrhh.consultorio.service.FichaOcupacionalService;
import ec.gob.igm.rrhh.consultorio.service.PersonaAuxService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import java.util.Date;

@Stateless
public class CitaFichaServiceImpl implements CitaFichaService {

    @EJB
    private EmpleadoService empleadoService;

    @EJB
    private PersonaAuxService personaAuxService;

    @EJB
    private FichaOcupacionalService fichaOcupacionalService;

    @Override
    public Long asegurarFichaActiva(CitaPacienteDTO paciente, String usuarioSesion) {
        if (paciente == null || paciente.getCedula() == null || paciente.getCedula().trim().isEmpty()) {
            throw new IllegalArgumentException("No se puede crear ficha: cédula inválida.");
        }

        if (paciente.getIdFichaActiva() != null) {
            return paciente.getIdFichaActiva();
        }

        String cedula = paciente.getCedula().trim();
        FichaOcupacional existente = fichaOcupacionalService.buscarFichaActivaOUltimaPorCedula(cedula);
        if (existente != null && !"ANULADA".equalsIgnoreCase(existente.getEstado())) {
            return existente.getIdFicha();
        }

        DatEmpleado empleado = empleadoService.buscarPorCedula(cedula);
        PersonaAux personaAux = null;
        if (empleado == null) {
            personaAux = personaAuxService.findByCedula(cedula);
            if (personaAux == null) {
                personaAux = crearPersonaAuxMinima(cedula, usuarioSesion);
            }
        }

        FichaOcupacional nueva = new FichaOcupacional();
        nueva.setFechaEvaluacion(new Date());
        nueva.setTipoEvaluacion("CITA");
        nueva.setEstado("BORRADOR");
        nueva.setNoHistoriaClinica(cedula);
        nueva.setNoArchivo(cedula);
        nueva.setUsrCreacion(usuarioSesion);
        nueva.setFechaCreacion(new Date());
        nueva.setEmpleado(empleado);
        nueva.setPersonaAux(personaAux);

        return fichaOcupacionalService.guardar(nueva).getIdFicha();
    }

    private PersonaAux crearPersonaAuxMinima(String cedula, String usuarioSesion) {
        PersonaAux personaAux = new PersonaAux();
        personaAux.setCedula(cedula);
        personaAux.setNombre1("POR");
        personaAux.setNombre2("DEFINIR");
        personaAux.setApellido1("POR");
        personaAux.setApellido2("DEFINIR");
        personaAux.setSexo("N");
        personaAux.setEstado("PENDIENTE");
        personaAux.setUsrCreacion(usuarioSesion);
        personaAux.setFechaCreacion(new Date());
        return personaAuxService.guardar(personaAux);
    }
}
