package ec.gob.igm.rrhh.consultorio.web.audit;

import ec.gob.igm.rrhh.consultorio.domain.model.AuditoriaConsultorio;
import ec.gob.igm.rrhh.consultorio.service.AuditoriaConsultorioService;
import ec.gob.igm.rrhh.consultorio.web.service.UserContextService;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.Date;

@Stateless
/**
 * Class CentroMedicoAuditService: registra eventos y auditoría del módulo de consultorio.
 */
public class CentroMedicoAuditService {

    @EJB
    private AuditoriaConsultorioService auditoriaService;
    @EJB
    private UserContextService userContextService;


    public void registrar(String accion, String tabla, String campo, String observaciones) {
        registrar(accion, tabla, campo, observaciones, userContextService.resolveCurrentUser());
    }

    public void registrar(String accion, String tabla, String campo, String observaciones, String usuario) {
        AuditoriaConsultorio aud = new AuditoriaConsultorio();
        aud.setModulo("CENTRO_MEDICO");
        aud.setUsuario(usuario);
        aud.setFecha(new Date());
        aud.setAccion(accion);
        aud.setTablaAfecta(tabla);
        aud.setCampoAfecta(campo);
        aud.setObservaciones(observaciones);
        auditoriaService.guardar(aud);
    }
}
