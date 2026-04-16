package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import ec.gob.igm.rrhh.consultorio.domain.enums.GrupoSangre;
import ec.gob.igm.rrhh.consultorio.domain.enums.Sexo;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;
import ec.gob.igm.rrhh.consultorio.service.PersonaAuxService;

@Stateless
/**
 * Class FichaPdfDataMapper: orquesta la logica de presentacion y flujo web.
 */
public class FichaPdfDataMapper implements Serializable {

    @Inject
    private PersonaAuxService personaAuxService;
    @Inject
    private EmpleadoService empleadoService;

    public FichaPdfMappedData map(FichaOcupacional ficha, DatEmpleado empleadoSel, PersonaAux personaAux, Date fechaNacimientoActual) {
        FichaPdfMappedData data = new FichaPdfMappedData();
        DatEmpleado empleadoData = resolveManagedEmpleado(empleadoSel, ficha);
        PersonaAux personaAuxData = resolvePersonaAux(ficha, personaAux);
        data.personaAux = personaAuxData;

        applyFichaData(data, ficha);
        applyPersonaData(data, empleadoData, personaAuxData, fechaNacimientoActual);
        applyFechaNacimiento(data, empleadoData, personaAuxData, fechaNacimientoActual);
        applyEdad(data);
        return data;
    }

    private DatEmpleado resolveManagedEmpleado(DatEmpleado empleadoSel, FichaOcupacional ficha) {
        DatEmpleado source = empleadoSel != null ? empleadoSel : (ficha != null ? ficha.getEmpleado() : null);
        if (source == null || empleadoService == null) {
            return source;
        }
        Integer noPersona;
        try {
            noPersona = source.getNoPersona();
        } catch (RuntimeException ex) {
            return null;
        }
        if (noPersona == null) {
            return source;
        }
        DatEmpleado managed = empleadoService.buscarPorId(noPersona);
        return managed != null ? managed : source;
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

    private void applyFichaData(FichaPdfMappedData data, FichaOcupacional ficha) {
        if (ficha == null) {
            return;
        }
        data.institucion = ficha.getInstSistema();
        data.ruc = ficha.getRucEstablecimiento();
        data.centroTrabajo = ficha.getEstablecimientoCt();
        data.ciiu = ficha.getCiiu();
        data.noHistoria = ficha.getNoHistoriaClinica();
        data.noArchivo = ficha.getNoArchivo();
        data.motivoObs = ficha.getObservacion();
        data.ginecoExamen1 = ficha.getGinecoExamen1();
        data.ginecoTiempo1 = ficha.getGinecoTiempo1();
        data.ginecoResultado1 = ficha.getGinecoResultado1();
        data.ginecoExamen2 = ficha.getGinecoExamen2();
        data.ginecoTiempo2 = ficha.getGinecoTiempo2();
        data.ginecoResultado2 = ficha.getGinecoResultado2();
        data.ginecoObservacion = ficha.getGinecoObservacion();
        data.enfermedadActual = ficha.getEnfermedadProbActual();
    }

    private void applyPersonaData(FichaPdfMappedData data, DatEmpleado empleadoSel, PersonaAux personaAuxData, Date fechaNacimientoActual) {
        if (empleadoSel != null) {
            applyEmpleadoData(data, empleadoSel, fechaNacimientoActual);
            return;
        }
        if (personaAuxData != null) {
            applyPersonaAuxData(data, personaAuxData, fechaNacimientoActual);
        }
    }

    private void applyEmpleadoData(FichaPdfMappedData data, DatEmpleado empleadoSel, Date fechaNacimientoActual) {
        data.apellido1 = empleadoSel.getPriApellido();
        data.apellido2 = empleadoSel.getSegApellido();
        applyNombreEmpleado(data, empleadoSel.getNombres());
        Sexo sexoEmpleado = empleadoSel.getSexo();
        if (sexoEmpleado != null) data.sexo = sexoEmpleado.getDescripcion();
        GrupoSangre grupoSangre = empleadoSel.getGrupoSangre();
        if (grupoSangre != null) data.grupoSanguineo = grupoSangre.getCodigo();
        if (fechaNacimientoActual == null) data.fechaNacimiento = empleadoSel.getfNacimiento();
    }

    private void applyPersonaAuxData(FichaPdfMappedData data, PersonaAux personaAuxData, Date fechaNacimientoActual) {
        data.apellido1 = personaAuxData.getApellido1();
        data.apellido2 = personaAuxData.getApellido2();
        data.nombre1 = personaAuxData.getNombre1();
        data.nombre2 = personaAuxData.getNombre2();
        data.sexo = personaAuxData.getSexo();
        if (fechaNacimientoActual == null) data.fechaNacimiento = personaAuxData.getFechaNac();
    }

    private void applyNombreEmpleado(FichaPdfMappedData data, String nombres) {
        if (nombres == null) {
            return;
        }
        String[] parts = nombres.trim().split("\\s+", 2);
        data.nombre1 = parts.length > 0 ? parts[0] : "";
        data.nombre2 = parts.length > 1 ? parts[1] : "";
    }

    private void applyFechaNacimiento(FichaPdfMappedData data, DatEmpleado empleadoSel, PersonaAux personaAuxData, Date fechaNacimientoActual) {
        if (fechaNacimientoActual != null) {
            data.fechaNacimiento = fechaNacimientoActual;
            return;
        }
        if (empleadoSel != null && data.fechaNacimiento == null) {
            data.fechaNacimiento = empleadoSel.getfNacimiento();
            return;
        }
        if (personaAuxData != null && data.fechaNacimiento == null) {
            data.fechaNacimiento = personaAuxData.getFechaNac();
        }
    }

    private void applyEdad(FichaPdfMappedData data) {
        if (data.fechaNacimiento == null) {
            return;
        }
        LocalDate fn = toLocalDate(data.fechaNacimiento);
        data.edad = Period.between(fn, LocalDate.now()).getYears();
    }

    private LocalDate toLocalDate(Date fecha) {
        if (fecha instanceof java.sql.Date) {
            return ((java.sql.Date) fecha).toLocalDate();
        }
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
