package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.SegPermiso;
import ec.gob.igm.rrhh.consultorio.domain.model.SegRol;
import ec.gob.igm.rrhh.consultorio.domain.model.SegRolPermiso;
import ec.gob.igm.rrhh.consultorio.domain.model.SegUsuarioRol;
import ec.gob.igm.rrhh.consultorio.domain.model.UsuarioAuth;
import jakarta.inject.Inject;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.text.Normalizer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Stateless
public class AdminSeguridadService {

    private static final String ROL_ADMIN_CODIGO = "ADMIN_SISTEMA";
    private static final String ROL_ADMIN_NOMBRE = "Administrador del sistema";
    private static final String CARGO_ADMIN_REQUERIDO = "ANALISTA SOPORTE TECNOLG. GEOINFORMATICAS";
    private static final String CARGO_ADMIN_REQUERIDO_NORMALIZADO = normalizeCargo(CARGO_ADMIN_REQUERIDO);

    private static final List<PermisoSeed> PERMISOS_ADMIN = Arrays.asList(
            new PermisoSeed("SEG_USUARIOS_ADMINISTRAR", "SEGURIDAD", "USUARIOS", "ADMINISTRAR"),
            new PermisoSeed("SEG_ROLES_ADMINISTRAR", "SEGURIDAD", "ROLES", "ADMINISTRAR"),
            new PermisoSeed("SEG_PERMISOS_ADMINISTRAR", "SEGURIDAD", "PERMISOS", "ADMINISTRAR"),
            new PermisoSeed("SEG_AGENDA_ADMINISTRAR", "AGENDA", "CITAS", "ADMINISTRAR"),
            new PermisoSeed("SEG_CONFIGURACION_ADMINISTRAR", "CONFIGURACION", "SISTEMA", "ADMINISTRAR")
    );

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;
    @Inject
    private EmpleadoRhService empleadoRhService;

    public UsuarioAuth provisionarAdministrador(String username, String nombreVisible, String email, String usuarioCreacion) {
        if (isBlank(username)) {
            throw new IllegalArgumentException("El usuario/cédula es obligatorio.");
        }

        String usernameNormalizado = username.trim();
        validarCargoAdministrador(usernameNormalizado);
        UsuarioAuth usuario = findUsuarioPorUsernameOCedula(usernameNormalizado);
        if (usuario == null) {
            usuario = crearUsuarioAdmin(usernameNormalizado, nombreVisible, email, usuarioCreacion);
        } else {
            actualizarDatosBasicos(usuario, nombreVisible, email);
            em.merge(usuario);
        }

        SegRol rolAdmin = findOrCreateRolAdmin();
        List<SegPermiso> permisosAdmin = findOrCreatePermisosAdmin();
        vincularRolPermisos(rolAdmin, permisosAdmin);
        vincularUsuarioRol(usuario, rolAdmin);

        return usuario;
    }

    public String getCargoAdminRequerido() {
        return CARGO_ADMIN_REQUERIDO;
    }

    private void validarCargoAdministrador(String usernameOCedula) {
        String cargoVigente = empleadoRhService.buscarCargoVigentePorCedula(usernameOCedula);
        String cargoNormalizado = normalizeCargo(cargoVigente);
        if (cargoNormalizado == null || !cargoNormalizado.equals(CARGO_ADMIN_REQUERIDO_NORMALIZADO)) {
            throw new IllegalArgumentException("No autorizado: el cargo vigente debe ser '" + CARGO_ADMIN_REQUERIDO + "'.");
        }
    }

    private UsuarioAuth crearUsuarioAdmin(String username, String nombreVisible, String email, String usuarioCreacion) {
        UsuarioAuth nuevo = new UsuarioAuth();
        nuevo.setUsername(username);
        nuevo.setNoCedula(username);
        nuevo.setNombreVisible(isBlank(nombreVisible) ? "Administrador " + username : nombreVisible.trim());
        nuevo.setEmail(isBlank(email) ? null : email.trim());
        nuevo.setClaveHash(hash(username));
        nuevo.setAlgoritmoHash("SHA-256");
        nuevo.setActivo("S");
        nuevo.setBloqueado("N");
        nuevo.setIntentosFallidos(0);
        nuevo.setRequiereCambioClave("S");
        nuevo.setFechaCreacion(new Date());
        nuevo.setUsrCreacion(isBlank(usuarioCreacion) ? "ADMIN_SETUP" : usuarioCreacion.trim());
        em.persist(nuevo);
        return nuevo;
    }

    private void actualizarDatosBasicos(UsuarioAuth usuario, String nombreVisible, String email) {
        usuario.setActivo("S");
        usuario.setBloqueado("N");
        if (!isBlank(nombreVisible)) {
            usuario.setNombreVisible(nombreVisible.trim());
        }
        if (!isBlank(email)) {
            usuario.setEmail(email.trim());
        }
    }

