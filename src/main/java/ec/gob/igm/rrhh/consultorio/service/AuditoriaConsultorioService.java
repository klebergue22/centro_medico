package ec.gob.igm.rrhh.consultorio.service;





import ec.gob.igm.rrhh.consultorio.domain.model.AuditoriaConsultorio;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import java.util.Date;

@Stateless
public class AuditoriaConsultorioService {

    // Asegúrate que este unitName exista en tu persistence.xml
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
        // Diagnóstico rápido: si esto falla, tu PU/inyector está mal configurado
        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }

        AuditoriaConsultorio a = new AuditoriaConsultorio();
        a.setModulo(nvl(modulo, "MOD_APP"));
        a.setUsuario(nvl(usuario, "USR_APP"));
        a.setFecha(new Date());
        a.setAccion(nvl(accion, "ACCION"));
        a.setTablaAfecta(tablaAfecta);
        a.setCampoAfecta(campoAfecta);
        a.setObservaciones(observaciones);

        try {
            em.persist(a);

            // Útil en debug para que cualquier error de BD salga aquí (constraints, null, secuencias)
            // em.flush();

            return a;
        } catch (PersistenceException e) {
            // Aquí puedes loguear con slf4j si ya lo usas
            throw e; // No ocultes el error: mejor que reviente para ver la causa real.
        }
    }

    public AuditoriaConsultorio guardar(AuditoriaConsultorio auditoria) {
        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }
        if (auditoria == null) {
            throw new IllegalArgumentException("La auditoría no puede ser null");
        }

        if (auditoria.getId() == null) {
            em.persist(auditoria);
            // em.flush(); // opcional
            return auditoria;
        }
        return em.merge(auditoria);
    }

    private static String nvl(String v, String def) {
        return (v == null || v.trim().isEmpty()) ? def : v.trim();
    }
}

