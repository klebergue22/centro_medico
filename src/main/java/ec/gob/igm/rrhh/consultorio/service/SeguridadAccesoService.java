package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.SegBitacoraAcceso;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Date;

@Stateless
public class SeguridadAccesoService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    public void registrarEvento(Long idUsuario, String usernameIntentado, String evento, boolean exitoso, String detalle) {
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
