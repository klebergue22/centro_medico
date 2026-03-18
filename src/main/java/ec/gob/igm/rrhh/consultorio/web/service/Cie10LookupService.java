package ec.gob.igm.rrhh.consultorio.web.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import ec.gob.igm.rrhh.consultorio.domain.model.Cie10;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.service.Cie10Service;

@Stateless
/**
 * Class Cie10LookupService: orquesta la lógica de presentación y flujo web.
 */
public class Cie10LookupService {

    @EJB
    private Cie10Service cie10Service;

    public void completarDiagnosticoPorCodigo(ConsultaDiagnostico diagnostico) {
        if (diagnostico == null) {
            return;
        }

        String codigo = diagnostico.getCodigo();
        if (codigo == null || codigo.trim().isEmpty()) {
            diagnostico.setDescripcion(null);
            diagnostico.setCie10(null);
            return;
        }

        Cie10 cie = cie10Service.buscarPorCodigo(codigo.trim());
        if (cie != null) {
            diagnostico.setDescripcion(cie.getDescripcion());
            diagnostico.setCie10(cie);
            return;
        }

        diagnostico.setDescripcion(null);
        diagnostico.setCie10(null);
    }

    public void sincronizarFilaSeleccionada(ConsultaDiagnostico diagnostico) {
        if (diagnostico == null || diagnostico.getCie10() == null) {
            return;
        }

        diagnostico.setCodigo(diagnostico.getCie10().getCodigo());
        diagnostico.setDescripcion(diagnostico.getCie10().getDescripcion());
    }

    public List<Cie10> completarPorCodigoODescripcion(String query, int max) {
        return cie10Service.buscarPorCodigoODescripcion(query, max);
    }

    public List<Cie10> completarPorCodigo(String query) {
        if (query == null) {
            return new ArrayList<>();
        }

        String q = query.trim().toUpperCase();
        if (q.isEmpty()) {
            return new ArrayList<>();
        }

        List<Cie10> lista = cie10Service.buscarJerarquiaPorTerm(q);
        List<Cie10> out = new ArrayList<>();

        for (Cie10 c : lista) {
            if (c != null && c.getCodigo() != null && c.getCodigo().toUpperCase().startsWith(q)) {
                out.add(c);
            }
        }

        return out;
    }

    public List<Cie10> completarPorDescripcion(String query, int max) {
        if (query == null) {
            return new ArrayList<>();
        }

        String q = query.trim();
        if (q.isEmpty()) {
            return new ArrayList<>();
        }

        return cie10Service.buscarPorDescripcionLike(q, max);
    }

