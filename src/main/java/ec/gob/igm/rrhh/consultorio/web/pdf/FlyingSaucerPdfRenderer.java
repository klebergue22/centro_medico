package ec.gob.igm.rrhh.consultorio.web.pdf;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@ApplicationScoped
public class FlyingSaucerPdfRenderer implements PdfRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(FlyingSaucerPdfRenderer.class);

    @Override
    public byte[] render(String xhtml) throws IOException {
        if (xhtml == null || xhtml.trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido XHTML para generar el PDF está vacío.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();

        String baseURL = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getResource("/")
                .toExternalForm();

        try {
            String fontsBase = FacesContext.getCurrentInstance()
                    .getExternalContext().getRealPath("/resources/fonts/");
            if (fontsBase != null) {
                renderer.getFontResolver().addFont(
                        fontsBase + File.separator + "DejaVuSans.ttf",
                        BaseFont.IDENTITY_H, true
                );
            }
        } catch (DocumentException | IOException e) {
            LOG.debug("Skipping optional font registration for PDF rendering.", e);
        }

        renderer.setDocumentFromString(xhtml, baseURL);
        renderer.layout();
        renderer.createPDF(baos);
        renderer.finishPDF();

        return baos.toByteArray();
    }
}
