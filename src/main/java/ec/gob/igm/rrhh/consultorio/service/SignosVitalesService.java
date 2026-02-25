/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.igm.rrhh.consultorio.service;

/**
 *
 * @author GUERRA_KLEBER
 */

import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;

@Stateless
public class SignosVitalesService implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * Guarda o actualiza un registro de signos vitales.
     * - Si idSignos es null -> persist
     * - Si idSignos no es null -> merge
     */
    public SignosVitales guardar(SignosVitales signos) {
        assertEm();

        if (signos == null) {
            return null;
        }

        if (signos.getIdSignos() == null) {
            em.persist(signos);
            // em.flush(); // útil en debug para errores de BD
            return signos;
        }

        SignosVitales merged = em.merge(signos);
        // em.flush(); // útil en debug
        return merged;
    }

    /**
     * Buscar signos vitales por ID.
     */
    public SignosVitales buscarPorId(Long id) {
        assertEm();

        if (id == null) {
            return null;
        }
        return em.find(SignosVitales.class, id);
    }
}

