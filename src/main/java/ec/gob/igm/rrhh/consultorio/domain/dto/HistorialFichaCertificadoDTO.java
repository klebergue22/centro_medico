package ec.gob.igm.rrhh.consultorio.domain.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO para presentar el historial de fichas/certificados por cédula en UI.
 */
public class HistorialFichaCertificadoDTO implements Serializable {

    private Long idFicha;
    private String cedula;
    private String nombrePaciente;
    private Date fechaEvaluacion;
    private Date fechaEmision;
    private String estado;
    private String usuarioRegistro;
    private String ultimaAccion;
    private Date fechaUltimaAccion;

    public HistorialFichaCertificadoDTO() {
    }

    public HistorialFichaCertificadoDTO(Long idFicha, String cedula, String nombrePaciente,
            Date fechaEvaluacion, Date fechaEmision, String estado, String usuarioRegistro) {
        this.idFicha = idFicha;
        this.cedula = cedula;
        this.nombrePaciente = nombrePaciente;
        this.fechaEvaluacion = fechaEvaluacion;
        this.fechaEmision = fechaEmision;
        this.estado = estado;
        this.usuarioRegistro = usuarioRegistro;
    }

    public Long getIdFicha() { return idFicha; }
    public void setIdFicha(Long idFicha) { this.idFicha = idFicha; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombrePaciente() { return nombrePaciente; }
    public void setNombrePaciente(String nombrePaciente) { this.nombrePaciente = nombrePaciente; }

    public Date getFechaEvaluacion() { return fechaEvaluacion; }
    public void setFechaEvaluacion(Date fechaEvaluacion) { this.fechaEvaluacion = fechaEvaluacion; }

    public Date getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(Date fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getUsuarioRegistro() { return usuarioRegistro; }
    public void setUsuarioRegistro(String usuarioRegistro) { this.usuarioRegistro = usuarioRegistro; }

    public String getUltimaAccion() { return ultimaAccion; }
    public void setUltimaAccion(String ultimaAccion) { this.ultimaAccion = ultimaAccion; }

    public Date getFechaUltimaAccion() { return fechaUltimaAccion; }
    public void setFechaUltimaAccion(Date fechaUltimaAccion) { this.fechaUltimaAccion = fechaUltimaAccion; }
}
