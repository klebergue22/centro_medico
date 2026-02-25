package ec.gob.igm.rrhh.consultorio.web.facade;

import com.lowagie.text.DocumentException;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfRenderer;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfSessionStore;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * Facade responsable de:
 * - cargar plantilla HTML (resources/pdf/plantilla_ficha.html)
 * - aplicar reemplazos {{...}}
 * - renderizar PDF
 * - almacenar el PDF en sesión (PDF_STORE) y devolver token
 *
 * Mantiene al Controller delgado (orquestación/UI).
 */
@ApplicationScoped
public class CentroMedicoPdfFacade {

    private static final String TEMPLATE_PATH = "/resources/pdf/plantilla_ficha.html";

    @Inject
    private PdfTemplateEngine pdfTemplateEngine;

    @Inject
    private PdfRenderer pdfRenderer;

    @Inject
    private PdfSessionStore pdfSessionStore;

    public String generarYAlmacenarFichaEnSesion(FacesContext ctx, Map<String, String> reemplazos)
            throws IOException, DocumentException {

        if (ctx == null) {
            throw new IllegalArgumentException("FacesContext no puede ser null");
        }

        String html = construirHtmlDesdePlantilla(ctx, reemplazos);
        byte[] bytes = pdfRenderer.render(html);

        String token = "FICHA_" + UUID.randomUUID().toString().replace("-", "");
        pdfSessionStore.store(ctx.getExternalContext(), token, bytes);

        return token;
    }

    
    /**
     * Almacena directamente bytes PDF en sesión y retorna token.
     * Útil cuando el Controller ya generó el PDF y solo requiere storage.
     */
    public String storePdfInSession(FacesContext ctx, String prefix, byte[] pdfBytes) {
        if (ctx == null) {
            throw new IllegalArgumentException("FacesContext no puede ser null");
        }
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IllegalArgumentException("pdfBytes no puede ser null/vacío");
        }
        final String p = (prefix == null || prefix.isBlank()) ? "PDF_" : prefix;
        final String token = p + UUID.randomUUID().toString().replace("-", "");
        pdfSessionStore.store(ctx.getExternalContext(), token, pdfBytes);
        return token;
    }

    private String construirHtmlDesdePlantilla(FacesContext ctx, Map<String, String> rep) throws IOException {
        String plantilla = leerRecursoComoString(ctx.getExternalContext(), TEMPLATE_PATH);
        String html = pdfTemplateEngine.render(plantilla, rep);
        return normalizarXhtml(html);
    }

    private String leerRecursoComoString(ExternalContext ec, String path) throws IOException {
        try (InputStream is = ec.getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("No se encontró la plantilla: " + path);
            }
            byte[] data = is.readAllBytes();
            return new String(data, StandardCharsets.UTF_8);
        }
    }

    /**
     * Normaliza XHTML para Flying Saucer:
     * - asegura xmlns
     * - corrige & sueltos
     * - etc.
     *
     * Deja esta función pequeña; si ya tienes una implementación más completa,
     * la migramos aquí también.
     */
    private String normalizarXhtml(String html) {
        if (html == null) return "";

        String x = html;

        // Evita entidades '&' sin escapar (causaban: "The entity name must immediately follow the '&'")
        x = x.replace("& ", "&amp; ");

        return x;
    }

}
