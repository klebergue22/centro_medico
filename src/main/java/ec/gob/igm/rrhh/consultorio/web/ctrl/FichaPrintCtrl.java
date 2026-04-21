package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.service.FichaOcupacionalService;
import jakarta.ejb.EJB;
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

    @EJB
    private FichaOcupacionalService fichaOcupacionalService;

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

        String idFichaParam = ec.getRequestParameterMap().get("idFicha");
        if (idFichaParam == null || idFichaParam.isBlank()) {
            return null;
        }

        Long idFicha;
        try {
            idFicha = Long.valueOf(idFichaParam);
        } catch (NumberFormatException ex) {
            return null;
        }

        FichaOcupacional ficha = fichaOcupacionalService.reloadForPrint(idFicha);
        if (ficha == null) {
            return null;
        }

        CentroMedicoCtrl cm = ctx.getApplication().evaluateExpressionGet(ctx, "#{centroMedicoCtrl}", CentroMedicoCtrl.class);
        if (cm == null) {
            return null;
        }

        cm.setFicha(ficha);
        cm.setEmpleadoSel(ficha.getEmpleado());
        cm.setNoPersonaSel(ficha.getEmpleado() != null ? ficha.getEmpleado().getNoPersona() : null);
        cm.setPersonaAux(ficha.getPersonaAux());
        ec.getSessionMap().put("centroMedicoPrint", cm);
        return cm;
    }

    public CentroMedicoCtrl getCm() {
        return getCentroMedico();
    }
}
