package ec.gob.igm.rrhh.consultorio.web.facade;

import java.io.Serializable;

import ec.gob.igm.rrhh.consultorio.web.audit.CentroMedicoAuditService;
import ec.gob.igm.rrhh.consultorio.web.service.UserContextService;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
/**
 * Fachada para registrar auditoría del módulo web con usuario resuelto desde
 * contexto de sesión.
 */
public class AuditFacade implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private CentroMedicoAuditService centroMedicoAuditService;
    @EJB
    private UserContextService userContextService;

    public void registrar(String accion, String tabla, String campo, String observaciones) {
        String user = userContextService.resolveCurrentUser();
        centroMedicoAuditService.registrar(accion, tabla, campo, observaciones, user);
    }
}
