package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaExamenComp;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Stateless
/**
 * Class FichaExamenCompService: encapsula reglas de negocio y acceso a datos del dominio.
 */
public class FichaExamenCompService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    private void assertEm() {
        if (em == null) {
            throw new IllegalStateException(
                "EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'."
            );
        }
    }

    public FichaExamenComp find(Long id) {
        assertEm();
        if (id == null) return null;
        return em.find(FichaExamenComp.class, id);
    }

    public List<FichaExamenComp> listarPorFicha(Long idFicha) {
        assertEm();
        if (idFicha == null) {
            return Collections.emptyList();
        }

        return em.createQuery(
                "SELECT e FROM FichaExamenComp e " +
                "WHERE e.ficha.idFicha = :idFicha " +
                "ORDER BY e.nroFila, e.fechaExamen",
                FichaExamenComp.class
        ).setParameter("idFicha", idFicha)
         .getResultList();
    }

    public FichaExamenComp guardar(FichaExamenComp e, String usuario) {
        assertEm();
        if (e == null) return null;

        final String usr = (usuario == null || usuario.trim().isEmpty()) ? "SYSTEM" : usuario.trim();
        final Date ahora = new Date();

        if (e.getIdFichaExamen() == null) {
            applyInsertAudit(e, usr, ahora);
            em.persist(e);
            return e;
        }

        applyUpdateAudit(e, usr, ahora);
        return em.merge(e);
    }

    public int eliminarPorFicha(Long idFicha) {
        assertEm();
        if (idFicha == null) return 0;

        return em.createQuery(
                "DELETE FROM FichaExamenComp e WHERE e.ficha.idFicha = :idFicha"
        ).setParameter("idFicha", idFicha)
         .executeUpdate();
    }

    public FichaExamenComp buscarPorFichaYFila(Long idFicha, Integer nroFila) {
        assertEm();
        if (idFicha == null || nroFila == null) {
            return null;
        }

        List<FichaExamenComp> res = em.createQuery(
                "SELECT e FROM FichaExamenComp e " +
                "WHERE e.ficha.idFicha = :idFicha AND e.nroFila = :nroFila",
                FichaExamenComp.class
        ).setParameter("idFicha", idFicha)
         .setParameter("nroFila", nroFila)
         .setMaxResults(1)
         .getResultList();

        return res.isEmpty() ? null : res.get(0);
    }

    public int eliminarPorFichaYFila(Long idFicha, Integer nroFila) {
        assertEm();
        if (idFicha == null || nroFila == null) return 0;

        return em.createQuery(
                "DELETE FROM FichaExamenComp e " +
                "WHERE e.ficha.idFicha = :idFicha AND e.nroFila = :nroFila"
        ).setParameter("idFicha", idFicha)
         .setParameter("nroFila", nroFila)
         .executeUpdate();
    }

    private void applyInsertAudit(FichaExamenComp e, String usr, Date ahora) {
        if (e.getfCreacion() == null) {
            e.setfCreacion(ahora);
        }
        if (e.getUsrCreacion() == null || e.getUsrCreacion().trim().isEmpty()) {
            e.setUsrCreacion(usr);
        }
        e.setfActualizacion(null);
        e.setUsrActualizacion(null);
    }

    private void applyUpdateAudit(FichaExamenComp e, String usr, Date ahora) {
        e.setfActualizacion(ahora);
        e.setUsrActualizacion(usr);
    }
}
