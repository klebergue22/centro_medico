package ec.gob.igm.rrhh.consultorio.service.security;

import ec.gob.igm.rrhh.consultorio.domain.model.SegPermiso;
import ec.gob.igm.rrhh.consultorio.domain.model.SegRol;
import ec.gob.igm.rrhh.consultorio.domain.model.SegRolPermiso;
import ec.gob.igm.rrhh.consultorio.domain.model.SegUsuarioRol;
import ec.gob.igm.rrhh.consultorio.domain.model.UsuarioAuth;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Stateless
public class RolePermissionServiceImpl implements RolePermissionService {

    private static final String ROL_PACIENTE_CODIGO = "PACIENTE";
    private static final String ROL_PACIENTE_NOMBRE = "Paciente";

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    @Override
    public SegRol crearORestaurarRolPaciente() {
        return findOrCreateRolPaciente();
    }

    @Override
    public void asignarRolPaciente(Long idUsuario) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("No se encontró el usuario.");
        }

        UsuarioAuth usuario = em.find(UsuarioAuth.class, idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("No se encontró el usuario.");
        }

        SegRol rolPaciente = findOrCreateRolPaciente();
        vincularUsuarioRol(usuario, rolPaciente);
    }

    @Override
    public List<PermisoRolGestionItem> listarPermisosParaGestionRol(Long idRol) {
        List<SegPermiso> permisos = em.createQuery("""
                SELECT p
                FROM SegPermiso p
                ORDER BY p.modulo, p.nombre
                """, SegPermiso.class).getResultList();

        if (idRol == null || permisos.isEmpty()) {
            return permisos.stream()
                    .map(p -> new PermisoRolGestionItem(
                            p.getIdPermiso(), p.getCodigo(), p.getModulo(), p.getNombre(), false
                    ))
                    .toList();
        }

        Set<Long> activos = em.createQuery("""
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
                        p.getIdPermiso(), p.getCodigo(), p.getModulo(), p.getNombre(), activos.contains(p.getIdPermiso())
                ))
                .toList();
    }

    @Override
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

            boolean activo = habilitados.contains(permiso.getIdPermiso());
            if (links.isEmpty()) {
                if (!activo) {
                    continue;
                }
                SegRolPermiso nuevo = new SegRolPermiso();
                nuevo.setIdRol(idRol);
                nuevo.setIdPermiso(permiso.getIdPermiso());
                nuevo.setActivo("S");
                em.persist(nuevo);
            } else {
                SegRolPermiso existente = links.get(0);
                existente.setActivo(activo ? "S" : "N");
                em.merge(existente);
            }
        }
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

    private void vincularUsuarioRol(UsuarioAuth usuario, SegRol rol) {
        List<SegUsuarioRol> links = em.createQuery(
                        "SELECT ur FROM SegUsuarioRol ur WHERE ur.idUsuario = :idUsuario AND ur.idRol = :idRol", SegUsuarioRol.class)
                .setParameter("idUsuario", usuario.getIdUsuario())
                .setParameter("idRol", rol.getIdRol())
                .setMaxResults(1)
                .getResultList();

        if (links.isEmpty()) {
            SegUsuarioRol nuevo = new SegUsuarioRol();
            nuevo.setIdUsuario(usuario.getIdUsuario());
            nuevo.setIdRol(rol.getIdRol());
            nuevo.setActivo("S");
            nuevo.setFechaDesde(new Date());
            em.persist(nuevo);
            return;
        }

        SegUsuarioRol existente = links.get(0);
        existente.setActivo("S");
        if (existente.getFechaDesde() == null) {
            existente.setFechaDesde(new Date());
        }
        em.merge(existente);
    }
}
