package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

import jakarta.ejb.Stateless;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;

@Stateless
public class FichaPdfDataMapper implements Serializable {

    public FichaPdfMappedData map(FichaOcupacional ficha, DatEmpleado empleadoSel, Date fechaNacimientoActual) {
        FichaPdfMappedData data = new FichaPdfMappedData();

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
        }

        data.fechaNacimiento = fechaNacimientoActual;
        if (fechaNacimientoActual != null) {
            LocalDate fn = fechaNacimientoActual.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            data.edad = Period.between(fn, LocalDate.now()).getYears();
        }

        return data;
    }
}
