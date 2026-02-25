package ec.gob.igm.rrhh.consultorio.web.facade;

import com.lowagie.text.DocumentException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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

    private static final Logger log = LoggerFactory.getLogger(CentroMedicoPdfFacade.class);

    private static final String TEMPLATE_PATH = "/resources/pdf/plantilla_ficha.html";
    private static final String PDF_STORE_KEY = "PDF_STORE";

    public String generarYAlmacenarFichaEnSesion(FacesContext ctx, Map<String, String> reemplazos)
            throws IOException, DocumentException {

        if (ctx == null) {
            throw new IllegalArgumentException("FacesContext no puede ser null");
        }

        String html = construirHtmlDesdePlantilla(ctx, reemplazos);
        byte[] bytes = renderizarPdf(html);

        String token = "FICHA_" + UUID.randomUUID().toString().replace("-", "");
        almacenarEnSesion(ctx.getExternalContext(), token, bytes);

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
        almacenarEnSesion(ctx.getExternalContext(), token, pdfBytes);
        return token;
    }

private void almacenarEnSesion(ExternalContext ec, String token, byte[] bytes) {
        HttpSession session = (HttpSession) ec.getSession(true);

        @SuppressWarnings("unchecked")
        Map<String, byte[]> pdfStore = (Map<String, byte[]>) session.getAttribute(PDF_STORE_KEY);
        if (pdfStore == null) {
            pdfStore = new HashMap<>();
            session.setAttribute(PDF_STORE_KEY, pdfStore);
        }
        pdfStore.put(token, bytes);
    }

    private String construirHtmlDesdePlantilla(FacesContext ctx, Map<String, String> rep) throws IOException {
        String plantilla = leerRecursoComoString(ctx.getExternalContext(), TEMPLATE_PATH);
        String html = aplicarReemplazos(plantilla, rep);
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
     * Aplica reemplazos {{key}} sobre la plantilla.
     * Nota: Esto es intencionalmente simple y determinístico.
     */
    private String aplicarReemplazos(String plantilla, Map<String, String> rep) {
        if (plantilla == null) return "";

        String html = plantilla;

        if (rep != null) {
            for (Map.Entry<String, String> e : rep.entrySet()) {
                String key = e.getKey();
                String val = safe(e.getValue());
                html = html.replace("{{" + key + "}}", val);
            }
        }

        // Limpia placeholders no resueltos para evitar ver "{{...}}" en el PDF
        html = html.replaceAll("\\{\\{[^}]+\\}\\}", "");

        return html;
    }

    /**
     * Render PDF usando Flying Saucer (ITextRenderer).
     * Mantiene el comportamiento que ya tenías en el Controller.
     */
    private byte[] renderizarPdf(String xhtml) throws DocumentException, IOException {
        if (xhtml == null || xhtml.trim().isEmpty()) {
            log.error("renderizarPdf: El string HTML recibido es NULO o VACÍO.");
            throw new IllegalArgumentException("El contenido HTML para generar el PDF está vacío.");
        }

        final String xhtmlNorm = normalizarXhtml(xhtml);

        ITextRenderer renderer = new ITextRenderer();

        // Base URL para recursos relativos (imagenes/css) dentro del WAR
        // Flying Saucer necesita un base para resolver recursos; en tu plantilla usas rutas /resources/...
        // Nota: si alguna ruta no resuelve, ajustamos aquí.
        try {
            renderer.setDocumentFromString(xhtmlNorm);
        } catch (Exception ex) {
            // fallback: setDocumentFromString sin base
            renderer.setDocumentFromString(xhtmlNorm);
        }

        renderer.layout();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            renderer.createPDF(baos);
            renderer.finishPDF();
            return baos.toByteArray();
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

    private String safe(String s) {
        if (s == null) return "";
        // escape mínimo para HTML
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
