/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.igm.rrhh.consultorio.service;

/**
 *
 * @author GUERRA_KLEBER
 */


import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;

@Stateless
public class Cie10Service {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    /**
     * Busca por código:
     *  1) exacto (case-insensitive)
     *  2) normalizado sin puntos ni símbolos (ej: "T46.5" == "T465")
     */
    public Cie10 buscarPorCodigo(String codigo) {
        if (codigo == null) return null;

        String raw = codigo.trim().toUpperCase();
        if (raw.isEmpty()) return null;

        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }

        // 1) intento exacto
        List<Cie10> exacto = em.createQuery(
                "SELECT c FROM Cie10 c WHERE UPPER(c.codigo) = :codigo",
                Cie10.class)
            .setParameter("codigo", raw)
            .setMaxResults(1)
            .getResultList();

        if (!exacto.isEmpty()) {
            return exacto.get(0);
        }

        // 2) intento normalizado: quitar todo lo que no sea A-Z0-9
        String norm = raw.replaceAll("[^A-Z0-9]", "");
        if (norm.isEmpty()) return null;

        // IMPORTANTE: REPLACE directo puede romper en Hibernate 6 (Strict JPA).
        // Usamos FUNCTION('replace', ...) para que no explote.
        List<Cie10> normalizado = em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(FUNCTION('replace', c.codigo, '.', '')) = :norm " +
                "ORDER BY c.codigo",
                Cie10.class)
            .setParameter("norm", norm)
            .setMaxResults(1)
            .getResultList();

        return normalizado.isEmpty() ? null : normalizado.get(0);
    }

    /**
     * Buscar por descripción que contenga...
     */
    public List<Cie10> buscarPorDescripcionLike(String query, int maxResults) {
        if (query == null) return Collections.emptyList();

        String q = query.trim();
        if (q.isEmpty()) return Collections.emptyList();

        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }

        int max = maxResults > 0 ? maxResults : 20;

        return em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.descripcion) LIKE :query " +
                "ORDER BY c.descripcion",
                Cie10.class)
            .setParameter("query", "%" + q.toUpperCase() + "%")
            .setMaxResults(max)
            .getResultList();
    }

    /**
     * Buscar por código o descripción (LIKE).
     */
    public List<Cie10> buscarPorCodigoODescripcion(String termino, int maxResults) {
        if (termino == null) return Collections.emptyList();

        String t = termino.trim();
        if (t.isEmpty()) return Collections.emptyList();

        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }

        int max = maxResults > 0 ? maxResults : 20;
        String term = "%" + t.toUpperCase() + "%";

        return em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.codigo) LIKE :term OR UPPER(c.descripcion) LIKE :term " +
                "ORDER BY c.codigo",
                Cie10.class)
            .setParameter("term", term)
            .setMaxResults(max)
            .getResultList();
    }

    /**
     * Compatibilidad con llamadas existentes que usan el nombre genérico
     * buscarPorTermino(...).
     */
    public List<Cie10> buscarPorTermino(String termino, int maxResults) {
        return buscarPorCodigoODescripcion(termino, maxResults);
    }

    /**
     * Buscar primero por descripción exacta (case-insensitive).
     */
    public Cie10 buscarPrimeroPorDescripcion(String descripcion) {
        if (descripcion == null) return null;

        String d = descripcion.trim();
        if (d.isEmpty()) return null;

        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }

        List<Cie10> lista = em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.descripcion) = :d " +
                "ORDER BY c.codigo",
                Cie10.class)
            .setParameter("d", d.toUpperCase())
            .setMaxResults(1)
            .getResultList();

        return lista.isEmpty() ? null : lista.get(0);
    }

    /**
     * Búsqueda jerárquica (autocomplete por prefijo), ignorando puntos:
     * term "T46.5" -> "T465%" sobre replace(codigo,'.','')
     */
    public List<Cie10> buscarJerarquiaPorTerm(String termino) {
        if (termino == null) return Collections.emptyList();

        String norm = termino.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (norm.isEmpty()) return Collections.emptyList();

        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }

        return em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(FUNCTION('replace', c.codigo, '.', '')) LIKE :term " +
                "ORDER BY c.codigo",
                Cie10.class)
            .setParameter("term", norm + "%")
            .setMaxResults(20)
            .getResultList();
    }
}
