package ec.gob.igm.rrhh.consultorio.web.facade;

/**
 *
 * @author GUERRA_KLEBER
 */
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfRenderer;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfSessionStore;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("centroMedicoPdfFacade")
@ViewScoped
public class CentroMedicoPdfFacade implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(CentroMedicoPdfFacade.class);

    private static final String TEMPLATE_PATH = "/resources/pdf/plantilla_ficha.html";
    private static final Pattern AMPERSAND_PATTERN = Pattern.compile("&(?![a-zA-Z]{2,8};|#\\d{2,5};|#x[0-9a-fA-F]{2,5};)");

    @Inject
    private PdfTemplateEngine pdfTemplateEngine;

    @Inject
    private PdfRenderer pdfRenderer;

    @Inject
    private PdfSessionStore pdfSessionStore;

    public String generarFichaPdf(Map<String, String> reemplazos) {
        return generarDesdePlantilla(TEMPLATE_PATH, reemplazos, "FICHA_");
    }

    public String generarCertificadoPdf(Map<String, String> reemplazos) {
        return generarDesdePlantilla(TEMPLATE_PATH, reemplazos, "CERT_");
    }

    public String generarDesdePlantilla(String templatePath, Map<String, String> reemplazos, String prefijoToken) {
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            if (ctx == null) {
                throw new IllegalStateException("No existe FacesContext activo para generar el PDF.");
            }

            String html = construirHtmlDesdePlantilla(ctx, templatePath, reemplazos);
            byte[] pdfBytes = pdfRenderer.render(html);

            return almacenarPdfEnSesion(ctx, pdfBytes, prefijoToken);
        } catch (Exception e) {
            LOG.error("Error al generar PDF desde plantilla. templatePath={}", templatePath, e);
            throw new RuntimeException("No fue posible generar el PDF.", e);
        }
    }

    public String generarDesdeHtml(String html, String prefijoToken) {
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            if (ctx == null) {
                throw new IllegalStateException("No existe FacesContext activo para generar el PDF.");
            }

            String xhtmlNormalizado = normalizarXhtml(html);
            byte[] pdfBytes = pdfRenderer.render(xhtmlNormalizado);

            return almacenarPdfEnSesion(ctx, pdfBytes, prefijoToken);
        } catch (Exception e) {
            LOG.error("Error al generar PDF desde HTML directo.", e);
            throw new RuntimeException("No fue posible generar el PDF.", e);
        }
    }

    private String almacenarPdfEnSesion(FacesContext ctx, byte[] pdfBytes, String prefijoToken) {
        String prefijo = (prefijoToken == null || prefijoToken.isBlank()) ? "PDF_" : prefijoToken;
        String token = prefijo + UUID.randomUUID().toString().replace("-", "");
        pdfSessionStore.store(ctx.getExternalContext(), token, pdfBytes);
        return token;
    }

    private String construirHtmlDesdePlantilla(FacesContext ctx, String templatePath, Map<String, String> reemplazos)
            throws IOException {

        String plantilla = leerRecursoComoString(ctx.getExternalContext(), templatePath);
        String html = pdfTemplateEngine.render(plantilla, reemplazos);
        return normalizarXhtml(html);
    }

    private String leerRecursoComoString(ExternalContext externalContext, String path) throws IOException {
        InputStream is = externalContext.getResourceAsStream(path);
        if (is == null) {
            throw new IOException("No se encontró el recurso: " + path);
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }

        return sb.toString();
    }

    private String normalizarXhtml(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }

        String xhtml = html.trim();

        xhtml = AMPERSAND_PATTERN.matcher(xhtml).replaceAll("&amp;");

        if (!xhtml.contains("xmlns=\"http://www.w3.org/1999/xhtml\"")) {
            xhtml = xhtml.replaceFirst("<html(\\s*?)>", "<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        }

        return xhtml;
    }
}