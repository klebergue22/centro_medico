package ec.gob.igm.rrhh.consultorio.domain.dto;



import java.io.Serializable;
import java.util.Date;

/**
 * Class EmpleadoCargoDTO: transporta datos entre capas de la aplicación.
 */
public class EmpleadoCargoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long noPersona;
    private String noCedula;

    private String priApellido;
    private String segApellido;
    private String nombres;
    private String nombreC;

    private String sexo;
    private Date fNacimiento;

    private String telefono;
    private String email;
    private String emailInstitucional;

    private Long noCont;
    private String estadoContrato;
    private Date fContrato;
    private Date fSalida;

    private Long noFuncion;

    private String cargoDescrip;
    private String cargoAbrev;

    public EmpleadoCargoDTO() {
    }

    // Getters / Setters

    public Long getNoPersona() {
        return noPersona;
    }

    public void setNoPersona(Long noPersona) {
        this.noPersona = noPersona;
    }

    public String getNoCedula() {
        return noCedula;
    }

    public void setNoCedula(String noCedula) {
        this.noCedula = noCedula;
    }

    public String getPriApellido() {
        return priApellido;
    }

    public void setPriApellido(String priApellido) {
        this.priApellido = priApellido;
    }

    public String getSegApellido() {
        return segApellido;
    }

    public void setSegApellido(String segApellido) {
        this.segApellido = segApellido;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getNombreC() {
        return nombreC;
    }

    public void setNombreC(String nombreC) {
        this.nombreC = nombreC;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Date getFNacimiento() {
        return fNacimiento;
    }

    public void setFNacimiento(Date fNacimiento) {
        this.fNacimiento = fNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailInstitucional() {
        return emailInstitucional;
    }

    public void setEmailInstitucional(String emailInstitucional) {
        this.emailInstitucional = emailInstitucional;
    }

    public Long getNoCont() {
        return noCont;
    }

    public void setNoCont(Long noCont) {
        this.noCont = noCont;
    }

    public String getEstadoContrato() {
        return estadoContrato;
    }

    public void setEstadoContrato(String estadoContrato) {
        this.estadoContrato = estadoContrato;
    }

    public Date getFContrato() {
        return fContrato;
    }

    public void setFContrato(Date fContrato) {
        this.fContrato = fContrato;
    }

    public Date getFSalida() {
        return fSalida;
    }

    public void setFSalida(Date fSalida) {
        this.fSalida = fSalida;
    }

    public Long getNoFuncion() {
        return noFuncion;
    }

    public void setNoFuncion(Long noFuncion) {
        this.noFuncion = noFuncion;
    }

    public String getCargoDescrip() {
        return cargoDescrip;
    }

    public void setCargoDescrip(String cargoDescrip) {
        this.cargoDescrip = cargoDescrip;
    }

    public String getCargoAbrev() {
        return cargoAbrev;
    }

    public void setCargoAbrev(String cargoAbrev) {
        this.cargoAbrev = cargoAbrev;
    }


    public String getApellidosNombres() {
        String a1 = priApellido != null ? priApellido.trim() : "";
        String a2 = segApellido != null ? segApellido.trim() : "";
        String n = nombres != null ? nombres.trim() : "";
        return (a1 + " " + a2 + " " + n).trim().replaceAll("\\s{2,}", " ");
    }
}
