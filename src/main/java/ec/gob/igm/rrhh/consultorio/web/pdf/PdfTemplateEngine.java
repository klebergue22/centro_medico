package ec.gob.igm.rrhh.consultorio.web.pdf;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor para plantillas HTML tipo {{clave}} y bloques condicionales {{#if clave}} ... {{/if}}.
 * - Precarga placeholders encontrados en la plantilla con "" para evitar que se impriman literales {{...}}
 * - Aplica reemplazos usando el mismo criterio que el controlador (null => "")
 * - Limpia placeholders no resueltos al final
 */
@ApplicationScoped
public class PdfTemplateEngine {

    // Captura {{clave}} donde clave solo contiene letras/números/underscore.
    // No captura {{#if ...}} ni {{/if}}.
    private static final Pattern PH = Pattern.compile("\\{\\{([a-zA-Z0-9_]+)\\}\\}");
    private static final Pattern IF_BLOCK = Pattern.compile(
            "\\{\\{#if\\s+([a-zA-Z0-9_]+)\\}\\}([\\s\\S]*?)\\{\\{\\/if\\}\\}",
            Pattern.MULTILINE
    );

    public String render(String templateHtml, Map<String, String> rep) {

        if (templateHtml == null) {
            return "";
        }

        // Copia para no mutar el map de entrada
        Map<String, String> local = new LinkedHashMap<>();
        if (rep != null && !rep.isEmpty()) {
            local.putAll(rep);
        }

        // 1) Evalúa bloques condicionales
        String html = applyIfBlocks(templateHtml, local);

        // 2) Precarga placeholders ausentes => ""
        precargarPlaceholders(html, local);

        // 3) Reemplaza
        for (Map.Entry<String, String> e : local.entrySet()) {
            String key = e.getKey();
            if (key == null) {
                continue;
            }
            String val = escapeHtml(e.getValue() == null ? "" : e.getValue());
            html = html.replace("{{" + key + "}}", val);
        }

        // 4) Limpia placeholders remanentes si quedaron por alguna razón
        return html.replaceAll("\\{\\{[^}]+\\}\\}", "");
    }

    private String applyIfBlocks(String templateHtml, Map<String, String> rep) {
        Matcher matcher = IF_BLOCK.matcher(templateHtml);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String body = matcher.group(2);
            String value = rep.getOrDefault(key, "");

            boolean enabled = "true".equalsIgnoreCase(value)
                    || "1".equals(value)
                    || "x".equalsIgnoreCase(value)
                    || "si".equalsIgnoreCase(value);

            matcher.appendReplacement(out, Matcher.quoteReplacement(enabled ? body : ""));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private void precargarPlaceholders(String templateHtml, Map<String, String> rep) {

        Matcher m = PH.matcher(templateHtml);
        while (m.find()) {
            String key = m.group(1);
            if (key == null) {
                continue;
            }
            // si no existe en el map, deja vacío
            rep.putIfAbsent(key, "");
        }
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
