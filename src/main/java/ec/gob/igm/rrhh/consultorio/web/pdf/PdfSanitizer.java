package ec.gob.igm.rrhh.consultorio.web.pdf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;

@ApplicationScoped
public class PdfSanitizer {

    private static final Pattern AMP_BAD = Pattern.compile("&(?!amp;|lt;|gt;|quot;|apos;|#\\d+;|#x[0-9A-Fa-f]+;)");

    public String sanitizeXhtmlForPdf(String html) {
        if (html == null) {
            return null;
        }
        String output = html.replace("&nbsp;", "&#160;");
        return AMP_BAD.matcher(output).replaceAll("&amp;");
    }

    public void dumpXhtmlDebug(String fileName, String xhtml, Exception ex, Logger log) {
        try {
            Path p = Path.of(System.getProperty("java.io.tmpdir"), fileName);
            Files.writeString(p, xhtml, StandardCharsets.UTF_8);
            log.error("PDF DEBUG: Se guardó XHTML en {}", p.toAbsolutePath(), ex);
        } catch (IOException ioEx) {
            log.error("PDF DEBUG: No se pudo guardar XHTML de diagnóstico.", ioEx);
        }
    }
}
