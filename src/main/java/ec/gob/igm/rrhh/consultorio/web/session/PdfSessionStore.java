package ec.gob.igm.rrhh.consultorio.web.session;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;

public interface PdfSessionStore {

    void put(FacesContext ctx, String token, byte[] bytes);

    byte[] find(HttpSession session, String token);

    void remove(FacesContext ctx, String token);
}
