package ec.gob.igm.rrhh.consultorio.web.util;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CentroMedicoCalcUtil implements Serializable {

    private static final long serialVersionUID = 1L;

    public Integer calcularEdad(Date fechaNacimiento) {
        if (fechaNacimiento == null) {
            return null;
        }

        Calendar hoy = Calendar.getInstance();
        Calendar nac = Calendar.getInstance();
        nac.setTime(fechaNacimiento);

        limpiarHora(hoy);
        limpiarHora(nac);

        if (nac.after(hoy)) {
            return null;
        }

        int years = hoy.get(Calendar.YEAR) - nac.get(Calendar.YEAR);

        int mesHoy = hoy.get(Calendar.MONTH);
        int mesNac = nac.get(Calendar.MONTH);

        if (mesHoy < mesNac || (mesHoy == mesNac && hoy.get(Calendar.DAY_OF_MONTH) < nac.get(Calendar.DAY_OF_MONTH))) {
            years--;
        }

        return years;
    }

    public void limpiarHora(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    public Date getFechaMaximaNacimiento() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -18);
        limpiarHora(cal);
        return cal.getTime();
    }

    public boolean validarEdadMinima(Integer edad) {
        return edad == null || edad >= 18;
    }

    public Double recalcularIMC(Double peso, Double tallaCm) {
        if (peso != null && tallaCm != null && tallaCm > 0) {
            double m = tallaCm / 100.0;
            return Math.round((peso / (m * m)) * 100.0) / 100.0;
        }
        return null;
    }
}
