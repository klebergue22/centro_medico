package ec.gob.igm.rrhh.consultorio.web.pdf;

import java.io.IOException;

public interface PdfRenderer {

    byte[] render(String xhtml) throws IOException;
}