    private SegRol findOrCreateRolAdmin() {
        List<SegRol> rows = em.createQuery(
                        "SELECT r FROM SegRol r WHERE r.codigo = :codigo", SegRol.class)
                .setParameter("codigo", ROL_ADMIN_CODIGO)
                .setMaxResults(1)
                .getResultList();

        if (!rows.isEmpty()) {
            SegRol rol = rows.get(0);
            rol.setActivo("S");
            rol.setNombre(ROL_ADMIN_NOMBRE);
            return em.merge(rol);
        }

        SegRol rol = new SegRol();
        rol.setCodigo(ROL_ADMIN_CODIGO);
        rol.setNombre(ROL_ADMIN_NOMBRE);
        rol.setActivo("S");
        em.persist(rol);
        return rol;
    }

    private List<SegPermiso> findOrCreatePermisosAdmin() {
        return PERMISOS_ADMIN.stream().map(this::findOrCreatePermiso).toList();
    }

    private SegPermiso findOrCreatePermiso(PermisoSeed seed) {
        List<SegPermiso> rows = em.createQuery(
                        "SELECT p FROM SegPermiso p WHERE p.codigo = :codigo", SegPermiso.class)
                .setParameter("codigo", seed.codigo)
                .setMaxResults(1)
                .getResultList();

        if (!rows.isEmpty()) {
            SegPermiso permiso = rows.get(0);
            permiso.setModulo(seed.modulo);
            permiso.setRecurso(seed.recurso);
            permiso.setAccion(seed.accion);
            return em.merge(permiso);
        }

        SegPermiso permiso = new SegPermiso();
        permiso.setCodigo(seed.codigo);
        permiso.setModulo(seed.modulo);
        permiso.setRecurso(seed.recurso);
        permiso.setAccion(seed.accion);
        em.persist(permiso);
        return permiso;
    }

    private void vincularRolPermisos(SegRol rol, List<SegPermiso> permisos) {
        for (SegPermiso permiso : permisos) {
            List<SegRolPermiso> links = em.createQuery(
                            "SELECT rp FROM SegRolPermiso rp WHERE rp.idRol = :idRol AND rp.idPermiso = :idPermiso", SegRolPermiso.class)
                    .setParameter("idRol", rol.getIdRol())
                    .setParameter("idPermiso", permiso.getIdPermiso())
                    .setMaxResults(1)
                    .getResultList();
            if (links.isEmpty()) {
                SegRolPermiso link = new SegRolPermiso();
                link.setIdRol(rol.getIdRol());
                link.setIdPermiso(permiso.getIdPermiso());
                link.setActivo("S");
                em.persist(link);
            } else {
                SegRolPermiso link = links.get(0);
                link.setActivo("S");
                em.merge(link);
            }
        }
    }

    private void vincularUsuarioRol(UsuarioAuth usuario, SegRol rol) {
        List<SegUsuarioRol> links = em.createQuery(
                        "SELECT ur FROM SegUsuarioRol ur WHERE ur.idUsuario = :idUsuario AND ur.idRol = :idRol", SegUsuarioRol.class)
                .setParameter("idUsuario", usuario.getIdUsuario())
                .setParameter("idRol", rol.getIdRol())
                .setMaxResults(1)
                .getResultList();

        if (links.isEmpty()) {
            SegUsuarioRol ur = new SegUsuarioRol();
            ur.setIdUsuario(usuario.getIdUsuario());
            ur.setIdRol(rol.getIdRol());
            ur.setActivo("S");
            ur.setFechaDesde(new Date());
            em.persist(ur);
            return;
        }

        SegUsuarioRol ur = links.get(0);
        ur.setActivo("S");
        if (ur.getFechaDesde() == null) {
            ur.setFechaDesde(new Date());
        }
        em.merge(ur);
    }

    private UsuarioAuth findUsuarioPorUsernameOCedula(String username) {
        List<UsuarioAuth> byUsername = em.createQuery(
                        "SELECT u FROM UsuarioAuth u WHERE u.username = :username", UsuarioAuth.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultList();
        if (!byUsername.isEmpty()) {
            return byUsername.get(0);
        }

        List<UsuarioAuth> byCedula = em.createQuery(
                        "SELECT u FROM UsuarioAuth u WHERE u.noCedula = :cedula", UsuarioAuth.class)
                .setParameter("cedula", username)
                .setMaxResults(1)
                .getResultList();
        return byCedula.isEmpty() ? null : byCedula.get(0);
    }

    private String hash(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo calcular hash", e);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String normalizeCargo(String cargo) {
        if (cargo == null) {
            return null;
        }
        String stripped = Normalizer.normalize(cargo, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return stripped.replaceAll("\\s+", " ").trim().toUpperCase();
    }

    private record PermisoSeed(String codigo, String modulo, String recurso, String accion) {
    }
}
