package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.SegBitacoraAcceso;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Date;

@Stateless
public class SeguridadAccesoService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;
    @EJB
    private ClientIdentifierService clientIdentifierService;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void registrarEvento(Long idUsuario, String usernameIntentado, String evento, boolean exitoso, String detalle) {
        clientIdentifierService.apply(usernameIntentado);
        SegBitacoraAcceso bitacora = new SegBitacoraAcceso();
        bitacora.setIdUsuario(idUsuario);
        bitacora.setUsernameIntentado(usernameIntentado);
        bitacora.setEvento(evento);
        bitacora.setExitoso(exitoso ? "S" : "N");
        bitacora.setFechaEvento(new Date());
        bitacora.setDetalle(detalle);
        em.persist(bitacora);
    }
}
