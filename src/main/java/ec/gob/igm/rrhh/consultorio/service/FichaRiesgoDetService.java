/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.igm.rrhh.consultorio.service;

/**
 *
 * @author GUERRA_KLEBER
 */
import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgoDet;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Stateless
public class FichaRiesgoDetService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    private void assertEm() {
        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }
    }

    public FichaRiesgoDet find(Long id) {
        assertEm();
        return (id == null) ? null : em.find(FichaRiesgoDet.class, id);
    }

    public FichaRiesgoDet findByNaturalKey(Long idFicha, String grupo, String item, Integer actividadNro) {
        assertEm();
        if (idFicha == null || grupo == null || item == null || actividadNro == null) {
            return null;
        }

        List<FichaRiesgoDet> list = em.createQuery(
                "SELECT d FROM FichaRiesgoDet d "
                + "WHERE d.ficha.idFicha = :idFicha "
                + "AND d.grupo = :grupo "
                + "AND d.item = :item "
                + "AND d.actividadNro = :act",
                FichaRiesgoDet.class
        ).setParameter("idFicha", idFicha)
                .setParameter("grupo", grupo)
                .setParameter("item", item)
                .setParameter("act", actividadNro)
                .setMaxResults(1)
                .getResultList();

        return list.isEmpty() ? null : list.get(0);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public FichaRiesgoDet guardar(FichaRiesgoDet d, String usuario) {
        assertEm();
        if (d == null) {
            return null;
        }

        final String usr = (usuario == null || usuario.trim().isEmpty()) ? "SYSTEM" : usuario.trim();
        final Date ahora = new Date();

        // defaults coherentes con DDL
        if (d.getMarcado() == null || d.getMarcado().trim().isEmpty()) {
            d.setMarcado("N");
        }

        if (d.getIdFichaRiesgoDet() == null) {
            // Actualizado: getFCreacion / setFCreacion
            if (d.getFCreacion() == null) {
                d.setFCreacion(ahora);
            }
            if (d.getUsrCreacion() == null) {
                d.setUsrCreacion(usr);
            }

            em.persist(d);
            return d;
        } else {
            // Actualizado: setFActualizacion
            d.setFActualizacion(ahora);
            if (d.getUsrActualizacion() == null) {
                d.setUsrActualizacion(usr);
            }

            return em.merge(d);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public FichaRiesgoDet upsertPorNaturalKey(FichaRiesgoDet nuevo, String usuario) {
        assertEm();
        if (nuevo == null) {
            return null;
        }

        if (nuevo.getFicha() == null || nuevo.getFicha().getIdFicha() == null) {
            throw new IllegalArgumentException("FichaOcupacional (ID_FICHA) es obligatoria en FichaRiesgoDet.");
        }
        if (nuevo.getGrupo() == null || nuevo.getItem() == null || nuevo.getActividadNro() == null) {
            throw new IllegalArgumentException("GRUPO, ITEM y ACTIVIDAD_NRO son obligatorios en FichaRiesgoDet.");
        }

        final Long idFicha = nuevo.getFicha().getIdFicha();

        FichaRiesgoDet existente = findByNaturalKey(
                idFicha, nuevo.getGrupo(), nuevo.getItem(), nuevo.getActividadNro()
        );

        if (existente == null) {
            return guardar(nuevo, usuario);
        }

        // update mínimo
        existente.setMarcado((nuevo.getMarcado() == null || nuevo.getMarcado().trim().isEmpty()) ? "N" : nuevo.getMarcado());
        existente.setOrden(nuevo.getOrden());
        existente.setFichaRiesgo(nuevo.getFichaRiesgo());

        return guardar(existente, usuario);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public int eliminarPorFicha(Long idFicha) {
        assertEm();
        if (idFicha == null) {
            return 0;
        }

        return em.createQuery("DELETE FROM FichaRiesgoDet d WHERE d.ficha.idFicha = :idFicha")
                .setParameter("idFicha", idFicha)
                .executeUpdate();
    }

    public List<FichaRiesgoDet> listarPorFicha(Long idFicha) {
        assertEm();
        if (idFicha == null) {
            return java.util.Collections.emptyList();
        }

        return em.createQuery(
                "SELECT d FROM FichaRiesgoDet d "
                + "WHERE d.ficha.idFicha = :idFicha "
                + "ORDER BY "
                + "  COALESCE(d.actividadNro, 999999) ASC, "
                + "  COALESCE(d.orden, 999999) ASC, "
                + "  COALESCE(d.grupo, 999999) ASC, "
                + "  COALESCE(d.item, 999999) ASC, "
                + "  d.idFichaRiesgoDet ASC",
                FichaRiesgoDet.class
        ).setParameter("idFicha", idFicha)
                .getResultList();
    }
}
