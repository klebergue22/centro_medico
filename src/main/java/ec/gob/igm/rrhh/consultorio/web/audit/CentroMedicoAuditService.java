package ec.gob.igm.rrhh.consultorio.web.audit;

import ec.gob.igm.rrhh.consultorio.domain.model.AuditoriaConsultorio;
import ec.gob.igm.rrhh.consultorio.service.AuditoriaConsultorioService;
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

    public void registrar(String accion, String tabla, String campo, String observaciones) {
        AuditoriaConsultorio aud = new AuditoriaConsultorio();
        aud.setModulo("CENTRO_MEDICO");
        aud.setUsuario("USR_APP");
        aud.setFecha(new Date());
        aud.setAccion(accion);
        aud.setTablaAfecta(tabla);
        aud.setCampoAfecta(campo);
        aud.setObservaciones(observaciones);
        auditoriaService.guardar(aud);
    }
}
