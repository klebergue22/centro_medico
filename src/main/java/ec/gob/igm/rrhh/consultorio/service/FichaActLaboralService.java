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

        // INSERT
        if (e.getIdFichaActLab() == null) {

            // Defaults "N" (por checks de BD)
            if (e.getEsAnterior() == null) e.setEsAnterior("N");
            if (e.getEsActual() == null) e.setEsActual("N");
            if (e.getIncidente() == null) e.setIncidente("N");
            if (e.getAccidente() == null) e.setAccidente("N");
            if (e.getEnfOcupacional() == null) e.setEnfOcupacional("N");

            if (e.getFCreacion() == null) e.setFCreacion(ahora);
            if (e.getUsrCreacion() == null) e.setUsrCreacion("SYSTEM");

            em.persist(e);
            em.flush();
            return e;
        }

        // UPDATE
        e.setFActualizacion(ahora);
        if (e.getUsrActualizacion() == null) e.setUsrActualizacion("SYSTEM");

        FichaActLaboral merged = em.merge(e);
        em.flush();
        return merged;
    }

    public FichaActLaboral guardarUpsert(FichaActLaboral a, String usuario) {
        assertEm();
        if (a == null) return null;

        if (a.getFicha() == null || a.getFicha().getIdFicha() == null) {
            throw new IllegalArgumentException("La actividad laboral debe tener ficha (ID_FICHA) obligatoria.");
        }
        if (a.getNroFila() == null) {
            throw new IllegalArgumentException("NRO_FILA es obligatorio.");
        }

        final String usr = (usuario == null || usuario.trim().isEmpty()) ? "SYSTEM" : usuario.trim();
        final Date ahora = new Date();

        // Defaults
        if (a.getEsAnterior() == null) a.setEsAnterior("N");
        if (a.getEsActual() == null) a.setEsActual("N");
        if (a.getIncidente() == null) a.setIncidente("N");
        if (a.getAccidente() == null) a.setAccidente("N");
        if (a.getEnfOcupacional() == null) a.setEnfOcupacional("N");

        FichaActLaboral existente = buscarPorFichaYFila(a.getFicha().getIdFicha(), a.getNroFila());
        if (existente != null) {
            a.setIdFichaActLab(existente.getIdFichaActLab());
            a.setFCreacion(existente.getFCreacion());
            a.setUsrCreacion(existente.getUsrCreacion());
        }

        if (a.getIdFichaActLab() == null) {
            a.setFCreacion(ahora);
            a.setUsrCreacion(usr);
            em.persist(a);
            em.flush();
            return a;
        }

        a.setFActualizacion(ahora);
        a.setUsrActualizacion(usr);
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
}