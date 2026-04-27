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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Stateless
public class AdminSeguridadService {

    private static final String ROL_ADMIN_CODIGO = "ADMIN_SISTEMA";
    private static final String ROL_ADMIN_NOMBRE = "Administrador del sistema";
    private static final String ROL_MEDICO_CODIGO = "MEDICO";
    private static final String ROL_MEDICO_NOMBRE = "Médico";
    private static final String ROL_PACIENTE_CODIGO = "PACIENTE";
    private static final String ROL_PACIENTE_NOMBRE = "Paciente";
    private static final String CARGO_ADMIN_REQUERIDO = "ANALISTA SOPORTE TECNOLG. GEOINFORMATICAS";
    private static final String CARGO_ADMIN_REQUERIDO_NORMALIZADO = normalizeCargo(CARGO_ADMIN_REQUERIDO);

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
        List<SegPermiso> permisosAdmin = findPermisosAdminExistentes();
        vincularRolPermisos(rolAdmin, permisosAdmin);
        vincularUsuarioRol(usuario, rolAdmin);

        return usuario;
    }

    public UsuarioAuth guardarUsuarioConRol(String username, String nombreVisible, String email, Long idRol, String usuarioCreacion) {
        if (isBlank(username)) {
            throw new IllegalArgumentException("El usuario/cédula es obligatorio.");
        }
        if (idRol == null) {
            throw new IllegalArgumentException("Seleccione un rol.");
        }

        SegRol rol = em.find(SegRol.class, idRol);
        if (rol == null || !"S".equalsIgnoreCase(rol.getActivo())) {
            throw new IllegalArgumentException("El rol seleccionado no existe o está inactivo.");
        }

        String usernameNormalizado = username.trim();
        UsuarioAuth usuario = findUsuarioPorUsernameOCedula(usernameNormalizado);
        if (usuario == null) {
            usuario = crearUsuarioBase(usernameNormalizado, nombreVisible, email, usuarioCreacion);
        } else {
            actualizarDatosBasicos(usuario, nombreVisible, email);
            em.merge(usuario);
        }

        vincularUsuarioRol(usuario, rol);
        return usuario;
    }

    public List<SegRol> listarRolesActivos() {
        return em.createQuery("SELECT r FROM SegRol r WHERE r.activo = 'S' ORDER BY r.nombre", SegRol.class)
                .getResultList();
    }

    public List<SegPermiso> listarPermisosPorRol(Long idRol) {
        if (idRol == null) {
            return List.of();
        }
        return em.createQuery("""
                        SELECT p
                        FROM SegPermiso p
                        WHERE p.idPermiso IN (
                            SELECT rp.idPermiso
                            FROM SegRolPermiso rp
                            WHERE rp.idRol = :idRol AND rp.activo = 'S'
                        )
                        ORDER BY p.modulo, p.nombre
                        """, SegPermiso.class)
                .setParameter("idRol", idRol)
                .getResultList();
    }

    public List<UsuarioGestionItem> listarUsuariosGestion() {
        List<UsuarioAuth> usuarios = em.createQuery(
                        "SELECT u FROM UsuarioAuth u ORDER BY u.fechaCreacion DESC, u.username", UsuarioAuth.class)
                .getResultList();
        if (usuarios.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = usuarios.stream()
                .map(UsuarioAuth::getIdUsuario)
                .toList();

        List<SegUsuarioRol> usuarioRoles = em.createQuery(
                        "SELECT ur FROM SegUsuarioRol ur WHERE ur.idUsuario IN :ids AND ur.activo = 'S'", SegUsuarioRol.class)
                .setParameter("ids", userIds)
                .getResultList();

        Set<Long> roleIds = usuarioRoles.stream().map(SegUsuarioRol::getIdRol).collect(Collectors.toSet());
        Map<Long, SegRol> rolesById = roleIds.isEmpty()
                ? Map.of()
                : em.createQuery("SELECT r FROM SegRol r WHERE r.idRol IN :ids", SegRol.class)
                .setParameter("ids", roleIds)
                .getResultList()
                .stream()
                .collect(Collectors.toMap(SegRol::getIdRol, r -> r));

        Map<Long, Set<String>> rolesPorUsuario = usuarioRoles.stream()
                .collect(Collectors.groupingBy(SegUsuarioRol::getIdUsuario,
                        Collectors.mapping(ur -> {
                            SegRol rol = rolesById.get(ur.getIdRol());
                            return rol == null ? null : rol.getNombre();
                        }, Collectors.toCollection(LinkedHashSet::new))));

        List<UsuarioGestionItem> salida = new ArrayList<>();
        for (UsuarioAuth usuario : usuarios) {
            Set<String> roles = rolesPorUsuario.getOrDefault(usuario.getIdUsuario(), Set.of())
                    .stream()
                    .filter(v -> v != null && !v.isBlank())
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            salida.add(new UsuarioGestionItem(
                    usuario.getIdUsuario(),
                    usuario.getUsername(),
                    usuario.getNombreVisible(),
                    usuario.getEmail(),
                    usuario.getActivo(),
                    usuario.getBloqueado(),
                    String.join(", ", roles)
            ));
        }
        return salida;
    }

    public void actualizarEstadoUsuario(Long idUsuario, boolean activo) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("No se encontró el usuario.");
        }
        UsuarioAuth usuario = em.find(UsuarioAuth.class, idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("No se encontró el usuario.");
        }
        usuario.setActivo(activo ? "S" : "N");
        if (activo) {
            usuario.setBloqueado("N");
        }
        em.merge(usuario);
    }

    public UsuarioAuth findUsuarioPorId(Long idUsuario) {
        if (idUsuario == null) {
            return null;
        }
        return em.find(UsuarioAuth.class, idUsuario);
    }

    public Long obtenerRolActivoPrincipal(Long idUsuario) {
        if (idUsuario == null) {
            return null;
        }
        List<Long> rows = em.createQuery("""
                        SELECT ur.idRol
                        FROM SegUsuarioRol ur
                        WHERE ur.idUsuario = :idUsuario
                          AND ur.activo = 'S'
                        ORDER BY ur.idUsuarioRol DESC
                        """, Long.class)
                .setParameter("idUsuario", idUsuario)
                .setMaxResults(1)
                .getResultList();
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void resetearClaveUsuario(Long idUsuario) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("No se encontró el usuario.");
        }
        UsuarioAuth usuario = em.find(UsuarioAuth.class, idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("No se encontró el usuario.");
        }
        String baseClave = !isBlank(usuario.getNoCedula()) ? usuario.getNoCedula() : usuario.getUsername();
        if (isBlank(baseClave)) {
            throw new IllegalArgumentException("El usuario no tiene identificador para resetear la clave.");
        }
        usuario.setClaveHash(hash(baseClave.trim()));
        usuario.setRequiereCambioClave("S");
        usuario.setFechaUltimoCambioClave(new Date());
        usuario.setIntentosFallidos(0);
        usuario.setBloqueado("N");
        em.merge(usuario);
    }

    public void asignarRolMedico(Long idUsuario) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("No se encontró el usuario.");
        }
        UsuarioAuth usuario = em.find(UsuarioAuth.class, idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("No se encontró el usuario.");
        }
        String cedula = !isBlank(usuario.getNoCedula()) ? usuario.getNoCedula() : usuario.getUsername();
        String cargoVigente = empleadoRhService.buscarCargoVigentePorCedula(cedula);
        if (!esCargoMedico(cargoVigente)) {
            throw new IllegalArgumentException("No se puede asignar rol médico: el cargo vigente no corresponde a médico.");
        }
        SegRol rolMedico = findOrCreateRolMedico();
        vincularUsuarioRol(usuario, rolMedico);
    }

    public List<PermisoRolGestionItem> listarPermisosParaGestionRol(Long idRol) {
        List<SegPermiso> permisos = em.createQuery("""
                SELECT p
                FROM SegPermiso p
                ORDER BY p.modulo, p.nombre
                """, SegPermiso.class).getResultList();

        if (idRol == null || permisos.isEmpty()) {
            return permisos.stream()
                    .map(p -> new PermisoRolGestionItem(
                            p.getIdPermiso(),
                            p.getCodigo(),
                            p.getModulo(),
                            p.getNombre(),
                            false
                    ))
                    .toList();
        }

        Set<Long> permisosActivosRol = em.createQuery("""
                SELECT rp.idPermiso
                FROM SegRolPermiso rp
                WHERE rp.idRol = :idRol
                  AND rp.activo = 'S'
                """, Long.class)
                .setParameter("idRol", idRol)
                .getResultList()
                .stream()
                .collect(Collectors.toSet());

        return permisos.stream()
                .map(p -> new PermisoRolGestionItem(
                        p.getIdPermiso(),
                        p.getCodigo(),
                        p.getModulo(),
                        p.getNombre(),
                        permisosActivosRol.contains(p.getIdPermiso())
                ))
                .toList();
    }

    public void actualizarPermisosRol(Long idRol, Set<Long> permisosHabilitados) {
        if (idRol == null) {
            throw new IllegalArgumentException("Seleccione un rol.");
        }

        SegRol rol = em.find(SegRol.class, idRol);
        if (rol == null) {
            throw new IllegalArgumentException("No se encontró el rol seleccionado.");
        }

        Set<Long> habilitados = (permisosHabilitados == null) ? Set.of() : permisosHabilitados;

        List<SegPermiso> permisos = em.createQuery("SELECT p FROM SegPermiso p", SegPermiso.class).getResultList();
        for (SegPermiso permiso : permisos) {
            List<SegRolPermiso> links = em.createQuery("""
                            SELECT rp
                            FROM SegRolPermiso rp
                            WHERE rp.idRol = :idRol
                              AND rp.idPermiso = :idPermiso
                            """, SegRolPermiso.class)
                    .setParameter("idRol", idRol)
                    .setParameter("idPermiso", permiso.getIdPermiso())
                    .setMaxResults(1)
                    .getResultList();

            boolean debeEstarActivo = habilitados.contains(permiso.getIdPermiso());
            if (links.isEmpty()) {
                if (!debeEstarActivo) {
                    continue;
                }
                SegRolPermiso nuevo = new SegRolPermiso();
                nuevo.setIdRol(idRol);
                nuevo.setIdPermiso(permiso.getIdPermiso());
                nuevo.setActivo("S");
                em.persist(nuevo);
            } else {
                SegRolPermiso link = links.get(0);
                link.setActivo(debeEstarActivo ? "S" : "N");
                em.merge(link);
            }
        }
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
        return crearUsuarioBase(username, nombreVisible, email, usuarioCreacion);
    }

    private UsuarioAuth crearUsuarioBase(String username, String nombreVisible, String email, String usuarioCreacion) {
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

    private SegRol findOrCreateRolMedico() {
        List<SegRol> rows = em.createQuery(
                        "SELECT r FROM SegRol r WHERE r.codigo = :codigo", SegRol.class)
                .setParameter("codigo", ROL_MEDICO_CODIGO)
                .setMaxResults(1)
                .getResultList();

        if (!rows.isEmpty()) {
            SegRol rol = rows.get(0);
            rol.setActivo("S");
            rol.setNombre(ROL_MEDICO_NOMBRE);
            return em.merge(rol);
        }

        SegRol rol = new SegRol();
        rol.setCodigo(ROL_MEDICO_CODIGO);
        rol.setNombre(ROL_MEDICO_NOMBRE);
        rol.setActivo("S");
        em.persist(rol);
        return rol;
    }

    private SegRol findOrCreateRolPaciente() {
        List<SegRol> rows = em.createQuery(
                        "SELECT r FROM SegRol r WHERE r.codigo = :codigo", SegRol.class)
                .setParameter("codigo", ROL_PACIENTE_CODIGO)
                .setMaxResults(1)
                .getResultList();

        if (!rows.isEmpty()) {
            SegRol rol = rows.get(0);
            rol.setActivo("S");
            rol.setNombre(ROL_PACIENTE_NOMBRE);
            return em.merge(rol);
        }

        SegRol rol = new SegRol();
        rol.setCodigo(ROL_PACIENTE_CODIGO);
        rol.setNombre(ROL_PACIENTE_NOMBRE);
        rol.setActivo("S");
        em.persist(rol);
        return rol;
    }

    private List<SegPermiso> findPermisosAdminExistentes() {
        List<SegPermiso> permisos = em.createQuery("SELECT p FROM SegPermiso p", SegPermiso.class)
                .getResultList();
        if (permisos.isEmpty()) {
            throw new IllegalStateException("No existen permisos en SEG_PERMISO. Ejecute primero el script de seguridad.");
        }
        return permisos;
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

    private boolean esCargoMedico(String cargo) {
        String normalized = normalizeCargo(cargo);
        if (normalized == null || normalized.isBlank()) {
            return false;
        }
        return normalized.contains("MEDCO")
                || normalized.contains("MEDICO GENERAL")
                || "MEDICO".equals(normalized)
                || normalized.startsWith("MEDICO ")
                || normalized.contains("DOCTOR")
                || normalized.startsWith("DR ");
    }

    private static String normalizeCargo(String cargo) {
        if (cargo == null) {
            return null;
        }
        String stripped = Normalizer.normalize(cargo, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return stripped.replaceAll("\\s+", " ").trim().toUpperCase();
    }

    public static class UsuarioGestionItem {
        private final Long idUsuario;
        private final String username;
        private final String nombreVisible;
        private final String email;
        private final String activo;
        private final String bloqueado;
        private final String roles;

        public UsuarioGestionItem(Long idUsuario, String username, String nombreVisible, String email,
                                  String activo, String bloqueado, String roles) {
            this.idUsuario = idUsuario;
            this.username = username;
            this.nombreVisible = nombreVisible;
            this.email = email;
            this.activo = activo;
            this.bloqueado = bloqueado;
            this.roles = roles;
        }

        public Long getIdUsuario() {
            return idUsuario;
        }

        public String getUsername() {
            return username;
        }

        public String getNombreVisible() {
            return nombreVisible;
        }

        public String getEmail() {
            return email;
        }

        public String getActivo() {
            return activo;
        }

        public String getBloqueado() {
            return bloqueado;
        }

        public String getRoles() {
            return roles;
        }
    }

    public static class PermisoRolGestionItem {
        private final Long idPermiso;
        private final String codigo;
        private final String modulo;
        private final String nombre;
        private final boolean habilitado;

        public PermisoRolGestionItem(Long idPermiso, String codigo, String modulo, String nombre, boolean habilitado) {
            this.idPermiso = idPermiso;
            this.codigo = codigo;
            this.modulo = modulo;
            this.nombre = nombre;
            this.habilitado = habilitado;
        }

        public Long getIdPermiso() {
            return idPermiso;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getModulo() {
            return modulo;
        }

        public String getNombre() {
            return nombre;
        }

        public boolean isHabilitado() {
            return habilitado;
        }
    }
}
