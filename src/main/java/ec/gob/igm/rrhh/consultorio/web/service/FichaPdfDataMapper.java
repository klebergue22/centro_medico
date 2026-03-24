package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.domain.enums.GrupoSangre;
import ec.gob.igm.rrhh.consultorio.domain.enums.Sexo;
import ec.gob.igm.rrhh.consultorio.service.PersonaAuxService;

@Stateless
/**
 * Class FichaPdfDataMapper: orquesta la lógica de presentación y flujo web.
 */
public class FichaPdfDataMapper implements Serializable {

    @Inject
    private PersonaAuxService personaAuxService;

    public FichaPdfMappedData map(FichaOcupacional ficha, DatEmpleado empleadoSel, PersonaAux personaAux, Date fechaNacimientoActual) {
        FichaPdfMappedData data = new FichaPdfMappedData();
        PersonaAux personaAuxData = resolvePersonaAux(ficha, personaAux);
        data.personaAux = personaAuxData;

        if (ficha != null) {
            data.institucion = ficha.getInstSistema();
            data.ruc = ficha.getRucEstablecimiento();
            data.centroTrabajo = ficha.getEstablecimientoCt();
            data.ciiu = ficha.getCiiu();
            data.noHistoria = ficha.getNoHistoriaClinica();
            data.noArchivo = ficha.getNoArchivo();
            data.ginecoExamen1 = ficha.getGinecoExamen1();
            data.ginecoTiempo1 = ficha.getGinecoTiempo1();
            data.ginecoResultado1 = ficha.getGinecoResultado1();
            data.ginecoExamen2 = ficha.getGinecoExamen2();
            data.ginecoTiempo2 = ficha.getGinecoTiempo2();
            data.ginecoResultado2 = ficha.getGinecoResultado2();
            data.ginecoObservacion = ficha.getGinecoObservacion();
            data.enfermedadActual = ficha.getEnfermedadProbActual();
        }

        if (empleadoSel != null) {
            data.apellido1 = empleadoSel.getPriApellido();
            data.apellido2 = empleadoSel.getSegApellido();
            String nombres = empleadoSel.getNombres();
            if (nombres != null) {
                String[] parts = nombres.trim().split("\\s+", 2);
                data.nombre1 = parts.length > 0 ? parts[0] : "";
                data.nombre2 = parts.length > 1 ? parts[1] : "";
            }
            Sexo sexoEmpleado = empleadoSel.getSexo();
            if (sexoEmpleado != null) {
                data.sexo = sexoEmpleado.getDescripcion();
            }
            GrupoSangre grupoSangre = empleadoSel.getGrupoSangre();
            if (grupoSangre != null) {
                data.grupoSanguineo = grupoSangre.getCodigo();
            }
            if (fechaNacimientoActual == null) {
                data.fechaNacimiento = empleadoSel.getfNacimiento();
            }
        } else if (personaAuxData != null) {
            data.apellido1 = personaAuxData.getApellido1();
            data.apellido2 = personaAuxData.getApellido2();
            data.nombre1 = personaAuxData.getNombre1();
            data.nombre2 = personaAuxData.getNombre2();
            data.sexo = personaAuxData.getSexo();
            if (fechaNacimientoActual == null) {
                data.fechaNacimiento = personaAuxData.getFechaNac();
            }
        }

        if (fechaNacimientoActual != null) {
            data.fechaNacimiento = fechaNacimientoActual;
        }
        if (data.fechaNacimiento != null) {
            LocalDate fn = data.fechaNacimiento.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            data.edad = Period.between(fn, LocalDate.now()).getYears();
        }

        return data;
    }

    private PersonaAux resolvePersonaAux(FichaOcupacional ficha, PersonaAux personaAux) {
        PersonaAux source = personaAux;
        if (source == null && ficha != null) {
            source = ficha.getPersonaAux();
        }
        if (source == null || source.getIdPersonaAux() == null) {
            return source;
        }
        return personaAuxService.find(source.getIdPersonaAux());
    }
}
