package ec.gob.igm.rrhh.consultorio.web.service;

import java.security.Principal;

import jakarta.ejb.Stateless;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

@Stateless
/**
 * Resuelve el usuario actual de la sesión web para operaciones de auditoría
 * y campos técnicos de persistencia.
 */
public class UserContextService {

    public static final String DEFAULT_TECH_USER = "USR_APP";

    public String resolveCurrentUser() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) {
            return DEFAULT_TECH_USER;
        }

        ExternalContext externalContext = facesContext.getExternalContext();
        if (externalContext == null) {
            return DEFAULT_TECH_USER;
        }

        String remoteUser = trimToNull(externalContext.getRemoteUser());
        if (remoteUser != null) {
            return remoteUser;
        }

        String sessionUser = trimToNull(String.valueOf(externalContext.getSessionMap().get("AUTH_USER")));
        if (sessionUser != null && !"null".equalsIgnoreCase(sessionUser)) {
            return sessionUser;
        }

        Principal principal = externalContext.getUserPrincipal();
        if (principal == null) {
            return DEFAULT_TECH_USER;
        }

        String principalName = trimToNull(principal.getName());
        return principalName != null ? principalName : DEFAULT_TECH_USER;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

