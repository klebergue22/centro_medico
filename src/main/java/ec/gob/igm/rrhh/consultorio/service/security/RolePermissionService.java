package ec.gob.igm.rrhh.consultorio.service.security;

import ec.gob.igm.rrhh.consultorio.domain.model.SegRol;

import java.util.List;
import java.util.Set;

/**
 * Contrato dedicado a la administración de roles/permisos (SRP).
 */
public interface RolePermissionService {

    SegRol crearORestaurarRolPaciente();

    void asignarRolPaciente(Long idUsuario);

    List<PermisoRolGestionItem> listarPermisosParaGestionRol(Long idRol);

    void actualizarPermisosRol(Long idRol, Set<Long> permisosHabilitados);

    class PermisoRolGestionItem {
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
