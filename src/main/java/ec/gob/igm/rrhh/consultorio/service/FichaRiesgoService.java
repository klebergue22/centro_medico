/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.igm.rrhh.consultorio.service;

/**
 *
 * @author GUERRA_KLEBER
 */


import ec.gob.igm.rrhh.consultorio.domain.model.FichaRiesgo;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;

@Stateless
public class FichaRiesgoService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    private void assertEm() {
        if (em == null) {
            throw new IllegalStateException(
                "EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'."
            );
        }
    }

    /**
     * Crear o actualizar una ficha de riesgos.
     */
    public FichaRiesgo guardar(FichaRiesgo fr) {
        assertEm();

        if (fr == null) {
            return null;
        }

        if (fr.getIdFichaRiesgo() == null) {
            em.persist(fr);
            // em.flush(); // útil en debug para detectar errores de BD aquí
            return fr;
        }

        FichaRiesgo merged = em.merge(fr);
        // em.flush(); // útil en debug
        return merged;
    }

    /**
     * Buscar por ID.
     */
    public FichaRiesgo buscarPorId(Long id) {
        assertEm();

        if (id == null) {
            return null;
        }
        return em.find(FichaRiesgo.class, id);
    }

    /**
     * Buscar riesgos por ID_FICHA.
     * Nota: devuelve lista vacía si idFicha es null o no hay resultados.
     */
    public List<FichaRiesgo> buscarPorFicha(Long idFicha) {
        assertEm();

        if (idFicha == null) {
            return Collections.emptyList();
        }

        return em.createQuery(
                "SELECT r FROM FichaRiesgo r " +
                "WHERE r.ficha.idFicha = :idFicha " +
                "ORDER BY r.idFichaRiesgo",
                FichaRiesgo.class)
            .setParameter("idFicha", idFicha)
            .getResultList();
    }
}
