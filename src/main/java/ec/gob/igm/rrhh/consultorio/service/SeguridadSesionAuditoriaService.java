package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.AuditoriaConsultorio;
import ec.gob.igm.rrhh.consultorio.domain.model.SegAuditoriaDet;
import ec.gob.igm.rrhh.consultorio.domain.model.SegSesion;
import ec.gob.igm.rrhh.consultorio.domain.model.UsuarioAuth;
import jakarta.ejb.Stateless;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Stateless
public class SeguridadSesionAuditoriaService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    public void registrarLoginExitoso(UsuarioAuth usuario, String cedula) {
        if (usuario == null) {
            return;
        }

        Date ahora = new Date();
        String ipOrigen = resolveClientIp();
        String navegador = resolveUserAgent();
        String usuarioAuditoria = resolveUsuarioAuditoria(cedula, usuario);
        Long rolId = buscarRolActivo(usuario.getIdUsuario());
        SegSesion sesion = new SegSesion();
        sesion.setIdUsuario(usuario.getIdUsuario());
        sesion.setTokenSesion(UUID.randomUUID().toString());
        sesion.setFechaLogin(ahora);
        sesion.setFechaUltimaAct(ahora);
        sesion.setFechaExpiracion(new Date(ahora.getTime() + 60L * 60L * 1000L));
        sesion.setActiva("S");
        sesion.setIpOrigen(ipOrigen);
        sesion.setNavegador(navegador);
        sesion.setUsrCreacion(usuarioAuditoria);
        em.persist(sesion);
        em.flush();

        SegAuditoriaDet auditoriaDet = new SegAuditoriaDet();
        auditoriaDet.setIdUsuario(usuario.getIdUsuario());
        auditoriaDet.setIdSesion(sesion.getIdSesion());
        auditoriaDet.setModulo("SEGURIDAD");
        auditoriaDet.setProceso("LOGIN");
        auditoriaDet.setEntidad("SEG_SESION");
        auditoriaDet.setIdRegistro(String.valueOf(sesion.getIdSesion()));
        auditoriaDet.setOperacion("I");
        auditoriaDet.setValorNuevo("CEDULA=" + safe(cedula)
                + ", ID_USUARIO=" + usuario.getIdUsuario()
                + ", ID_ROL=" + safe(rolId)
                + ", IP_ORIGEN=" + safe(ipOrigen)
                + ", NAVEGADOR=" + safe(navegador)
                + ", USR_CREACION=" + safe(usuarioAuditoria));
        auditoriaDet.setFechaEvento(ahora);
        em.persist(auditoriaDet);

        AuditoriaConsultorio auditoria = new AuditoriaConsultorio();
        auditoria.setModulo("SEGURIDAD");
        auditoria.setUsuario(usuarioAuditoria);
        auditoria.setFecha(ahora);
        auditoria.setAccion("LOGIN_OK");
        auditoria.setTablaAfecta("SEG_SESION");
        auditoria.setCampoAfecta("ID_SESION");
        auditoria.setObservaciones("Inicio de sesión exitoso. ID_SESION=" + sesion.getIdSesion()
                + ", ID_ROL=" + safe(rolId)
                + ", IP_ORIGEN=" + safe(ipOrigen)
                + ", NAVEGADOR=" + safe(navegador)
                + ", USR_CREACION=" + safe(usuarioAuditoria));
        auditoria.setPkValor(String.valueOf(sesion.getIdSesion()));
        em.persist(auditoria);
    }

    public void registrarIntentoFallido(String cedula, String motivo) {
        AuditoriaConsultorio auditoria = new AuditoriaConsultorio();
        auditoria.setModulo("SEGURIDAD");
        auditoria.setUsuario(safe(cedula));
        auditoria.setFecha(new Date());
        auditoria.setAccion("LOGIN_FAIL");
        auditoria.setTablaAfecta("SEG_USUARIO");
        auditoria.setCampoAfecta("USERNAME");
        auditoria.setObservaciones(safe(motivo) + ", IP_ORIGEN=" + safe(resolveClientIp()));
        auditoria.setPkValor(safe(cedula));
        em.persist(auditoria);
    }

    private Long buscarRolActivo(Long idUsuario) {
        if (idUsuario == null) {
            return null;
        }
        List<Long> roles = em.createQuery("""
                SELECT ur.idRol
                FROM SegUsuarioRol ur
                WHERE ur.idUsuario = :idUsuario
                  AND ur.activo = 'S'
                ORDER BY ur.idUsuarioRol DESC
                """, Long.class)
            .setParameter("idUsuario", idUsuario)
            .setMaxResults(1)
            .getResultList();
        return roles.isEmpty() ? null : roles.get(0);
    }

    private String resolveUsuarioAuditoria(String cedula, UsuarioAuth usuario) {
        if (cedula != null && !cedula.isBlank()) {
            return cedula.trim();
        }
        if (usuario != null && usuario.getUsername() != null && !usuario.getUsername().isBlank()) {
            return usuario.getUsername().trim();
        }
        return "USR_APP";
    }

    private String safe(Object value) {
        return value == null ? "N/D" : String.valueOf(value);
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
}
