package ec.gob.igm.rrhh.consultorio.service;



import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Stateless
public class Cie10Service {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

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

    public List<Cie10> buscarPorDescripcionLike(String query, int maxResults) {
        if (query == null) return Collections.emptyList();

        String q = query.trim();
        if (q.isEmpty()) return Collections.emptyList();

        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }

        int max = maxResults > 0 ? maxResults : 20;

        List<Cie10> lista = em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.descripcion) LIKE :query " +
                "ORDER BY c.descripcion",
                Cie10.class)
            .setParameter("query", "%" + q.toUpperCase() + "%")
            .setMaxResults(max)
            .getResultList();

        if (!lista.isEmpty()) {
            return lista;
        }

        // Fallback para catálogos con tildes: "COLERA" debe encontrar "CÓLERA".
        String from = "ÁÀÄÂÉÈËÊÍÌÏÎÓÒÖÔÚÙÜÛÑ";
        String to = "AAAAEEEEIIIIOOOOUUUUN";
        String qNorm = q.toUpperCase()
                .replace('Á', 'A').replace('À', 'A').replace('Ä', 'A').replace('Â', 'A')
                .replace('É', 'E').replace('È', 'E').replace('Ë', 'E').replace('Ê', 'E')
                .replace('Í', 'I').replace('Ì', 'I').replace('Ï', 'I').replace('Î', 'I')
                .replace('Ó', 'O').replace('Ò', 'O').replace('Ö', 'O').replace('Ô', 'O')
                .replace('Ú', 'U').replace('Ù', 'U').replace('Ü', 'U').replace('Û', 'U')
                .replace('Ñ', 'N');

        try {
            return em.createQuery(
                    "SELECT c FROM Cie10 c " +
                    "WHERE UPPER(FUNCTION('translate', c.descripcion, :from, :to)) LIKE :query " +
                    "ORDER BY c.descripcion",
                    Cie10.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("query", "%" + qNorm + "%")
                .setMaxResults(max)
                .getResultList();
        } catch (PersistenceException | IllegalArgumentException ex) {
            // Si la BD no soporta translate, devolver el resultado base (vacío).
            return lista;
        }
    }

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

    public List<Cie10> buscarPorTermino(String termino, int maxResults) {
        return buscarPorCodigoODescripcion(termino, maxResults);
    }

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

    public List<Cie10> buscarJerarquiaPorTerm(String termino) {
        if (termino == null) return Collections.emptyList();

        String norm = termino.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (norm.isEmpty()) return Collections.emptyList();

        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }

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
            // Fallback cuando la BD/JPA no soporta FUNCTION('replace', ...).
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

    public List<Cie10> buscarPorCodigoAproximado(String termino, int maxResults) {
        if (termino == null) return Collections.emptyList();

        String norm = normalizarCodigo(termino);
        if (norm.isEmpty()) return Collections.emptyList();

        if (em == null) {
            throw new IllegalStateException("EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'.");
        }

        int max = maxResults > 0 ? maxResults : 20;

        // Trae un conjunto acotado por la primera letra y filtra en memoria normalizado.
        List<Cie10> candidatos = em.createQuery(
                "SELECT c FROM Cie10 c " +
                "WHERE UPPER(c.codigo) LIKE :head " +
                "ORDER BY c.codigo",
                Cie10.class)
            .setParameter("head", norm.substring(0, 1) + "%")
            .setMaxResults(Math.max(200, max * 10))
            .getResultList();

        List<Cie10> out = new ArrayList<>();
        for (Cie10 c : candidatos) {
            if (c == null || c.getCodigo() == null) {
                continue;
            }

            String codNorm = normalizarCodigo(c.getCodigo());
            if (!codNorm.contains(norm)) {
                continue;
            }

            out.add(c);
            if (out.size() >= max) {
                break;
            }
        }

        return out;
    }

    private String normalizarCodigo(String codigo) {
        return codigo == null
                ? ""
                : codigo.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
    }
}
