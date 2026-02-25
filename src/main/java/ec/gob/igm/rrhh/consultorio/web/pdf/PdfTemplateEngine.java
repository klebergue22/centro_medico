package ec.gob.igm.rrhh.consultorio.web.pdf;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Motor simple para plantillas HTML tipo {{clave}}.
 * - Precarga placeholders encontrados en la plantilla con "" para evitar que se impriman literales {{...}}
 * - Aplica reemplazos usando el mismo criterio que el controlador (null => "")
 *
 * Nota: NO interpreta {{#if}} / {{/if}} (eso se mantiene en el controlador por ahora).
 */
@ApplicationScoped
public class PdfTemplateEngine {

    // Captura {{clave}} donde clave solo contiene letras/números/underscore.
    // No captura {{#if ...}} ni {{/if}}.
    private static final Pattern PH = Pattern.compile("\\{\\{([a-zA-Z0-9_]+)\\}\\}");

    public String render(String templateHtml, Map<String, String> rep) {

        if (templateHtml == null) {
            return "";
        }

        // Copia para no mutar el map de entrada
        Map<String, String> local = new LinkedHashMap<>();
        if (rep != null && !rep.isEmpty()) {
            local.putAll(rep);
        }

        // 1) Precarga placeholders ausentes => ""
        precargarPlaceholders(templateHtml, local);

        // 2) Reemplaza
        String html = templateHtml;
        for (Map.Entry<String, String> e : local.entrySet()) {
            String key = e.getKey();
            if (key == null) {
                continue;
            }
            String val = e.getValue() == null ? "" : e.getValue();
            html = html.replace("{{" + key + "}}", val);
        }

        return html;
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
}
