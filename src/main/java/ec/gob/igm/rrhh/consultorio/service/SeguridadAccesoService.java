package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.SegBitacoraAcceso;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;

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
        String ipOrigen = resolveClientIp();
        String navegador = resolveUserAgent();
        String usuarioAuditoria = normalizeUsuario(usernameIntentado);
        SegBitacoraAcceso bitacora = new SegBitacoraAcceso();
        bitacora.setIdUsuario(idUsuario);
        bitacora.setUsernameIntentado(usernameIntentado);
        bitacora.setEvento(evento);
        bitacora.setExitoso(exitoso ? "S" : "N");
        bitacora.setFechaEvento(new Date());
        bitacora.setDetalle(detalle);
        bitacora.setIpOrigen(ipOrigen);
        bitacora.setNavegador(navegador);
        bitacora.setUsrCreacion(usuarioAuditoria);
        em.persist(bitacora);
    }

    private String normalizeUsuario(String usernameIntentado) {
        if (usernameIntentado == null || usernameIntentado.isBlank()) {
            return "USR_APP";
        }
        return usernameIntentado.trim();
    }

    private String resolveUserAgent() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }
        ExternalContext externalContext = facesContext.getExternalContext();
        if (externalContext == null) {
            return null;
        }
        String userAgent = externalContext.getRequestHeaderMap().get("User-Agent");
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        return userAgent.trim();
    }

    private String resolveClientIp() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return null;
        }
        ExternalContext externalContext = facesContext.getExternalContext();
        if (externalContext == null) {
            return null;
        }

        String forwardedFor = header(externalContext, "X-Forwarded-For");
        if (forwardedFor != null) {
            int comma = forwardedFor.indexOf(',');
            return comma > 0 ? forwardedFor.substring(0, comma).trim() : forwardedFor.trim();
        }
        String realIp = header(externalContext, "X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        Object request = externalContext.getRequest();
        if (request instanceof HttpServletRequest httpServletRequest) {
            String remoteAddr = httpServletRequest.getRemoteAddr();
            if (remoteAddr != null && !remoteAddr.isBlank()) {
                return remoteAddr.trim();
            }
        }
        return null;
    }

    private String header(ExternalContext externalContext, String key) {
        String value = externalContext.getRequestHeaderMap().get(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
