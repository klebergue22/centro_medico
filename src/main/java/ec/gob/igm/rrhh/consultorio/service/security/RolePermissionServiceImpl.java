package ec.gob.igm.rrhh.consultorio.service.security;

import ec.gob.igm.rrhh.consultorio.domain.model.SegPermiso;
import ec.gob.igm.rrhh.consultorio.domain.model.SegRol;
import ec.gob.igm.rrhh.consultorio.domain.model.SegRolPermiso;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Stateless
public class RolePermissionServiceImpl implements RolePermissionService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

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

}
