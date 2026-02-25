/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.igm.rrhh.consultorio.domain.model;

/**
 *
 * @author GUERRA_KLEBER
 */
 


import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "PERSONA_AUX", schema = "CONSULTORIO")
@NamedQueries({
        @NamedQuery(
                name = "PersonaAux.findAll",
                query = "SELECT p FROM PersonaAux p ORDER BY p.idPersonaAux"
        ),
        @NamedQuery(
                name = "PersonaAux.findByCedula",
                query = "SELECT p FROM PersonaAux p WHERE p.cedula = :cedula"
        ),
        @NamedQuery(
                name = "PersonaAux.findPendientes",
                query = "SELECT p FROM PersonaAux p WHERE p.estado = 'PENDIENTE'"
        ),
        @NamedQuery(
                name = "PersonaAux.findActivos",
                query = "SELECT p FROM PersonaAux p WHERE p.estado = 'ACTIVO'"
        )
})
public class PersonaAux implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seqPersonaAux")
    @SequenceGenerator(
            name = "seqPersonaAux",
            sequenceName = "CONSULTORIO.SQ_PERSONA_AUX",
            allocationSize = 1
    )
    @Column(name = "ID_PERSONA_AUX", nullable = false)
    private Long idPersonaAux;

    @Column(name = "CEDULA", length = 10, nullable = false)
    private String cedula;

    // Campos desglosados
    @Column(name = "APELLIDO1", length = 50)
    private String apellido1;

    @Column(name = "APELLIDO2", length = 50)
    private String apellido2;

    @Column(name = "NOMBRE1", length = 50)
    private String nombre1;

    @Column(name = "NOMBRE2", length = 50)
    private String nombre2;

    // Campos compuestos (se mantienen por compatibilidad / reportes)
    @Column(name = "NOMBRES", length = 100)
    private String nombres;

    @Column(name = "APELLIDOS", length = 100)
    private String apellidos;

    @Column(name = "SEXO", length = 1)
    private String sexo;

    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_NAC")
    private Date fechaNac;

    @Column(name = "NO_PERSONA")
    private Long noPersona;

    @Column(name = "ESTADO", length = 20)
    private String estado; 

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_CREACION")
    private Date fechaCreacion;

    @Column(name = "USR_CREACION", length = 30)
    private String usrCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "F_ACTUALIZACION")
    private Date fechaActualizacion;

    @Column(name = "USR_ACTUALIZACION", length = 30)
    private String usrActualizacion;

    // ==========================
    // Constructor (Requerido por JPA)
    // ==========================
    public PersonaAux() {
    }

    // ===========================
    // Lifecycle
    // ===========================
    @PrePersist
    public void prePersist() {
        if (estado == null || estado.trim().isEmpty()) {
            estado = "PENDIENTE";
        }
        if (fechaCreacion == null) {
            fechaCreacion = new Date();
        }
        // fechaActualizacion normalmente null al crear
        sincronizarCamposCompuestos();
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = new Date();
        sincronizarCamposCompuestos();
    }

    // ==========================================
    // Sincroniza NOMBRES y APELLIDOS (compuestos)
    // ==========================================
    private void sincronizarCamposCompuestos() {
        this.nombres = buildNombreCompleto(nombre1, nombre2);
        this.apellidos = buildNombreCompleto(apellido1, apellido2);
    }

    private static String buildNombreCompleto(String a, String b) {
        String x = (a == null) ? "" : a.trim();
        String y = (b == null) ? "" : b.trim();
        String res = (x + " " + y).trim();
        return res.isEmpty() ? null : res;
    }

    // ==========================
    // Equals y HashCode
    // Basado en idPersonaAux
    // ==========================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonaAux that = (PersonaAux) o;
        return Objects.equals(idPersonaAux, that.idPersonaAux);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPersonaAux);
    }

    // ==========================
    // Getters y Setters
    // ==========================
    public Long getIdPersonaAux() {
        return idPersonaAux;
    }

    public void setIdPersonaAux(Long idPersonaAux) {
        this.idPersonaAux = idPersonaAux;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getApellido1() {
        return apellido1;
    }

    public void setApellido1(String apellido1) {
        this.apellido1 = apellido1;
    }

    public String getApellido2() {
        return apellido2;
    }

    public void setApellido2(String apellido2) {
        this.apellido2 = apellido2;
    }

    public String getNombre1() {
        return nombre1;
    }

    public void setNombre1(String nombre1) {
        this.nombre1 = nombre1;
    }

    public String getNombre2() {
        return nombre2;
    }

    public void setNombre2(String nombre2) {
        this.nombre2 = nombre2;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Date getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(Date fechaNac) {
        this.fechaNac = fechaNac;
    }

    public Long getNoPersona() {
        return noPersona;
    }

    public void setNoPersona(Long noPersona) {
        this.noPersona = noPersona;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getUsrCreacion() {
        return usrCreacion;
    }

    public void setUsrCreacion(String usrCreacion) {
        this.usrCreacion = usrCreacion;
    }

    public Date getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(Date fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getUsrActualizacion() {
        return usrActualizacion;
    }

    public void setUsrActualizacion(String usrActualizacion) {
        this.usrActualizacion = usrActualizacion;
    }
    
    @Override
    public String toString() {
        return "PersonaAux{" +
                "idPersonaAux=" + idPersonaAux +
                ", cedula='" + cedula + '\'' +
                ", apellido1='" + apellido1 + '\'' +
                ", apellido2='" + apellido2 + '\'' +
                ", nombre1='" + nombre1 + '\'' +
                ", nombre2='" + nombre2 + '\'' +
                ", nombres='" + nombres + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", sexo='" + sexo + '\'' +
                ", fechaNac=" + fechaNac +
                ", noPersona=" + noPersona +
                ", estado='" + estado + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", usrCreacion='" + usrCreacion + '\'' +
                ", fechaActualizacion=" + fechaActualizacion +
                ", usrActualizacion='" + usrActualizacion + '\'' +
                '}';
    }
}