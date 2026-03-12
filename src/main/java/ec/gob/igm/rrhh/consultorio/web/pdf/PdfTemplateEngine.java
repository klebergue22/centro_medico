package ec.gob.igm.rrhh.consultorio.web.pdf;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor simple para plantillas HTML tipo {{clave}}.
 * - Soporta bloques condicionales {{#if key}}...{{/if}}
 * - Precarga placeholders encontrados en la plantilla con ""
 * - Aplica reemplazos con escape HTML
 */
@ApplicationScoped
public class PdfTemplateEngine {

    private static final Pattern PH = Pattern.compile("\\{\\{([a-zA-Z0-9_]+)\\}\\}");
    private static final Pattern IF_BLOCK = Pattern.compile(
            "\\{\\{#if\\s+([a-zA-Z0-9_]+)\\}\\}([\\s\\S]*?)\\{\\{\\/if\\}\\}",
            Pattern.MULTILINE
    );

    public String render(String templateHtml, Map<String, String> rep) {

        if (templateHtml == null) {
            return "";
        }

        Map<String, String> local = new LinkedHashMap<>();
        if (rep != null && !rep.isEmpty()) {
            local.putAll(rep);
        }

        String html = applyIfBlocks(templateHtml, local);
        precargarPlaceholders(html, local);

        for (Map.Entry<String, String> e : local.entrySet()) {
            String key = e.getKey();
            if (key == null) {
                continue;
            }
            html = html.replace("{{" + key + "}}", escapeHtml(e.getValue()));
        }

        return html.replaceAll("\\{\\{[^}]+\\}\\}", "");
    }

    private String applyIfBlocks(String templateHtml, Map<String, String> rep) {
        Matcher matcher = IF_BLOCK.matcher(templateHtml);
        StringBuffer out = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            String body = matcher.group(2);
            String value = rep.getOrDefault(key, "");
            boolean enabled = isTrue(value);
            matcher.appendReplacement(out, Matcher.quoteReplacement(enabled ? body : ""));
        }

        matcher.appendTail(out);
        return out.toString();
    }

    private boolean isTrue(String value) {
        return "true".equalsIgnoreCase(value)
                || "1".equals(value)
                || "x".equalsIgnoreCase(value)
                || "si".equalsIgnoreCase(value);
    }

    private void precargarPlaceholders(String templateHtml, Map<String, String> rep) {

        Matcher m = PH.matcher(templateHtml);
        while (m.find()) {
            String key = m.group(1);
            if (key == null) {
                continue;
            }
            rep.putIfAbsent(key, "");
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
