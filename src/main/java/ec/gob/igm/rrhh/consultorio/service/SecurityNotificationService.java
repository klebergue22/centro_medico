package ec.gob.igm.rrhh.consultorio.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.mail.MessagingException;

import java.security.SecureRandom;
import java.util.Base64;

@Stateless
public class SecurityNotificationService {

    private static final String DEFAULT_ADMIN_EMAIL = "kleber.guerra@geograficomilitar.gob.ec";

    @EJB
    private EmailNotificationService emailNotificationService;

    public String generarClaveTemporal() {
        byte[] bytes = new byte[9];
        new SecureRandom().nextBytes(bytes);
        return "Cm-" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public void enviarNotificacionResetClave(String correoUsuario, String nombreUsuario, String cedula, String claveTemporal)
            throws MessagingException {
        String destinatarioUsuario = MailConfigResolver.normalize(correoUsuario);
        String adminEmail = MailConfigResolver.resolve("consultorio.mail.admin",
                "CONSULTORIO_MAIL_ADMIN", DEFAULT_ADMIN_EMAIL);
        if (destinatarioUsuario == null) {
            throw new MessagingException("El usuario no tiene correo registrado para notificación.");
        }
        if (MailConfigResolver.normalize(adminEmail) == null) {
            throw new MessagingException("No existe correo de administrador configurado para notificación.");
        }

        String subject = "Restablecimiento de clave - Sistema Centro Médico";
        String bodyUsuario = "Estimado/a " + valueOrDefault(nombreUsuario, "usuario") + ",\n\n"
                + "Se solicitó el restablecimiento de su clave para el Sistema Centro Médico.\n"
                + "Usuario (cédula): " + valueOrDefault(cedula, "N/D") + "\n"
                + "Clave temporal: " + claveTemporal + "\n\n"
                + "Ingrese al sistema y cambie su clave inmediatamente.\n\n"
                + "Este correo fue generado automáticamente.";

        String bodyAdmin = "Notificación de restablecimiento de clave:\n\n"
                + "Usuario: " + valueOrDefault(nombreUsuario, "N/D") + "\n"
                + "Cédula: " + valueOrDefault(cedula, "N/D") + "\n"
                + "Correo usuario: " + destinatarioUsuario + "\n\n"
                + "Se envió una clave temporal al usuario para cambio obligatorio al siguiente ingreso.";

        emailNotificationService.send(destinatarioUsuario, subject, bodyUsuario);
        emailNotificationService.send(adminEmail, subject, bodyAdmin);
    }

    private String valueOrDefault(String value, String fallback) {
        String normalized = MailConfigResolver.normalize(value);
        return normalized != null ? normalized : fallback;
    }
}
