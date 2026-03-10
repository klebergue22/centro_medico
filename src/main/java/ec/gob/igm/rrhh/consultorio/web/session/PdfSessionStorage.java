package ec.gob.igm.rrhh.consultorio.web.session;

import jakarta.faces.context.FacesContext;

public interface PdfSessionStorage {

    void put(FacesContext ctx, String token, byte[] bytes);

    void remove(FacesContext ctx, String token);
}
