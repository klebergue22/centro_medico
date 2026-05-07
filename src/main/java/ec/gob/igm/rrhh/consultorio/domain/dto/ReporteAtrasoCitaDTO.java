package ec.gob.igm.rrhh.consultorio.domain.dto;

import java.io.Serializable;
import java.util.Date;

public class ReporteAtrasoCitaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idCita;
    private String cedula;
    private String paciente;
    private String especialidad;
    private String profesional;
    private Date fechaInicio;
    private String estado;
    private Long minutosAtraso;

    public ReporteAtrasoCitaDTO() {
    }

    public ReporteAtrasoCitaDTO(Long idCita, String cedula, String paciente, String especialidad,
            String profesional, Date fechaInicio, String estado, Long minutosAtraso) {
        this.idCita = idCita;
        this.cedula = cedula;
        this.paciente = paciente;
        this.especialidad = especialidad;
        this.profesional = profesional;
        this.fechaInicio = fechaInicio;
        this.estado = estado;
        this.minutosAtraso = minutosAtraso;
    }

    public Long getIdCita() { return idCita; }
    public void setIdCita(Long idCita) { this.idCita = idCita; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getPaciente() { return paciente; }
    public void setPaciente(String paciente) { this.paciente = paciente; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getProfesional() { return profesional; }
    public void setProfesional(String profesional) { this.profesional = profesional; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Long getMinutosAtraso() { return minutosAtraso; }
    public void setMinutosAtraso(Long minutosAtraso) { this.minutosAtraso = minutosAtraso; }
}
