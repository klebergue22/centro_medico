package ec.gob.igm.rrhh.consultorio.web.pdf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;

@ApplicationScoped
public class PdfResourceResolver {

    public String readPdfTemplate(String templateName) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) {
            throw new IllegalStateException("FacesContext no disponible para leer template PDF.");
        }

        String resourcePath = "/resources/pdf/" + templateName;
        try (InputStream in = ctx.getExternalContext().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("No se encontró la plantilla: " + resourcePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo recurso: " + resourcePath, e);
        }
    }

    public String buildLogoDataUri(String fileName) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) {
            return "";
        }

        String resourcePath = "/resources/images/" + fileName;
        try (InputStream in = ctx.getExternalContext().getResourceAsStream(resourcePath)) {
            if (in == null) {
                return "";
            }

            byte[] bytes = in.readAllBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);

            String lower = fileName.toLowerCase(Locale.ROOT);
            String mime = lower.endsWith(".png") ? "image/png"
                    : (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) ? "image/jpeg"
                    : "application/octet-stream";

            return "data:" + mime + ";base64," + base64;
        } catch (IOException e) {
            return "";
        }
    }

    public String resolveImageUrl(String fileName) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) {
            return "";
        }
        try {
            return ctx.getExternalContext().getResource("/resources/images/" + fileName).toExternalForm();
        } catch (RuntimeException ex) {
            return "";
        }
    }
}
