package ec.gob.igm.rrhh.consultorio.web.ctrl;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("fichaPrintCtrl")
@RequestScoped
/**
 * Class FichaPrintCtrl: controla las acciones de la interfaz web.
 */
public class FichaPrintCtrl {

    public CentroMedicoCtrl getCentroMedico() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) {
            return null;
        }
        ExternalContext ec = ctx.getExternalContext();
        if (ec == null) {
            return null;
        }
        Object obj = ec.getSessionMap().get("centroMedicoPrint");
        if (obj instanceof CentroMedicoCtrl) {
            return (CentroMedicoCtrl) obj;
        }
        return null;
    }

    public CentroMedicoCtrl getCm() {
        return getCentroMedico();
    }
}
