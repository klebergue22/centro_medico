package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.web.security.CredentialStore;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

@Named
@RequestScoped
public class AuthCtrl implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String KEY_AUTH_USER = "AUTH_USER";
    private static final String KEY_FORCE_CHANGE = "AUTH_PASSWORD_CHANGE_REQUIRED";

    @EJB
    private CredentialStore credentialStore;

    private String cedula;
    private String clave;
    private String nuevaClave;
    private String confirmarNuevaClave;

    public String login() {
        String cedulaNormalizada = normalize(cedula);
        if (!isCedulaValida(cedulaNormalizada)) {
            addError("La cédula debe tener 10 dígitos numéricos.");
            return null;
        }

        if (!credentialStore.validate(cedulaNormalizada, clave)) {
            addError("Usuario o clave inválidos.");
            return null;
        }

        boolean forceChange = credentialStore.isFirstLogin(cedulaNormalizada) && cedulaNormalizada.equals(clave);
        setSessionAuth(cedulaNormalizada, forceChange);

        if (forceChange) {
            addInfo("Primer ingreso detectado. Debe cambiar su clave para continuar.");
            return "/change-password.xhtml?faces-redirect=true";
        }

        return "/pages/centroMedico.xhtml?faces-redirect=true";
    }

    public String cambiarClave() {
        String usuario = getSessionValue(KEY_AUTH_USER);
        if (usuario == null) {
            addError("Su sesión expiró. Inicie sesión nuevamente.");
            return "/login.xhtml?faces-redirect=true";
        }

        if (!Boolean.parseBoolean(String.valueOf(getSessionValue(KEY_FORCE_CHANGE)))) {
            return "/pages/centroMedico.xhtml?faces-redirect=true";
        }

        if (nuevaClave == null || nuevaClave.length() < 8) {
            addError("La nueva clave debe tener al menos 8 caracteres.");
            return null;
        }

        if (usuario.equals(nuevaClave)) {
            addError("La nueva clave no puede ser igual a la cédula.");
            return null;
        }

        if (!nuevaClave.equals(confirmarNuevaClave)) {
            addError("La confirmación de clave no coincide.");
            return null;
        }

        credentialStore.updatePassword(usuario, nuevaClave);
        setSessionAuth(usuario, false);
        addInfo("Clave actualizada correctamente.");
        return "/pages/centroMedico.xhtml?faces-redirect=true";
    }

    public void logout() throws IOException {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.invalidateSession();
        externalContext.redirect(externalContext.getRequestContextPath() + "/login.xhtml");
    }

    private boolean isCedulaValida(String cedulaValue) {
        return cedulaValue != null && cedulaValue.matches("\\d{10}");
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void setSessionAuth(String user, boolean forceChange) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> session = externalContext.getSessionMap();
        session.put(KEY_AUTH_USER, user);
        session.put(KEY_FORCE_CHANGE, forceChange);
    }

    private Object getSessionValue(String key) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        return externalContext.getSessionMap().get(key);
    }

    private void addError(String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", detail));
    }

    private void addInfo(String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Información", detail));
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getNuevaClave() {
        return nuevaClave;
    }

    public void setNuevaClave(String nuevaClave) {
        this.nuevaClave = nuevaClave;
    }

    public String getConfirmarNuevaClave() {
        return confirmarNuevaClave;
    }

    public void setConfirmarNuevaClave(String confirmarNuevaClave) {
        this.confirmarNuevaClave = confirmarNuevaClave;
    }
}
