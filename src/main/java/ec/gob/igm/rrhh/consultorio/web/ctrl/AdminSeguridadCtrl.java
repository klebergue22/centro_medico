package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.model.SegPermiso;
import ec.gob.igm.rrhh.consultorio.domain.model.UsuarioAuth;
import ec.gob.igm.rrhh.consultorio.service.AdminSeguridadService;
import ec.gob.igm.rrhh.consultorio.service.security.RolePermissionService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.util.HashSet;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Named("adminSeguridadCtrl")
@ViewScoped
public class AdminSeguridadCtrl implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient AdminSeguridadService adminSeguridadService;
    @Inject
    private transient RolePermissionService rolePermissionService;

    private String username;
    private String nombreVisible;
    private String email;
    private Long idRolSeleccionado;
    private Long idRolGestionPermisos;
    private Set<Long> permisosSeleccionados = new HashSet<>();
    private Long idUsuarioEdicion;

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

    public void guardarUsuario() {
        if (isBlank(username)) {
            addWarn("La cédula/usuario es obligatoria.");
            return;
        }
        String usuarioSesion = (String) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("AUTH_USER");
        try {
            UsuarioAuth usuario = adminSeguridadService.guardarUsuarioConRol(
                    username,
                    nombreVisible,
                    email,
                    idRolSeleccionado,
                    usuarioSesion
            );
            addInfo("Usuario guardado correctamente: " + usuario.getUsername());
            limpiarFormularioUsuario();
        } catch (IllegalArgumentException e) {
            addWarn(e.getMessage());
        }
    }

    public void cambiarEstadoUsuario(Long idUsuario, boolean activo) {
        try {
            adminSeguridadService.actualizarEstadoUsuario(idUsuario, activo);
            addInfo(activo ? "Usuario activado." : "Usuario desactivado.");
        } catch (IllegalArgumentException e) {
            addWarn(e.getMessage());
        }
    }

    public List<AdminSeguridadService.UsuarioGestionItem> getUsuarios() {
        return adminSeguridadService.listarUsuariosGestion();
    }

    public void editarUsuario(Long idUsuario) {
        UsuarioAuth usuario = adminSeguridadService.findUsuarioPorId(idUsuario);
        if (usuario == null) {
            addWarn("No se encontró el usuario seleccionado.");
            return;
        }
        idUsuarioEdicion = usuario.getIdUsuario();
        username = usuario.getUsername();
        nombreVisible = usuario.getNombreVisible();
        email = usuario.getEmail();
        idRolSeleccionado = adminSeguridadService.obtenerRolActivoPrincipal(idUsuario);
        addInfo("Usuario cargado para edición: " + usuario.getUsername());
    }

    public void resetearClaveUsuario(Long idUsuario) {
        try {
            adminSeguridadService.resetearClaveUsuario(idUsuario);
            addInfo("La clave fue reseteada por el administrador. Clave reseteada. La clave temporal es la cédula/usuario y se exigirá cambio al ingresar.");
        } catch (IllegalArgumentException e) {
            addWarn(e.getMessage());
        }
    }

    public void asignarRolMedico(Long idUsuario) {
        try {
            adminSeguridadService.asignarRolMedico(idUsuario);
            addInfo("Rol médico asignado correctamente.");
        } catch (IllegalArgumentException e) {
            addWarn(e.getMessage());
        }
    }

    public void cargarPermisosRolGestion() {
        permisosSeleccionados = new HashSet<>();
        List<RolePermissionService.PermisoRolGestionItem> permisos = getPermisosGestionRol();
        for (RolePermissionService.PermisoRolGestionItem permiso : permisos) {
            if (permiso.isHabilitado()) {
                permisosSeleccionados.add(permiso.getIdPermiso());
            }
        }
    }

    public void guardarPermisosRol() {
        try {
            rolePermissionService.actualizarPermisosRol(idRolGestionPermisos, permisosSeleccionados);
            addInfo("Permisos del rol actualizados correctamente.");
        } catch (IllegalArgumentException e) {
            addWarn(e.getMessage());
        }
    }

    public List<SegRol> getRolesActivos() {
        return adminSeguridadService.listarRolesActivos();
    }

    public List<SegPermiso> getPermisosRolSeleccionado() {
        return adminSeguridadService.listarPermisosPorRol(idRolSeleccionado);
    }

    public List<RolePermissionService.PermisoRolGestionItem> getPermisosGestionRol() {
        return rolePermissionService.listarPermisosParaGestionRol(idRolGestionPermisos);
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

    public Long getIdRolSeleccionado() {
        return idRolSeleccionado;
    }

    public void setIdRolSeleccionado(Long idRolSeleccionado) {
        this.idRolSeleccionado = idRolSeleccionado;
    }

    public Long getIdUsuarioEdicion() {
        return idUsuarioEdicion;
    }

    public Long getIdRolGestionPermisos() {
        return idRolGestionPermisos;
    }

    public void setIdRolGestionPermisos(Long idRolGestionPermisos) {
        this.idRolGestionPermisos = idRolGestionPermisos;
    }

    public Set<Long> getPermisosSeleccionados() {
        return permisosSeleccionados;
    }

    public void setPermisosSeleccionados(Set<Long> permisosSeleccionados) {
        this.permisosSeleccionados = (permisosSeleccionados == null) ? new HashSet<>() : permisosSeleccionados;
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

    private void limpiarFormularioUsuario() {
        idUsuarioEdicion = null;
        username = null;
        nombreVisible = null;
        email = null;
        idRolSeleccionado = null;
    }
}
