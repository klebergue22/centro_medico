package ec.gob.igm.rrhh.consultorio.web.service;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

@ApplicationScoped
public class CentroMedicoUiSupportService implements Serializable {

    private static final long serialVersionUID = 1L;

    public Integer calcularEdad(Date fechaNacimiento) {
        if (fechaNacimiento == null) {
            return null;
        }
        LocalDate nacimiento = fechaNacimiento.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hoy = LocalDate.now();
        if (nacimiento.isAfter(hoy)) {
            return null;
        }
        return java.time.Period.between(nacimiento, hoy).getYears();
    }

    public Double calcularImc(Double peso, Double tallaCm) {
        if (peso == null || tallaCm == null || tallaCm <= 0) {
            return null;
        }
        double m = tallaCm / 100.0;
        return Math.round((peso / (m * m)) * 100.0) / 100.0;
    }

    public void warn(String msg) {
        addMessage(FacesMessage.SEVERITY_WARN, "Step 1", msg);
    }

    public void info(String msg) {
        addMessage(FacesMessage.SEVERITY_INFO, "Step 1", msg);
    }

    public void error(String msg) {
        addMessage(FacesMessage.SEVERITY_ERROR, "Error", msg);
    }

    public void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            context.addMessage(null, new FacesMessage(severity, summary, detail));
        }
    }
}
