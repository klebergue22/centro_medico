package ec.gob.igm.rrhh.consultorio.web.facade;

/**
 *
 * @author GUERRA_KLEBER
 */
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfRenderer;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfSessionStore;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfTemplateEngine;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("centroMedicoPdfFacade")
@ViewScoped
/**
 * Class CentroMedicoPdfFacade: expone una fachada para simplificar operaciones del modulo web.
 */
public class CentroMedicoPdfFacade implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(CentroMedicoPdfFacade.class);

    private static final String TEMPLATE_PATH = "/resources/pdf/plantilla_ficha.html";
    private static final Pattern AMPERSAND_PATTERN = Pattern.compile("&(?![a-zA-Z]{2,8};|#\\d{2,5};|#x[0-9a-fA-F]{2,5};)");

    public static class PdfPreviewResult implements Serializable {

        private static final long serialVersionUID = 1L;

        private final boolean listo;
        private final String token;

        public PdfPreviewResult(boolean listo, String token) {
            this.listo = listo;
            this.token = token;
        }

        public boolean isListo() {
            return listo;
        }

        public String getToken() {
            return token;
        }
    }

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

    public PdfPreviewResult prepararPreviewDesdeHtml(Supplier<Boolean> verificador,
            Supplier<String> htmlSupplier,
            String prefijoToken) {
        if (verificador != null && !Boolean.TRUE.equals(verificador.get())) {
            return new PdfPreviewResult(false, null);
        }
        String token = generarDesdeHtml(htmlSupplier.get(), prefijoToken);
        return new PdfPreviewResult(token != null, token);
    }

    public String aplicarReemplazos(String template, Map<String, String> rep) {
        String html = template;
        for (Map.Entry<String, String> e : rep.entrySet()) {
            html = html.replace("{{" + e.getKey() + "}}", e.getValue() == null ? "" : e.getValue());
        }
        return html;
    }

    public String aplicarBloquesSexo(String html, String sexo) {
        String sx = (sexo == null) ? "" : sexo.trim().toUpperCase();

        boolean esM = "M".equals(sx) || "MASCULINO".equals(sx) || "H".equals(sx) || "HOMBRE".equals(sx);
        boolean esF = "F".equals(sx) || "FEMENINO".equals(sx) || "MUJER".equals(sx);

        html = aplicarBloqueCondicional(html, "sexoM", esM);
        html = aplicarBloqueCondicional(html, "sexoF", esF);

        return html;
    }

    public String aplicarBloquesTipoEvaluacion(String html, String tipoEvaluacion) {
        boolean esRetiro = "RETIRO".equalsIgnoreCase(tipoEvaluacion);
        return aplicarBloqueCondicional(html, "esRetiro", esRetiro);
    }

    public String dataUriFromResource(String pathFromResources) throws IOException {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) {
            throw new IllegalStateException("No existe FacesContext activo para resolver recursos.");
        }

        try (InputStream in = openResourceStream(ctx, pathFromResources)) {
            if (in == null) {
                LOG.warn("No se encontro recurso: /resources/{}", pathFromResources);
                return "";
            }
            return buildDataUri(pathFromResources, readResourceBytes(in));
        }
    }

    public String renderFaceletToHtml(String viewId) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            throw new IllegalStateException("FacesContext es null. Solo dentro de request JSF.");
        }

        UIViewRoot originalViewRoot = fc.getViewRoot();
        ResponseWriter originalWriter = fc.getResponseWriter();
        PartialViewContext pvc = fc.getPartialViewContext();
        boolean hadPvc = (pvc != null);
        boolean oldRenderAll = enableFullRender(pvc, hadPvc);

        try {
            return renderTemporaryView(fc, viewId);
        } catch (Exception e) {
            LOG.error("Error renderizando facelet viewId={}", viewId, e);
            throw new RuntimeException("Error renderizando facelet " + viewId, e);
        } finally {
            restoreOriginalContext(fc, originalWriter, originalViewRoot, pvc, hadPvc, oldRenderAll);
        }
    }

    private String aplicarBloqueCondicional(String html, String nombre, boolean incluir) {
        String open = "{{#if " + nombre + "}}";
        String close = "{{/if}}";

        int from = 0;
        while (true) {
            int i = html.indexOf(open, from);
            if (i < 0) {
                break;
            }

            int j = html.indexOf(close, i + open.length());
            if (j < 0) {
                html = html.replace(open, "");
                break;
            }

            int end = j + close.length();
            String contenido = html.substring(i + open.length(), j);
            String reemplazo = incluir ? contenido : "";

            html = html.substring(0, i) + reemplazo + html.substring(end);
            from = i + reemplazo.length();
        }
        return html;
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
            throw new IOException("No se encontro el recurso: " + path);
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

    private InputStream openResourceStream(FacesContext ctx, String pathFromResources) {
        return ctx.getExternalContext().getResourceAsStream("/resources/" + pathFromResources);
    }

    private byte[] readResourceBytes(InputStream in) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) {
                bos.write(buf, 0, r);
            }
            return bos.toByteArray();
        }
    }

    private String buildDataUri(String pathFromResources, byte[] bytes) {
        return "data:" + resolveMimeType(pathFromResources) + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }

    private String resolveMimeType(String pathFromResources) {
        String lower = pathFromResources.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/png";
    }

    private boolean enableFullRender(PartialViewContext pvc, boolean hadPvc) {
        boolean oldRenderAll = false;
        if (hadPvc) {
            try {
                oldRenderAll = pvc.isRenderAll();
                pvc.setRenderAll(true);
            } catch (Exception ignore) {
            }
        }
        return oldRenderAll;
    }

    private String renderTemporaryView(FacesContext fc, String viewId) throws IOException {
        ViewDeclarationLanguage vdl = fc.getApplication().getViewHandler().getViewDeclarationLanguage(fc, viewId);
        UIViewRoot tempViewRoot = createTemporaryView(fc, viewId, vdl);
        StringWriter sw = new StringWriter(128 * 1024);
        ResponseWriter rw = fc.getRenderKit().createResponseWriter(new PrintWriter(sw), "text/html", "UTF-8");
        fc.setViewRoot(tempViewRoot);
        fc.setResponseWriter(rw);
        fc.getApplication().getViewHandler().renderView(fc, tempViewRoot);
        rw.flush();
        return sw.toString();
    }

    private UIViewRoot createTemporaryView(FacesContext fc, String viewId, ViewDeclarationLanguage vdl) throws IOException {
        UIViewRoot tempViewRoot = vdl.createView(fc, viewId);
        tempViewRoot.setLocale(fc.getViewRoot() != null ? fc.getViewRoot().getLocale() : fc.getApplication().getDefaultLocale());
        tempViewRoot.setRenderKitId(fc.getApplication().getViewHandler().calculateRenderKitId(fc));
        vdl.buildView(fc, tempViewRoot);
        return tempViewRoot;
    }

    private void restoreOriginalContext(FacesContext fc, ResponseWriter originalWriter, UIViewRoot originalViewRoot,
            PartialViewContext pvc, boolean hadPvc, boolean oldRenderAll) {
        try {
            fc.setResponseWriter(originalWriter);
        } catch (Exception ignore) {
        }
        try {
            fc.setViewRoot(originalViewRoot);
        } catch (Exception ignore) {
        }
        if (hadPvc) {
            try {
                pvc.setRenderAll(oldRenderAll);
            } catch (Exception ignore) {
            }
        }
    }
}
