package ec.gob.igm.rrhh.consultorio.web.session;

import jakarta.ejb.Stateless;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Stateless
public class PdfSessionStore {

    private static final String PDF_STORE_KEY = "PDF_STORE";

    public void put(FacesContext ctx, String token, byte[] bytes) {
        if (ctx == null || token == null || bytes == null) {
            return;
        }
        ExternalContext ec = ctx.getExternalContext();
        HttpSession session = (HttpSession) ec.getSession(true);
        getStore(session, true).put(token, bytes);
    }

    public void remove(FacesContext ctx, String token) {
        if (ctx == null || token == null) {
            return;
        }
        ExternalContext ec = ctx.getExternalContext();
        HttpSession session = (HttpSession) ec.getSession(false);
        if (session == null) {
            return;
        }
        Map<String, byte[]> store = getStore(session, false);
        if (store != null) {
            store.remove(token);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, byte[]> getStore(HttpSession session, boolean create) {
        Map<String, byte[]> store = (Map<String, byte[]>) session.getAttribute(PDF_STORE_KEY);
        if (store == null && create) {
            store = new HashMap<>();
            session.setAttribute(PDF_STORE_KEY, store);
        }
        return store;
    }
}
