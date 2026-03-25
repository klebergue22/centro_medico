package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.AuditoriaConsultorio;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import java.util.Date;

@Stateless
/**
 * Class AuditoriaConsultorioService: encapsula reglas de negocio y acceso a datos del dominio.
 */
public class AuditoriaConsultorioService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    public AuditoriaConsultorio registrar(
            String modulo,
            String usuario,
            String accion,
            String tablaAfecta,
            String campoAfecta,
            String observaciones
    ) {
        assertEntityManager();
        AuditoriaConsultorio auditoria = buildAuditoria(modulo, usuario, accion, tablaAfecta, campoAfecta, observaciones);

        try {
            em.persist(auditoria);
            return auditoria;
        } catch (PersistenceException e) {
            throw e;
        }
    }

    public AuditoriaConsultorio guardar(AuditoriaConsultorio auditoria) {
        assertEntityManager();
        if (auditoria == null) {
            throw new IllegalArgumentException("La auditorÃ­a no puede ser null");
        }

        if (auditoria.getId() == null) {
            em.persist(auditoria);
            return auditoria;
        }
        return em.merge(auditoria);
    }

    private void assertEntityManager() {
        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }
    }

    private AuditoriaConsultorio buildAuditoria(String modulo, String usuario, String accion, String tablaAfecta,
            String campoAfecta, String observaciones) {
        AuditoriaConsultorio auditoria = new AuditoriaConsultorio();
        auditoria.setModulo(nvl(modulo, "MOD_APP"));
        auditoria.setUsuario(nvl(usuario, "USR_APP"));
        auditoria.setFecha(new Date());
        auditoria.setAccion(nvl(accion, "ACCION"));
        auditoria.setTablaAfecta(tablaAfecta);
        auditoria.setCampoAfecta(campoAfecta);
        auditoria.setObservaciones(observaciones);
        return auditoria;
    }

    private static String nvl(String v, String def) {
        return (v == null || v.trim().isEmpty()) ? def : v.trim();
    }
}
