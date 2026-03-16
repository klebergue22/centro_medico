package ec.gob.igm.rrhh.consultorio.web.pdf;

import jakarta.faces.context.ExternalContext;
import jakarta.servlet.http.HttpSession;

/**
 * Interface PdfSessionStore: gestiona la construcción y renderización de documentos PDF.
 */
public interface PdfSessionStore {

    String PDF_STORE_KEY = "PDF_STORE";

    void store(ExternalContext externalContext, String token, byte[] bytes);

    byte[] find(HttpSession session, String token);
}
