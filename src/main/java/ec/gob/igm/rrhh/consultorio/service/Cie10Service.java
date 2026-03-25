package ec.gob.igm.rrhh.consultorio.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;

import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;

@Stateless
/**
 * Class Cie10Service: encapsula reglas de negocio y acceso a datos del dominio.
 */
public class Cie10Service {

    private static final String ACCENTED_CHARS = "\u00C1\u00C0\u00C4\u00C2\u00C9\u00C8\u00CB\u00CA\u00CD\u00CC\u00CF\u00CE\u00D3\u00D2\u00D6\u00D4\u00DA\u00D9\u00DC\u00DB\u00D1";
    private static final String PLAIN_CHARS = "AAAAEEEEIIIIOOOOUUUUN";

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    public Cie10 buscarPorCodigo(String codigo) {
        String raw = trimUpperOrNull(codigo);
        if (raw == null) return null;

        ensureEntityManager();
        Cie10 exacto = findFirstByCodigo(raw);
        if (exacto != null) {
            return exacto;
        }

        String norm = normalizarCodigo(raw);
        return norm.isEmpty() ? null : findFirstByNormalizedCodigo(norm);
    }

    public List<Cie10> buscarPorDescripcionLike(String query, int maxResults) {
        String q = trimToNull(query);
        if (q == null) return Collections.emptyList();

        ensureEntityManager();
        int max = resolveMaxResults(maxResults);
        List<Cie10> lista = findByDescripcionLike(q.toUpperCase(), max);
        if (!lista.isEmpty()) {
            return lista;
        }
        return findByDescripcionLikeWithoutAccents(q, max, lista);
    }

    public List<Cie10> buscarPorCodigoODescripcion(String termino, int maxResults) {
        if (termino == null) return Collections.emptyList();

        String t = termino.trim();
        if (t.isEmpty()) return Collections.emptyList();

        ensureEntityManager();
        int max = resolveMaxResults(maxResults);
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

    public List<Cie10> buscarPorTermino(String termino, int maxResults) {
        return buscarPorCodigoODescripcion(termino, maxResults);
    }

    public Cie10 buscarPrimeroPorDescripcion(String descripcion) {
        if (descripcion == null) return null;

        String d = descripcion.trim();
        if (d.isEmpty()) return null;

        ensureEntityManager();
        List<Cie10> lista = em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.descripcion) = :d " +
                "ORDER BY c.codigo",
                Cie10.class)
            .setParameter("d", d.toUpperCase())
            .setMaxResults(1)
            .getResultList();

        return firstOrNull(lista);
    }

    public List<Cie10> buscarJerarquiaPorTerm(String termino) {
        String norm = normalizarCodigo(termino);
        if (norm.isEmpty()) return Collections.emptyList();

        ensureEntityManager();
        return findJerarquiaByNormalizedTerm(norm);
    }

    public List<Cie10> buscarPorCodigoAproximado(String termino, int maxResults) {
        String norm = normalizarCodigo(termino);
        if (norm.isEmpty()) return Collections.emptyList();

        ensureEntityManager();
        int max = resolveMaxResults(maxResults);
        return filterCodigoCandidates(loadCodigoCandidates(norm, max), norm, max);
    }

    private void ensureEntityManager() {
        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }
    }

    private int resolveMaxResults(int maxResults) {
        return maxResults > 0 ? maxResults : 20;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimUpperOrNull(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toUpperCase();
    }

    private Cie10 findFirstByCodigo(String codigo) {
        return firstOrNull(em.createQuery(
                "SELECT c FROM Cie10 c WHERE UPPER(c.codigo) = :codigo",
                Cie10.class)
            .setParameter("codigo", codigo)
            .setMaxResults(1)
            .getResultList());
    }

    private Cie10 findFirstByNormalizedCodigo(String norm) {
        return firstOrNull(em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(FUNCTION('replace', c.codigo, '.', '')) = :norm " +
                "ORDER BY c.codigo",
                Cie10.class)
            .setParameter("norm", norm)
            .setMaxResults(1)
            .getResultList());
    }

    private List<Cie10> findByDescripcionLike(String queryUpper, int max) {
        return em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.descripcion) LIKE :query " +
                "ORDER BY c.descripcion",
                Cie10.class)
            .setParameter("query", "%" + queryUpper + "%")
            .setMaxResults(max)
            .getResultList();
    }

    private List<Cie10> findByDescripcionLikeWithoutAccents(String query, int max, List<Cie10> fallback) {
        try {
            return em.createQuery(
                    "SELECT c FROM Cie10 c " +
                    "WHERE UPPER(FUNCTION('translate', c.descripcion, :from, :to)) LIKE :query " +
                    "ORDER BY c.descripcion",
                    Cie10.class)
                .setParameter("from", ACCENTED_CHARS)
                .setParameter("to", PLAIN_CHARS)
                .setParameter("query", "%" + replaceSupportedAccents(query) + "%")
                .setMaxResults(max)
                .getResultList();
        } catch (PersistenceException | IllegalArgumentException ex) {
            return fallback;
        }
    }

    private String replaceSupportedAccents(String value) {
        String normalized = value.toUpperCase();
        for (int i = 0; i < ACCENTED_CHARS.length(); i++) {
            normalized = normalized.replace(ACCENTED_CHARS.charAt(i), PLAIN_CHARS.charAt(i));
        }
        return normalized;
    }

    private List<Cie10> findJerarquiaByNormalizedTerm(String norm) {
        try {
            return em.createQuery(
                    "SELECT c FROM Cie10 c " +
                    "WHERE UPPER(FUNCTION('replace', c.codigo, '.', '')) LIKE :term " +
                    "ORDER BY c.codigo",
                    Cie10.class)
                .setParameter("term", norm + "%")
                .setMaxResults(20)
                .getResultList();
        } catch (PersistenceException | IllegalArgumentException ex) {
            return em.createQuery(
                    "SELECT c FROM Cie10 c " +
                    "WHERE UPPER(c.codigo) LIKE :term " +
                    "ORDER BY c.codigo",
                    Cie10.class)
                .setParameter("term", norm + "%")
                .setMaxResults(20)
                .getResultList();
        }
    }

    private List<Cie10> loadCodigoCandidates(String norm, int max) {
        return em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.codigo) LIKE :head " +
                "ORDER BY c.codigo",
                Cie10.class)
            .setParameter("head", norm.substring(0, 1) + "%")
            .setMaxResults(Math.max(200, max * 10))
            .getResultList();
    }

    private List<Cie10> filterCodigoCandidates(List<Cie10> candidatos, String norm, int max) {
        List<Cie10> out = new ArrayList<>();
        for (Cie10 candidato : candidatos) {
            if (!matchesNormalizedCodigo(candidato, norm)) {
                continue;
            }
            out.add(candidato);
            if (out.size() >= max) {
                break;
            }
        }
        return out;
    }

    private boolean matchesNormalizedCodigo(Cie10 candidato, String norm) {
        if (candidato == null || candidato.getCodigo() == null) {
            return false;
        }
        return normalizarCodigo(candidato.getCodigo()).contains(norm);
    }

    private String normalizarCodigo(String codigo) {
        return codigo == null
                ? ""
                : codigo.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
    }

    private Cie10 firstOrNull(List<Cie10> lista) {
        return lista.isEmpty() ? null : lista.get(0);
    }
}
