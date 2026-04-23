package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.model.UsuarioAuth;
import ec.gob.igm.rrhh.consultorio.service.AdminSeguridadService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;

@Named("adminSeguridadCtrl")
@ViewScoped
public class AdminSeguridadCtrl implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient AdminSeguridadService adminSeguridadService;

    private String username;
    private String nombreVisible;
    private String email;

    public void verificarAccesoAdmin() throws IOException {
        Object esAdmin = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("AUTH_IS_ADMIN");
        boolean accesoPermitido = esAdmin instanceof Boolean && (Boolean) esAdmin;
        if (accesoPermitido) {
            return;
        }
        FacesContext.getCurrentInstance()
                .getExternalContext()
                .redirect(FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/index.xhtml");
    }

    public void provisionarAdministrador() {
        if (isBlank(username)) {
            addWarn("La cédula/usuario es obligatoria.");
            return;
        }

        String usuarioSesion = (String) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("AUTH_USER");

        try {
            UsuarioAuth usuario = adminSeguridadService.provisionarAdministrador(
                    username,
                    nombreVisible,
                    email,
                    usuarioSesion
            );

            addInfo("Usuario administrador configurado: " + usuario.getUsername()
                    + ". Rol ADMIN_SISTEMA y permisos críticos habilitados.");
        } catch (IllegalArgumentException e) {
            addWarn(e.getMessage());
        }
    }

    public String getCargoRequeridoAdmin() {
        return adminSeguridadService.getCargoAdminRequerido();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    public void setNombreVisible(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private void addInfo(String summary) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, summary, null));
    }

    private void addWarn(String summary) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, summary, null));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
