package ec.gob.igm.rrhh.consultorio.web.pdf;

import com.lowagie.text.DocumentException;
import java.io.IOException;

/**
 * Abstracción para renderizado PDF (DIP).
 */
public interface PdfRenderer {

    byte[] render(String xhtml) throws DocumentException, IOException;
}
