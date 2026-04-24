package ec.gob.igm.rrhh.consultorio.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.mail.MessagingException;

import java.text.SimpleDateFormat;
import java.util.Date;

@Stateless
public class MedicalNotificationService {

    @EJB
    private EmailNotificationService emailNotificationService;

    public void enviarRecetaMedicaAtencion(String correoInstitucional, String nombrePaciente, Date fechaAtencion)
            throws MessagingException {
        enviarRecetaMedicaAtencion(correoInstitucional, nombrePaciente, fechaAtencion, null, null);
    }

    public void enviarRecetaMedicaAtencion(String correoInstitucional, String nombrePaciente, Date fechaAtencion,
                                           byte[] recetaPdf, String recetaPdfNombre)
            throws MessagingException {
        String destinatario = MailConfigResolver.normalize(correoInstitucional);
        if (destinatario == null) {
            throw new MessagingException("No existe correo institucional para enviar la receta.");
        }
        String fechaTexto = formatFecha(fechaAtencion);
        boolean conRecetaAdjunta = recetaPdf != null && recetaPdf.length > 0;
        String subject = conRecetaAdjunta
                ? "Receta medica atencion " + fechaTexto
                : "Atención médica registrada " + fechaTexto;
        String body = conRecetaAdjunta
                ? "Se generó la receta médica de atención con fecha " + fechaTexto + ".\n\n"
                    + "Paciente: " + valueOrDefault(nombrePaciente, "N/D") + "\n"
                    + "Correo institucional destino: " + destinatario + "\n\n"
                    + "Este correo fue generado automáticamente por el Sistema Centro Médico."
                : "Se registró una atención médica con fecha " + fechaTexto + ".\n\n"
                    + "Paciente: " + valueOrDefault(nombrePaciente, "N/D") + "\n"
                    + "Correo institucional destino: " + destinatario + "\n\n"
                    + "No se adjunta receta para esta atención.\n\n"
                    + "Este correo fue generado automáticamente por el Sistema Centro Médico.";
        if (conRecetaAdjunta) {
            String nombreAdjunto = MailConfigResolver.normalize(recetaPdfNombre);
            if (nombreAdjunto == null) {
                nombreAdjunto = "receta-medica-" + fechaTexto.replace("/", "-") + ".pdf";
            }
            emailNotificationService.sendWithAttachment(destinatario, subject, body,
                    recetaPdf, nombreAdjunto, "application/pdf");
            return;
        }
        emailNotificationService.send(destinatario, subject, body);
    }


    public void enviarNotificacionCitaAgendada(String correoInstitucional, String nombrePaciente, Date fechaCita)
            throws MessagingException {
        enviarNotificacionCita(correoInstitucional, nombrePaciente, fechaCita, "Cita médica agendada");
    }

    public void enviarNotificacionCitaCancelada(String correoInstitucional, String nombrePaciente, Date fechaCita)
            throws MessagingException {
        enviarNotificacionCita(correoInstitucional, nombrePaciente, fechaCita, "Cita médica cancelada");
    }

    public void enviarNotificacionCitaReagendada(String correoInstitucional, String nombrePaciente, Date fechaCita)
            throws MessagingException {
        enviarNotificacionCita(correoInstitucional, nombrePaciente, fechaCita, "Cita médica reagendada");
    }

    private void enviarNotificacionCita(String correoInstitucional, String nombrePaciente, Date fechaCita, String tipo)
            throws MessagingException {
        String destinatario = MailConfigResolver.normalize(correoInstitucional);
        if (destinatario == null) {
            throw new MessagingException("No existe correo institucional para notificar la cita.");
        }
        String fechaTexto = formatFecha(fechaCita);
        String body = tipo + " para el paciente " + valueOrDefault(nombrePaciente, "N/D")
                + ", fecha " + fechaTexto + ".\n\nEste correo fue generado automáticamente por el Sistema Centro Médico.";
        emailNotificationService.send(destinatario, tipo + " - " + fechaTexto, body);
    }

    private String formatFecha(Date fecha) {
        Date source = fecha != null ? fecha : new Date();
        return new SimpleDateFormat("dd/MM/yyyy").format(source);
    }

    private String valueOrDefault(String value, String fallback) {
        String normalized = MailConfigResolver.normalize(value);
        return normalized != null ? normalized : fallback;
    }
}
