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

@Stateless
public class CitaPacienteLookupServiceImpl implements CitaPacienteLookupService {

    @EJB
    private EmpleadoService empleadoService;

    @EJB
    private PersonaAuxService personaAuxService;

    @EJB
    private FichaOcupacionalService fichaOcupacionalService;

    @Override
    public CitaPacienteDTO buscarPorCedula(String cedula) {
        String cedulaNorm = trimToNull(cedula);
        if (cedulaNorm == null) {
            throw new IllegalArgumentException("Debe ingresar la cédula del paciente.");
        }

        DatEmpleado empleado = empleadoService.buscarPorCedula(cedulaNorm);
        PersonaAux personaAux = null;

        if (empleado == null) {
            personaAux = personaAuxService.findByCedulaConFichaYCertificado(cedulaNorm);
            if (personaAux == null) {
                personaAux = personaAuxService.findByCedula(cedulaNorm);
            }
        }

        String nombre = null;
        Integer noPersona = null;
        Long idPersonaAux = null;

        if (empleado != null) {
            nombre = empleado.getNombreC();
            noPersona = empleado.getNoPersona();
        } else if (personaAux != null) {
            nombre = (personaAux.getApellidos() + " " + personaAux.getNombres()).trim();
            idPersonaAux = personaAux.getIdPersonaAux();
            if (personaAux.getNoPersona() != null && personaAux.getNoPersona() <= Integer.MAX_VALUE) {
                noPersona = personaAux.getNoPersona().intValue();
            }
        } else {
            return CitaPacienteDTO.requiereFicha(cedulaNorm, null, null, "PACIENTE NO REGISTRADO");
        }

        FichaOcupacional ficha = fichaOcupacionalService.buscarFichaActivaOUltimaPorCedula(cedulaNorm);
        if (ficha == null || "ANULADA".equalsIgnoreCase(ficha.getEstado())) {
            return CitaPacienteDTO.requiereFicha(cedulaNorm, noPersona, idPersonaAux, nombre);
        }

        return CitaPacienteDTO.encontradoConFicha(cedulaNorm, noPersona, idPersonaAux, ficha.getIdFicha(), nombre);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
