package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.UsuarioAuth;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoRhService;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;
import ec.gob.igm.rrhh.consultorio.service.UsuarioAuthService;
import ec.gob.igm.rrhh.consultorio.service.SeguridadAccesoService;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

@Named
@RequestScoped
public class AuthCtrl implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String KEY_AUTH_USER = "AUTH_USER";
    private static final String KEY_AUTH_USER_NAME = "AUTH_USER_NAME";
    private static final String KEY_FORCE_CHANGE = "AUTH_PASSWORD_CHANGE_REQUIRED";

    @EJB
    private EmpleadoService empleadoService;
    @EJB
    private EmpleadoRhService empleadoRhService;
    @EJB
    private UsuarioAuthService usuarioAuthService;
    @EJB
    private SeguridadAccesoService seguridadAccesoService;

    private String cedula;
    private String clave;
    private String nuevaClave;
    private String confirmarNuevaClave;

    public String login() {
        String cedulaNormalizada = normalizeCedula(cedula);
        if (!isCedulaValida(cedulaNormalizada)) {
            addError("La cédula debe tener 10 dígitos numéricos.");
            return null;
        }

        DatEmpleado empleado = getEmpleadoValido(cedulaNormalizada);
        if (empleado == null) {
            addError("La cédula ingresada no corresponde a un empleado registrado.");
            audit(null, cedulaNormalizada, "INTENTO_FALLIDO", false, "Cedula no registrada en DAT_EMPLEADO");
            return null;
        }

        String cargoVigente = resolveCargoParaLogin(empleado, cedulaNormalizada);
        if (!isCargoAutorizado(cargoVigente)) {
            addError("Acceso denegado: el cargo registrado no corresponde a un perfil médico autorizado.");
            audit(null, cedulaNormalizada, "INTENTO_FALLIDO", false, "Cargo no autorizado");
            return null;
        }

        UsuarioAuth usuarioAuth = usuarioAuthService.findOrCreateByEmpleado(empleado);
        if (!usuarioAuthService.validatePassword(usuarioAuth, clave)) {
            addError("Usuario o clave inválidos.");
            audit(usuarioAuth.getIdUsuario(), cedulaNormalizada, "INTENTO_FALLIDO", false, "Clave invalida");
            return null;
        }

        usuarioAuthService.registrarLoginExitoso(usuarioAuth);

        boolean forceChange = usuarioAuthService.requiereCambioClave(usuarioAuth);
        audit(usuarioAuth.getIdUsuario(), cedulaNormalizada, "LOGIN", true, "Login exitoso");
        setSessionAuth(cedulaNormalizada, resolveNombreUsuario(empleado), forceChange);

        if (forceChange) {
            addInfo("Primer ingreso detectado. Debe cambiar su clave para continuar.");
            return "/change-password.xhtml?faces-redirect=true";
        }

        return "/pages/centroMedico.xhtml?faces-redirect=true";
    }

    public String cambiarClave() {
        String usuario = getSessionStringValue(KEY_AUTH_USER);
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

        DatEmpleado empleado = getEmpleadoValido(usuario);
        if (empleado == null) {
            addError("No se encontró el empleado asociado a la sesión.");
            return null;
        }

        UsuarioAuth usuarioAuth = usuarioAuthService.findOrCreateByEmpleado(empleado);
        usuarioAuthService.actualizarClave(usuarioAuth, nuevaClave);
        audit(usuarioAuth.getIdUsuario(), usuario, "CAMBIO_CLAVE", true, "Cambio de clave exitoso");
        setSessionForceChange(false);
        addInfo("Clave actualizada correctamente.");
        return "/pages/centroMedico.xhtml?faces-redirect=true";
    }

    public void logout() throws IOException {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String username = getSessionStringValue(KEY_AUTH_USER);
        audit(null, username, "LOGOUT", true, "Cierre de sesion manual");
        externalContext.invalidateSession();
        externalContext.redirect(externalContext.getRequestContextPath() + "/login.xhtml");
    }



    private void audit(Long idUsuario, String usernameIntentado, String evento, boolean exitoso, String detalle) {
        try {
            seguridadAccesoService.registrarEvento(idUsuario, usernameIntentado, evento, exitoso, detalle);
        } catch (RuntimeException ignored) {
            // No bloquear autenticación por un fallo de bitácora.
        }
    }

    private DatEmpleado getEmpleadoValido(String cedulaValue) {
        return empleadoService.buscarPorCedula(cedulaValue);
    }

    private String resolveCargoParaLogin(DatEmpleado empleado, String cedulaValue) {
        String cargoEmpleado = normalize(empleado.getCargoLossca());
        if (cargoEmpleado != null) {
            return cargoEmpleado;
        }
        return normalize(empleadoRhService.buscarCargoVigentePorCedula(cedulaValue));
    }

    private String resolveNombreUsuario(DatEmpleado empleado) {
        String nombre = normalize(empleado.getNombreC());
        return nombre != null ? nombre : empleado.getNoCedula();
    }

    private boolean isCargoAutorizado(String cargo) {
        String normalized = normalizeCargo(cargo);
        return normalized.contains("MEDCO")
                || normalized.contains("MEDICO GENERAL")
                || "MEDICO".equals(normalized)
                || normalized.startsWith("MEDICO ")
                || normalized.contains("DOCTOR")
                || normalized.startsWith("DR ");
    }

    private String normalizeCargo(String cargo) {
        if (cargo == null) {
            return "";
        }
        String stripped = Normalizer.normalize(cargo, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return stripped.trim().toUpperCase(Locale.ROOT);
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

    private String normalizeCedula(String value) {
        String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        String onlyDigits = normalized.replaceAll("\\D", "");
        return onlyDigits.isEmpty() ? null : onlyDigits;
    }

    private void setSessionAuth(String user, String nombreUsuario, boolean forceChange) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> session = externalContext.getSessionMap();
        session.put(KEY_AUTH_USER, user);
        session.put(KEY_AUTH_USER_NAME, nombreUsuario);
        session.put(KEY_FORCE_CHANGE, forceChange);
    }

    private void setSessionForceChange(boolean forceChange) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.getSessionMap().put(KEY_FORCE_CHANGE, forceChange);
    }

    private Object getSessionValue(String key) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        return externalContext.getSessionMap().get(key);
    }

    private String getSessionStringValue(String key) {
        Object value = getSessionValue(key);
        if (value == null) {
            return null;
        }
        return value.toString();
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