    public String buscarDescripcionPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }

        Cie10 cie = cie10Service.buscarPorCodigo(codigo.trim());
        return cie != null ? cie.getDescripcion() : null;
    }

    public String buscarCodigoPorDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return null;
        }

        Cie10 cie = cie10Service.buscarPrimeroPorDescripcion(descripcion.trim());
        return cie != null ? cie.getCodigo() : null;
    }

    public Cie10 inferirPrincipalDesdeLista(List<ConsultaDiagnostico> listaDiag) {
        if (listaDiag == null || listaDiag.isEmpty()) {
            return null;
        }

        ConsultaDiagnostico best = null;

        for (ConsultaDiagnostico r : listaDiag) {
            if (r == null) {
                continue;
            }
            String cod = r.getCodigo() != null ? r.getCodigo().trim() : "";
            if (cod.isEmpty()) {
                continue;
            }

            if ("D".equals(r.getTipoDiag())) {
                best = r;
                break;
            }
            if (best == null) {
                best = r;
            }
        }

        if (best == null || best.getCodigo() == null || best.getCodigo().trim().isEmpty()) {
            return null;
        }

        return cie10Service.buscarPorCodigo(best.getCodigo().trim());
    }

    public Cie10 buscarMejorCoincidenciaPorDescripcion(List<Cie10> lista, String input) {
        if (lista == null || lista.isEmpty() || input == null) {
            return null;
        }

        final String needle = normalizar(input);

        Cie10 best = null;
        int bestScore = Integer.MAX_VALUE;
        int bestLen = Integer.MAX_VALUE;

        for (Cie10 c : lista) {
            if (c == null || c.getDescripcion() == null) {
                continue;
            }

            String cand = normalizar(c.getDescripcion());
            int score;

            if (cand.equals(needle)) {
                score = 0;
            } else if (cand.startsWith(needle)) {
                score = 1;
            } else if (cand.contains(needle)) {
                score = 2;
            } else {
                score = 9;
            }
            int len = cand.length();

            if (best == null || score < bestScore || (score == bestScore && len < bestLen)) {
                best = c;
                bestScore = score;
                bestLen = len;
            }
        }

        return bestScore >= 9 ? null : best;
    }


    public List<Cie10> completarFilaCie10PorCodigo(String query, int max) {
        int limite = max > 0 ? max : 20;
        String qRaw = limpiarTexto(query);
        if (qRaw.isEmpty()) {
            return new ArrayList<>();
        }

        String qCodigo = normalizarCodigo(qRaw);
        List<Cie10> out = new ArrayList<>();
        Set<String> codigosAgregados = new LinkedHashSet<>();

        agregarCoincidenciasPorCodigo(out, codigosAgregados, cie10Service.buscarJerarquiaPorTerm(qRaw), qCodigo, limite);
        agregarCoincidenciasPorCodigo(out, codigosAgregados, cie10Service.buscarPorCodigoAproximado(qRaw, limite), qCodigo, limite);

        if (out.size() < limite) {
            agregarCoincidenciasPorCodigo(out, codigosAgregados, cie10Service.buscarPorTermino(qRaw, limite), qCodigo, limite);
        }

        return out;
    }

    public List<Cie10> completarFilaCie10PorDescripcion(String query, int max) {
        int limite = max > 0 ? max : 20;
        String q = limpiarTexto(query);
        if (q.isEmpty()) {
            return new ArrayList<>();
        }

        List<Cie10> lista = cie10Service.buscarPorDescripcionLike(q, limite);
        List<Cie10> out = new ArrayList<>();
        Set<String> claves = new LinkedHashSet<>();

        if (lista != null) {
            for (Cie10 cie10 : lista) {
                if (cie10 == null || cie10.getCodigo() == null) {
                    continue;
                }

                String codigo = cie10.getCodigo().trim();
                if (codigo.isEmpty() || !claves.add(codigo)) {
                    continue;
                }

                out.add(cie10);
                if (out.size() >= limite) {
                    break;
                }
            }
        }
        return out;
    }

    public List<String> completarFilaPorCodigo(String query) {
        String qRaw = limpiarTexto(query);
        if (qRaw.isEmpty()) {
            return new ArrayList<>();
        }

        String qCodigo = normalizarCodigo(qRaw);
        int limite = 20;

        List<Cie10> coincidencias = new ArrayList<>();
        Set<String> codigosAgregados = new LinkedHashSet<>();

        // Priorizar coincidencias por código para que si el usuario escribe "K" vea primero K00, K01, K21, etc.
        agregarCoincidenciasPorCodigo(coincidencias, codigosAgregados, cie10Service.buscarJerarquiaPorTerm(qRaw), qCodigo, limite);
        agregarCoincidenciasPorCodigo(coincidencias, codigosAgregados, cie10Service.buscarPorCodigoAproximado(qRaw, limite), qCodigo, limite);

        // Completar el panel con resultados combinados por código o descripción.
        if (coincidencias.size() < limite) {
            agregarCoincidencias(coincidencias, codigosAgregados, cie10Service.buscarPorCodigoODescripcion(qRaw, limite), limite);
        }

        // Refuerzo extra para entradas que solo aparecen en descripción o por tildes/variantes.
        if (coincidencias.size() < limite) {
            agregarCoincidencias(coincidencias, codigosAgregados, cie10Service.buscarPorDescripcionLike(qRaw, limite), limite);
        }

        List<String> out = new ArrayList<>();
        for (Cie10 cie10 : coincidencias) {
            out.add(formatearSugerenciaCodigo(cie10));
        }
        return out;
    }


    public String extraerCodigoDeSugerencia(String value) {
        String limpio = limpiarTexto(value);
        if (limpio.isEmpty()) {
            return limpio;
        }
        return out;
    }

        int sep = limpio.indexOf(" - ");
        String codigo = sep >= 0 ? limpio.substring(0, sep) : limpio;
        return codigo.trim().toUpperCase();
    }

    private String formatearSugerenciaCodigo(Cie10 cie10) {
        if (cie10 == null || cie10.getCodigo() == null) {
            return "";
        }

        String codigo = cie10.getCodigo().trim();
        String descripcion = cie10.getDescripcion() != null ? cie10.getDescripcion().trim() : "";
        if (descripcion.isEmpty()) {
            return codigo;
        }
        return codigo + " - " + descripcion;
    }

    public List<String> completarFilaPorDescripcion(String query, int max) {
        int limite = max > 0 ? max : 20;
        String q = limpiarTexto(query);
        if (q.isEmpty()) {
            return new ArrayList<>();
        }

        List<Cie10> lista = cie10Service.buscarPorDescripcionLike(q, limite);
        Set<String> descripciones = new LinkedHashSet<>();
        if (lista != null) {
            for (Cie10 c : lista) {
                if (c != null && c.getDescripcion() != null) {
                    String descripcion = c.getDescripcion().trim();
                    if (!descripcion.isEmpty()) {
                        descripciones.add(descripcion);
                    }
                }
            }
        }

        return limitarResultados(descripciones, limite);
    }


    private void agregarCoincidencias(List<Cie10> out, Set<String> codigosAgregados, List<Cie10> lista, int limite) {
        if (out == null || codigosAgregados == null || lista == null || limite <= 0) {
            return;
        }

        for (Cie10 cie10 : lista) {
            if (cie10 == null || cie10.getCodigo() == null) {
                continue;
            }

            String codigo = cie10.getCodigo().trim();
            if (codigo.isEmpty() || !codigosAgregados.add(codigo)) {
                continue;
            }

            out.add(cie10);
            if (out.size() >= limite) {
                return;
            }
        }
    }

    private void agregarCoincidenciasPorCodigo(List<Cie10> out, Set<String> codigosAgregados, List<Cie10> lista, String qCodigo, int limite) {
        if (out == null || codigosAgregados == null || lista == null || limite <= 0) {
            return;
        }

        for (Cie10 cie10 : lista) {
            if (cie10 == null || cie10.getCodigo() == null) {
                continue;
            }

            String codigo = cie10.getCodigo().trim();
            if (codigo.isEmpty()) {
                continue;
            }

            if (!qCodigo.isEmpty() && !normalizarCodigo(codigo).contains(qCodigo)) {
                continue;
            }

            if (!codigosAgregados.add(codigo)) {
                continue;
            }

            out.add(cie10);
            if (out.size() >= limite) {
                return;
            }
        }
    }

    private void agregarCodigos(Set<String> out, List<Cie10> lista) {
        if (lista == null || out == null) {
            return;
        }

        for (Cie10 c : lista) {
            if (c == null || c.getCodigo() == null) {
                continue;
            }

            String codigo = c.getCodigo().trim();
            if (codigo.isEmpty()) {
                continue;
            }

            out.add(codigo);
            if (out.size() >= 100) {
                return;
            }
        }
    }

    private List<String> limitarResultados(Set<String> items, int limite) {
        List<String> out = new ArrayList<>();
        if (items == null || items.isEmpty() || limite <= 0) {
            return out;
        }

        for (String item : items) {
            out.add(item);
            if (out.size() >= limite) {
                break;
            }
        }

        return out;
    }

    private String limpiarTexto(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizarCodigo(String codigo) {
        return limpiarTexto(codigo).toUpperCase().replaceAll("[^A-Z0-9]", "");
    }

    private String normalizar(String value) {
        return value.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
