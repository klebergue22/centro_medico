package ec.gob.igm.rrhh.consultorio.service;





import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Collections;
import java.util.List;

@Stateless
public class EmpleadoService {
@PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    private void assertEm() {
        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }
    }

    public List<DatEmpleado> listarTodos() {
        assertEm();
        return em.createQuery(
                "SELECT e FROM DatEmpleado e ORDER BY e.priApellido, e.nombres",
                DatEmpleado.class
        ).getResultList();
    }

    public List<DatEmpleado> buscarPorApellidos(String filtro) {
        assertEm();

        if (filtro == null) {
            return Collections.emptyList();
        }

        String f = filtro.trim();
        if (f.isEmpty()) {
            return Collections.emptyList();
        }

        // Oracle-friendly: UPPER(...) LIKE :f
        String like = "%" + f.toUpperCase() + "%";

        return em.createQuery(
                "SELECT e FROM DatEmpleado e " +
                "WHERE UPPER(e.priApellido) LIKE :f " +
                "   OR UPPER(e.segApellido) LIKE :f " +
                "   OR UPPER(e.nombres) LIKE :f " +
                "ORDER BY e.priApellido, e.nombres",
                DatEmpleado.class
        ).setParameter("f", like)
         .getResultList();
    }

    public DatEmpleado buscarPorId(Integer noPersona) {
        assertEm();
        if (noPersona == null) return null;
        return em.find(DatEmpleado.class, noPersona);
    }

    public DatEmpleado guardar(DatEmpleado e) {
        assertEm();
        if (e == null) {
            throw new IllegalArgumentException("DatEmpleado no puede ser null");
        }

        // Si noPersona es autogenerado por secuencia/trigger, persist
        if (e.getNoPersona() == null) {
            em.persist(e);
            // em.flush(); // útil para depurar constraints
            return e;
        }
        return em.merge(e);
    }

    public void eliminar(Integer noPersona) {
        assertEm();
        if (noPersona == null) return;

        DatEmpleado e = em.find(DatEmpleado.class, noPersona);
        if (e != null) {
            em.remove(e);
        }
    }

    public DatEmpleado buscarPorCedula(String cedula) {
        assertEm();

        if (cedula == null) return null;

        String c = cedula.trim();
        if (c.isEmpty()) return null;

        List<DatEmpleado> lista = em.createQuery(
                        "SELECT e FROM DatEmpleado e WHERE e.noCedula = :ced",
                        DatEmpleado.class)
                .setParameter("ced", c)
                .setMaxResults(1)
                .getResultList();

        return lista.isEmpty() ? null : lista.get(0);
    }
}

