package ec.gob.igm.rrhh.consultorio.web.pdf;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class HttpSessionPdfSessionStore implements PdfSessionStore {

    @Override
    public void store(ExternalContext externalContext, String token, byte[] bytes) {
        HttpSession session = (HttpSession) externalContext.getSession(true);

        @SuppressWarnings("unchecked")
        Map<String, byte[]> pdfStore = (Map<String, byte[]>) session.getAttribute(PDF_STORE_KEY);
        if (pdfStore == null) {
            pdfStore = new HashMap<>();
            session.setAttribute(PDF_STORE_KEY, pdfStore);
        }
        pdfStore.put(token, bytes);
    }

    @Override
    public byte[] find(HttpSession session, String token) {
        Object direct = session.getAttribute(token);
        if (direct instanceof byte[]) {
            return (byte[]) direct;
        }

        Object storeObj = session.getAttribute(PDF_STORE_KEY);
        if (storeObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, byte[]> store = (Map<String, byte[]>) storeObj;
            return store.get(token);
        }

        return null;
    }
}
