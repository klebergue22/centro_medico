package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaActLaboral;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author GUERRA_KLEBER
 */
@Stateless
public class FichaActLaboralService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    private void assertEm() {
        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }
    }

    public FichaActLaboral find(Long idFichaActLab) {
        assertEm();
        return (idFichaActLab == null) ? null : em.find(FichaActLaboral.class, idFichaActLab);
    }

    public List<FichaActLaboral> listarPorFicha(Long idFicha) {
        assertEm();
        if (idFicha == null) return Collections.emptyList();

        return em.createQuery(
                "SELECT a FROM FichaActLaboral a " +
                "WHERE a.ficha.idFicha = :idFicha " +
                "ORDER BY a.nroFila",
                FichaActLaboral.class)
            .setParameter("idFicha", idFicha)
            .getResultList();
    }

    public FichaActLaboral buscarPorFichaYFila(Long idFicha, Integer nroFila) {
        assertEm();
        if (idFicha == null || nroFila == null) return null;

        List<FichaActLaboral> res = em.createQuery(
                "SELECT a FROM FichaActLaboral a " +
                "WHERE a.ficha.idFicha = :idFicha " +
                "AND a.nroFila = :nroFila",
                FichaActLaboral.class)
            .setParameter("idFicha", idFicha)
            .setParameter("nroFila", nroFila)
            .setMaxResults(1)
            .getResultList();

        return res.isEmpty() ? null : res.get(0);
    }

    public FichaActLaboral guardar(FichaActLaboral e) {
        assertEm();
        if (e == null) return null;

        final Date ahora = new Date();
        applyBooleanDefaults(e);

        if (e.getIdFichaActLab() == null) {
            applyInsertAudit(e, ahora, "SYSTEM");
            em.persist(e);
            em.flush();
            return e;
        }

        applyUpdateAudit(e, ahora, "SYSTEM");
        FichaActLaboral merged = em.merge(e);
        em.flush();
        return merged;
    }

    public FichaActLaboral guardarUpsert(FichaActLaboral a, String usuario) {
        assertEm();
        if (a == null) return null;

        validateUpsert(a);
        final String usr = resolveUsuario(usuario);
        final Date ahora = new Date();
        applyBooleanDefaults(a);
        copyCreationAuditFromExisting(a);

        if (a.getIdFichaActLab() == null) {
            applyInsertAudit(a, ahora, usr);
            em.persist(a);
            em.flush();
            return a;
        }

        applyUpdateAudit(a, ahora, usr);
        FichaActLaboral merged = em.merge(a);
        em.flush();
        return merged;
    }

    public int eliminarPorFicha(Long idFicha) {
        assertEm();
        if (idFicha == null) return 0;

        return em.createQuery("DELETE FROM FichaActLaboral a WHERE a.ficha.idFicha = :idFicha")
            .setParameter("idFicha", idFicha)
            .executeUpdate();
    }

    public int eliminarPorFichaYFila(Long idFicha, Integer nroFila) {
        assertEm();
        if (idFicha == null || nroFila == null) return 0;

        return em.createQuery(
                "DELETE FROM FichaActLaboral a " +
                "WHERE a.ficha.idFicha = :idFicha " +
                "AND a.nroFila = :nroFila")
            .setParameter("idFicha", idFicha)
            .setParameter("nroFila", nroFila)
            .executeUpdate();
    }

    private void validateUpsert(FichaActLaboral a) {
        if (a.getFicha() == null || a.getFicha().getIdFicha() == null) {
            throw new IllegalArgumentException("La actividad laboral debe tener ficha (ID_FICHA) obligatoria.");
        }
        if (a.getNroFila() == null) {
            throw new IllegalArgumentException("NRO_FILA es obligatorio.");
        }
    }

    private String resolveUsuario(String usuario) {
        return (usuario == null || usuario.trim().isEmpty()) ? "SYSTEM" : usuario.trim();
    }

    private void applyBooleanDefaults(FichaActLaboral e) {
        if (e.getEsAnterior() == null) e.setEsAnterior("N");
        if (e.getEsActual() == null) e.setEsActual("N");
        if (e.getIncidente() == null) e.setIncidente("N");
        if (e.getAccidente() == null) e.setAccidente("N");
        if (e.getEnfOcupacional() == null) e.setEnfOcupacional("N");
    }

    private void applyInsertAudit(FichaActLaboral e, Date ahora, String usuario) {
        if (e.getFCreacion() == null) e.setFCreacion(ahora);
        if (e.getUsrCreacion() == null) e.setUsrCreacion(usuario);
    }

    private void applyUpdateAudit(FichaActLaboral e, Date ahora, String usuario) {
        e.setFActualizacion(ahora);
        if (e.getUsrActualizacion() == null) e.setUsrActualizacion(usuario);
    }

    private void copyCreationAuditFromExisting(FichaActLaboral a) {
        FichaActLaboral existente = buscarPorFichaYFila(a.getFicha().getIdFicha(), a.getNroFila());
        if (existente == null) {
            return;
        }
        a.setIdFichaActLab(existente.getIdFichaActLab());
        a.setFCreacion(existente.getFCreacion());
        a.setUsrCreacion(existente.getUsrCreacion());
    }
}
