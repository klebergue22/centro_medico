/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.igm.rrhh.consultorio.service;

/**
 *
 * @author GUERRA_KLEBER
 */


import ec.gob.igm.rrhh.consultorio.domain.enums.EstadoContrato;
import ec.gob.igm.rrhh.consultorio.domain.model.Contratacion;
import ec.gob.igm.rrhh.consultorio.domain.model.ContratacionId;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TemporalType;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Stateless
public class ContratacionService {

   @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    private void assertEm() {
        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Contratacion crear(Contratacion c) {
        assertEm();
        if (c == null) throw new IllegalArgumentException("Contratación no puede ser null");
        em.persist(c);
        return c;
    }

    public Contratacion porId(Long noPersona, Long noCont) {
        assertEm();
        if (noPersona == null || noCont == null) return null;
        ContratacionId pk = new ContratacionId(noPersona, noCont);
        return em.find(Contratacion.class, pk);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Contratacion actualizar(Contratacion c) {
        assertEm();
        if (c == null) throw new IllegalArgumentException("Contratación no puede ser null");
        return em.merge(c);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void eliminar(Long noPersona, Long noCont) {
        assertEm();
        Contratacion c = porId(noPersona, noCont);
        if (c != null) em.remove(c);
    }

    public List<Contratacion> listarPorPersona(Long noPersona) {
        assertEm();
        if (noPersona == null) return Collections.emptyList();

        return em.createQuery(
                "SELECT c FROM Contratacion c " +
                "WHERE c.id.noPersona = :noPersona " +
                "ORDER BY c.id.noCont DESC", Contratacion.class)
            .setParameter("noPersona", noPersona)
            .getResultList();
    }

    public Long siguienteNoCont(Long noPersona) {
        assertEm();
        if (noPersona == null) return 1L;

        Long max = em.createQuery(
                "SELECT MAX(c.id.noCont) FROM Contratacion c WHERE c.id.noPersona = :noPersona", Long.class)
            .setParameter("noPersona", noPersona)
            .getSingleResult();

        return (max == null) ? 1L : (max + 1L);
    }

    public boolean existeContratoVigenteSinSalida(Long noPersona) {
        assertEm();
        if (noPersona == null) return false;

        Long cnt = em.createQuery(
                "SELECT COUNT(c) FROM Contratacion c " +
                "WHERE c.id.noPersona = :p " +
                "AND c.estado = :est " +
                "AND c.fSalida IS NULL", Long.class)
            .setParameter("p", noPersona)
            .setParameter("est", EstadoContrato.VIGENTE)
            .getSingleResult();

        return cnt != null && cnt > 0;
    }

    public Contratacion obtenerContratoVigenteSinSalida(Long noPersona) {
        assertEm();
        if (noPersona == null) return null;

        try {
            return em.createQuery(
                    "SELECT c FROM Contratacion c " +
                    "WHERE c.id.noPersona = :p " +
                    "AND c.estado = :est " +
                    "AND c.fSalida IS NULL " +
                    "ORDER BY c.fContrato DESC", Contratacion.class)
                .setParameter("p", noPersona)
                .setParameter("est", EstadoContrato.VIGENTE)
                .setMaxResults(1)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Contratacion guardar(Contratacion c) {
        assertEm();
        if (c == null) throw new IllegalArgumentException("Contratación no puede ser null");

        if (c.getEmpleado() == null || c.getEmpleado().getNoPersona() == null) {
            throw new IllegalArgumentException("Debe existir empleado y NO_PERSONA para guardar Contratación.");
        }

        // Asegurar ID siempre consistente con MapsId
        if (c.getId() == null) c.setId(new ContratacionId());
        c.getId().setNoPersona(c.getEmpleado().getNoPersona().longValue()); // ajusta si empleado usa Long

        // NUEVO: si noCont es null, generar
        if (c.getId().getNoCont() == null) {
            Long sig = siguienteNoCont(c.getId().getNoPersona());
            c.getId().setNoCont(sig);

            em.persist(c);
            em.flush();
            return c;
        }

        // EDITAR
        Contratacion merged = em.merge(c);
        em.flush();
        return merged;
    }

    public DatEmpleado obtenerEmpleado(Long noPersona) {
        assertEm();
        if (noPersona == null) return null;

        List<DatEmpleado> l = em.createQuery(
                "SELECT d FROM DatEmpleado d WHERE d.noPersona = :p", DatEmpleado.class)
            .setParameter("p", noPersona.intValue()) // ajusta según tu PK real
            .setMaxResults(1)
            .getResultList();

        return l.isEmpty() ? null : l.get(0);
    }

    public List<Contratacion> porRangoFechas(Date desde, Date hasta, int page, int size) {
        assertEm();
        if (desde == null || hasta == null) return Collections.emptyList();

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);

        return em.createQuery(
                "SELECT c FROM Contratacion c " +
                "WHERE c.fContrato BETWEEN :d AND :h " +
                "ORDER BY c.fContrato DESC", Contratacion.class)
            .setParameter("d", desde, TemporalType.DATE)
            .setParameter("h", hasta, TemporalType.DATE)
            .setFirstResult(safePage * safeSize)
            .setMaxResults(safeSize)
            .getResultList();
    }
}
