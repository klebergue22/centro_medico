package ec.gob.igm.rrhh.consultorio.service;

import jakarta.ejb.Stateless;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Stateless
public class EmailNotificationService {

    private static final String DEFAULT_FROM = "notificaciones.igm@geograficomilitar.gob.ec";
    private static final String DEFAULT_SERVER = "mail.geograficomilitar.gob.ec";
    private static final String DEFAULT_PORT = "587";
    private static final String DEFAULT_USERNAME = "notificaciones.igm@geograficomilitar.gob.ec";
    private static final String DEFAULT_PASSWORD = "notiFI2023";
    private static final String DEFAULT_ENABLE_SSL = "true";

    public void send(String to, String subject, String body) throws MessagingException {
        String recipient = MailConfigResolver.normalize(to);
        if (recipient == null) {
            throw new MessagingException("No se puede enviar correo: destinatario vacío.");
        }
        MimeMessage message = new MimeMessage(buildSession());
        InternetAddress fromAddress = new InternetAddress(DEFAULT_FROM);
        message.setFrom(fromAddress);
        message.setSender(fromAddress);
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, false));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        message.setText(body, StandardCharsets.UTF_8.name());
        Transport.send(message);
    }

    private Session buildSession() {
        String smtpHost = MailConfigResolver.resolve("consultorio.mail.server",
                "CONSULTORIO_MAIL_SERVER", DEFAULT_SERVER);
        String smtpPort = MailConfigResolver.resolve("consultorio.mail.port",
                "CONSULTORIO_MAIL_PORT", DEFAULT_PORT);
        String smtpUsername = MailConfigResolver.resolve("consultorio.mail.username",
                "CONSULTORIO_MAIL_USERNAME", DEFAULT_USERNAME);
        String smtpPassword = MailConfigResolver.resolve("consultorio.mail.password",
                "CONSULTORIO_MAIL_PASSWORD", DEFAULT_PASSWORD);
        boolean enableSsl = Boolean.parseBoolean(MailConfigResolver.resolve("consultorio.mail.enableSsl",
                "CONSULTORIO_MAIL_ENABLESSL", DEFAULT_ENABLE_SSL));

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", String.valueOf(MailConfigResolver.normalize(smtpUsername) != null));
        props.put("mail.smtp.starttls.enable", String.valueOf(enableSsl));
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        if (MailConfigResolver.normalize(smtpUsername) == null) {
            return Session.getInstance(props);
        }
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });
    }
}
