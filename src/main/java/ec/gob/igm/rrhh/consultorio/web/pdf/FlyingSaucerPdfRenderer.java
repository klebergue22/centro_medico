package ec.gob.igm.rrhh.consultorio.web.pdf;

import com.lowagie.text.DocumentException;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;

@ApplicationScoped
public class FlyingSaucerPdfRenderer implements PdfRenderer {

    private static final Logger LOG = LoggerFactory.getLogger(FlyingSaucerPdfRenderer.class);

    @Override
    public byte[] render(String xhtml) throws DocumentException, IOException {
        if (xhtml == null || xhtml.trim().isEmpty()) {
            LOG.error("render: El string HTML recibido es nulo o vacío");
            throw new IllegalArgumentException("El contenido HTML para generar el PDF está vacío.");
        }

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(xhtml);
        renderer.layout();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            renderer.createPDF(baos);
            renderer.finishPDF();
            return baos.toByteArray();
        }
    }
}
