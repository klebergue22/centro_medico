package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.UsuarioAuth;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoRhService;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;
import ec.gob.igm.rrhh.consultorio.service.SecurityNotificationService;
import ec.gob.igm.rrhh.consultorio.service.SeguridadSesionAuditoriaService;
import ec.gob.igm.rrhh.consultorio.service.UsuarioAuthService;
import ec.gob.igm.rrhh.consultorio.service.SeguridadAccesoService;
import ec.gob.igm.rrhh.consultorio.service.AdminSeguridadService;
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
    private static final String KEY_AUTH_IS_ADMIN = "AUTH_IS_ADMIN";
    private static final String KEY_AUTH_ROLE = "AUTH_ROLE";
    private static final String EMAIL_DOMAIN_INSTITUCIONAL = "@geograficomilitar.gob.ec";
    private static final String ROLE_MEDICO = "MEDICO";
    private static final String ROLE_ODONTOLOGO = "ODONTOLOGO";

    @EJB
    private EmpleadoService empleadoService;
    @EJB
    private EmpleadoRhService empleadoRhService;
    @EJB
    private UsuarioAuthService usuarioAuthService;
    @EJB
    private SeguridadAccesoService seguridadAccesoService;
    @EJB
    private SecurityNotificationService securityNotificationService;
    @EJB
    private SeguridadSesionAuditoriaService seguridadSesionAuditoriaService;
    @EJB
    private AdminSeguridadService adminSeguridadService;

    private String cedula;
    private String clave;
    private String nuevaClave;
    private String confirmarNuevaClave;
    private String correoInstitucionalReset;
    private String cedulaReset;
    private String cedulaRegistro;

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
            registrarIntentoLoginFallidoAuditoria(cedulaNormalizada, "Cedula no registrada en DAT_EMPLEADO");
            return null;
        }

        String cargoVigente = resolveCargoParaLogin(empleado, cedulaNormalizada);
        if (!isCargoAutorizado(cargoVigente) && !isCargoAdmin(cargoVigente)) {
            addError("Acceso denegado: el cargo registrado no corresponde a un perfil autorizado.");
            audit(null, cedulaNormalizada, "INTENTO_FALLIDO", false, "Cargo no autorizado");
            registrarIntentoLoginFallidoAuditoria(cedulaNormalizada, "Cargo no autorizado");
            return null;
        }

        UsuarioAuth usuarioAuth = usuarioAuthService.findByUsername(cedulaNormalizada);
        if (usuarioAuth == null) {
            addError("Usuario no registrado. Use el botón \"Registrar usuario\" para crear su acceso.");
            audit(null, cedulaNormalizada, "INTENTO_FALLIDO", false, "Usuario no registrado en SEG_USUARIO");
            registrarIntentoLoginFallidoAuditoria(cedulaNormalizada, "Usuario no registrado en SEG_USUARIO");
            return null;
        }
        if (usuarioAuthService.isBloqueado(usuarioAuth)) {
            addError("Usuario bloqueado por exceder 3 intentos fallidos. Debe cambiar la clave para desbloquear su cuenta.");
            audit(usuarioAuth.getIdUsuario(), cedulaNormalizada, "INTENTO_FALLIDO", false, "Usuario bloqueado");
            registrarIntentoLoginFallidoAuditoria(cedulaNormalizada, "Usuario bloqueado");
            return null;
        }
        if (!usuarioAuthService.validatePassword(usuarioAuth, clave)) {
            int intentosRestantes = usuarioAuthService.registrarIntentoFallido(usuarioAuth);
            if (intentosRestantes == 0) {
                addError("Usuario bloqueado por exceder 3 intentos fallidos. Debe cambiar la clave para desbloquear su cuenta.");
                audit(usuarioAuth.getIdUsuario(), cedulaNormalizada, "INTENTO_FALLIDO", false, "Clave invalida. Usuario bloqueado");
                registrarIntentoLoginFallidoAuditoria(cedulaNormalizada, "Clave invalida. Usuario bloqueado");
            } else {
                addError("Usuario o clave inválidos. Le quedan " + intentosRestantes + " intento(s).");
                audit(usuarioAuth.getIdUsuario(), cedulaNormalizada, "INTENTO_FALLIDO", false,
                        "Clave invalida. Intentos restantes: " + intentosRestantes);
                registrarIntentoLoginFallidoAuditoria(cedulaNormalizada,
                        "Clave invalida. Intentos restantes: " + intentosRestantes);
            }
            return null;
        }

        usuarioAuthService.registrarLoginExitoso(usuarioAuth);

        boolean forceChange = usuarioAuthService.requiereCambioClave(usuarioAuth);
        audit(usuarioAuth.getIdUsuario(), cedulaNormalizada, "LOGIN", true, "Login exitoso");
        registrarLoginExitosoAuditoria(usuarioAuth, cedulaNormalizada);
        boolean esAdmin = usuarioAuthService.tieneRolAdminActivo(usuarioAuth.getIdUsuario());
        setSessionAuth(cedulaNormalizada, resolveNombreUsuario(empleado), forceChange, esAdmin, resolveRole(cargoVigente, esAdmin));

        if (forceChange) {
            addInfo("Primer ingreso detectado. Debe cambiar su clave para continuar.");
            return "/change-password.xhtml?faces-redirect=true";
        }

        return "/index.xhtml?faces-redirect=true";
    }

    public String cambiarClave() {
        String usuario = getSessionStringValue(KEY_AUTH_USER);
        if (usuario == null) {
            addError("Su sesión expiró. Inicie sesión nuevamente.");
            return "/login.xhtml?faces-redirect=true";
        }

        if (!Boolean.parseBoolean(String.valueOf(getSessionValue(KEY_FORCE_CHANGE)))) {
            return "/index.xhtml?faces-redirect=true";
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

        UsuarioAuth usuarioAuth = usuarioAuthService.findByUsername(usuario);
        if (usuarioAuth == null) {
            addError("No se encontró el usuario de seguridad asociado a la sesión.");
            return null;
        }
        usuarioAuthService.actualizarClave(usuarioAuth, nuevaClave);
        audit(usuarioAuth.getIdUsuario(), usuario, "CAMBIO_CLAVE", true, "Cambio de clave exitoso");
        setSessionForceChange(false);
        addInfo("Clave actualizada correctamente.");
        return "/index.xhtml?faces-redirect=true";
    }

    public void solicitarResetClave() {
        String cedulaNormalizada = normalizeCedula(cedulaReset);
        if (cedulaNormalizada == null) {
            cedulaNormalizada = normalizeCedula(cedula);
        }
        if (!isCedulaValida(cedulaNormalizada)) {
            addError("Para restablecer la clave, ingrese una cédula válida de 10 dígitos.");
            return;
        }

        DatEmpleado empleado = getEmpleadoValido(cedulaNormalizada);
        if (empleado == null) {
            addError("No se encontró un empleado registrado con esa cédula.");
            audit(null, cedulaNormalizada, "RESET_CLAVE", false, "Cedula no registrada");
            return;
        }

        String correoIngresado = normalize(correoInstitucionalReset);
        if (correoIngresado == null) {
            addError("Para restablecer la clave, ingrese el correo institucional.");
            audit(null, cedulaNormalizada, "RESET_CLAVE", false, "Correo institucional no ingresado");
            return;
        }

        if (!isCorreoInstitucionalValido(correoIngresado)) {
            addError("El correo debe terminar en " + EMAIL_DOMAIN_INSTITUCIONAL + ".");
            audit(null, cedulaNormalizada, "RESET_CLAVE", false, "Correo no institucional");
            return;
        }

        UsuarioAuth usuarioAuth = usuarioAuthService.findByUsername(cedulaNormalizada);
        if (usuarioAuth == null) {
            addError("Usuario no registrado. Debe registrarse primero en el botón \"Registrar usuario\".");
            audit(null, cedulaNormalizada, "RESET_CLAVE", false, "Usuario no registrado en SEG_USUARIO");
            return;
        }

        String correoRegistrado = normalize(usuarioAuth.getEmail());
        if (correoRegistrado == null) {
            addError("El usuario no tiene correo institucional registrado en SEG_USUARIO.");
            audit(usuarioAuth.getIdUsuario(), cedulaNormalizada, "RESET_CLAVE", false, "Usuario sin correo registrado");
            return;
        }

        if (!correoRegistrado.equalsIgnoreCase(correoIngresado)) {
            addError("El correo institucional ingresado no coincide con el registrado para el usuario.");
            audit(usuarioAuth.getIdUsuario(), cedulaNormalizada, "RESET_CLAVE", false, "Correo no coincide con SEG_USUARIO");
            return;
        }

        String claveTemporal = securityNotificationService.generarClaveTemporal();
        try {
            securityNotificationService.enviarNotificacionResetClave(correoIngresado,
                    resolveNombreUsuario(empleado), cedulaNormalizada, claveTemporal);
            usuarioAuthService.actualizarClaveTemporal(usuarioAuth, claveTemporal);
        } catch (Exception e) {
            addError("No se pudo completar el restablecimiento de clave. Intente nuevamente.");
            audit(usuarioAuth.getIdUsuario(), cedulaNormalizada, "RESET_CLAVE", false, "Error enviando correo");
            return;
        }

        addInfo("Correo enviado: se notificó al usuario (" + correoIngresado + ") y al administrador.");
        audit(usuarioAuth.getIdUsuario(), cedulaNormalizada, "RESET_CLAVE", true, "Reset enviado por correo");
    }

    public void registrarMedico() {
        String cedulaNormalizada = normalizeCedula(cedulaRegistro);
        if (!isCedulaValida(cedulaNormalizada)) {
            addError("Para registrarse, ingrese una cédula válida de 10 dígitos.");
            return;
        }

        DatEmpleado empleado = getEmpleadoValido(cedulaNormalizada);
        if (empleado == null) {
            addError("La cédula ingresada no corresponde a un empleado registrado.");
            audit(null, cedulaNormalizada, "REGISTRO_USUARIO", false, "Cedula no registrada en DAT_EMPLEADO");
            return;
        }

        String cargoVigente = resolveCargoParaLogin(empleado, cedulaNormalizada);
        if (!isCargoAutorizado(cargoVigente) && !isCargoAdmin(cargoVigente)) {
            addError("Solo perfiles médicos autorizados o el cargo administrador pueden registrarse.");
            audit(null, cedulaNormalizada, "REGISTRO_USUARIO", false, "Cargo no autorizado");
            return;
        }

        String correoInstitucional = normalize(empleado.getEmailInstitucional());
        if (!isCorreoInstitucionalValido(correoInstitucional)) {
            addError("No existe un correo institucional válido en DAT_EMPLEADO para completar el registro.");
            audit(null, cedulaNormalizada, "REGISTRO_USUARIO", false, "Correo institucional invalido");
            return;
        }

        if (usuarioAuthService.findByUsernameOrCedula(cedulaNormalizada) != null) {
            addWarn("El usuario ya está registrado. No se permite un doble registro.");
            audit(null, cedulaNormalizada, "REGISTRO_USUARIO", true, "Usuario ya existente");
            return;
        }

        if (isCargoAdmin(cargoVigente)) {
            try {
                adminSeguridadService.provisionarAdministrador(
                        cedulaNormalizada,
                        resolveNombreUsuario(empleado),
                        correoInstitucional,
                        "AUTH_AUTO"
                );
                addInfo("Registro exitoso como administrador. Ingrese con usuario (cédula) y clave inicial (su cédula).");
                audit(null, cedulaNormalizada, "REGISTRO_USUARIO", true, "Administrador registrado en SEG_USUARIO");
                return;
            } catch (RuntimeException e) {
                addError("No se pudo completar el registro de administrador. Intente nuevamente o contacte a soporte.");
                audit(null, cedulaNormalizada, "REGISTRO_USUARIO", false, "Error registrando administrador");
                return;
            }
        }

        usuarioAuthService.findOrCreateByEmpleado(empleado);
        if (isCargoOdontologo(cargoVigente)) {
            addInfo("Registro exitoso con rol ODONTOLOGO. Ingrese con usuario (cédula) y clave inicial (su cédula).");
        } else {
            addInfo("Registro exitoso. Ingrese con usuario (cédula) y clave inicial (su cédula).");
        }
        audit(null, cedulaNormalizada, "REGISTRO_USUARIO", true, "Usuario registrado en SEG_USUARIO");
    }

    public void logout() throws IOException {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String username = getSessionStringValue(KEY_AUTH_USER);
        registrarLogoutAuditoria(username, "Cierre de sesion manual");
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

    private void registrarLoginExitosoAuditoria(UsuarioAuth usuarioAuth, String cedulaNormalizada) {
        try {
            seguridadSesionAuditoriaService.registrarLoginExitoso(usuarioAuth, cedulaNormalizada);
        } catch (RuntimeException ignored) {
            // No bloquear login por fallas de auditoría secundaria.
        }
    }

    private void registrarIntentoLoginFallidoAuditoria(String cedulaNormalizada, String motivo) {
        try {
            seguridadSesionAuditoriaService.registrarIntentoFallido(cedulaNormalizada, motivo);
        } catch (RuntimeException ignored) {
            // No bloquear login por fallas de auditoría secundaria.
        }
    }

    private void registrarLogoutAuditoria(String username, String motivoCierre) {
        try {
            seguridadSesionAuditoriaService.registrarLogout(username, motivoCierre);
        } catch (RuntimeException ignored) {
            // No bloquear logout por fallas de auditoría secundaria.
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
                || normalized.startsWith("DR ")
                || isCargoOdontologo(cargo);
    }

    private boolean isCargoOdontologo(String cargo) {
        String normalized = normalizeCargo(cargo);
        return normalized.contains("ODONTOLOGO GENERAL")
                || "ODONTOLOGO".equals(normalized)
                || normalized.startsWith("ODONTOLOGO ");
    }

    private boolean isCargoAdmin(String cargo) {
        String normalized = normalizeCargo(cargo);
        return normalized.equals(normalizeCargo(adminSeguridadService.getCargoAdminRequerido()));
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

    private boolean isCorreoInstitucionalValido(String correo) {
        String normalized = normalize(correo);
        if (normalized == null) {
            return false;
        }
        return normalized.toLowerCase(Locale.ROOT).endsWith(EMAIL_DOMAIN_INSTITUCIONAL);
    }

    private String resolveRole(String cargo, boolean esAdmin) {
        if (esAdmin) {
            return UsuarioAuthService.ROL_ADMIN_SISTEMA;
        }
        return isCargoOdontologo(cargo) ? ROLE_ODONTOLOGO : ROLE_MEDICO;
    }

    private void setSessionAuth(String user, String nombreUsuario, boolean forceChange, boolean esAdmin, String role) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> session = externalContext.getSessionMap();
        session.put(KEY_AUTH_USER, user);
        session.put(KEY_AUTH_USER_NAME, nombreUsuario);
        session.put(KEY_FORCE_CHANGE, forceChange);
        session.put(KEY_AUTH_IS_ADMIN, esAdmin);
        session.put(KEY_AUTH_ROLE, role);
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

    private void addWarn(String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", detail));
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

    public String getCorreoInstitucionalReset() {
        return correoInstitucionalReset;
    }

    public void setCorreoInstitucionalReset(String correoInstitucionalReset) {
        this.correoInstitucionalReset = correoInstitucionalReset;
    }

    public String getCedulaRegistro() {
        return cedulaRegistro;
    }

    public void setCedulaRegistro(String cedulaRegistro) {
        this.cedulaRegistro = cedulaRegistro;
    }

    public String getCedulaReset() {
        return cedulaReset;
    }

    public void setCedulaReset(String cedulaReset) {
        this.cedulaReset = cedulaReset;
    }
}
