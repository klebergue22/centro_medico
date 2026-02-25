/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ec.gob.igm.rrhh.consultorio.domain.model;

/**
 *
 * @author GUERRA_KLEBER
 */
 

import ec.gob.igm.rrhh.consultorio.domain.enums.EstadoCivil;
import ec.gob.igm.rrhh.consultorio.domain.enums.GrupoSangre;
import ec.gob.igm.rrhh.consultorio.domain.enums.Sexo;
import ec.gob.igm.rrhh.consultorio.persistence.converter.EstadoCivilConverterJPA;
import ec.gob.igm.rrhh.consultorio.persistence.converter.GrupoSangreConverter;
import ec.gob.igm.rrhh.consultorio.persistence.converter.SexoConverterJPA;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

@Entity
@Access(AccessType.FIELD)
@Table(
    name = "T_DAT_EMPLEADO",
    schema = "RH",
    uniqueConstraints = @UniqueConstraint(name = "T_DAT_EMPLEADO_U01", columnNames = "NO_CEDULA")
)
public class DatEmpleado implements Serializable {

    private static final long serialVersionUID = 1L;

    // PK (NO autogenerado en el DDL)
    @Id
    @Column(name = "NO_PERSONA", nullable = false, precision = 6, scale = 0)
    private Integer noPersona;

    @Column(name = "NO_RELIG", precision = 2, scale = 0)
    private Integer noRelig;

    @Column(name = "NO_SEG", precision = 2, scale = 0)
    private Integer noSeg;

    @Column(name = "NO_PROVEEDOR", precision = 10, scale = 0)
    private Long noProveedor;

    @Column(name = "CODIGO", length = 10)
    private String codigo;

    @Column(name = "NO_CEDULA", length = 10, nullable = false)
    private String noCedula;

    @Column(name = "PRI_APELLIDO", length = 20)
    private String priApellido;

    @Column(name = "SEG_APELLIDO", length = 20)
    private String segApellido;

    @Column(name = "NOMBRES", length = 40)
    private String nombres;

    @Column(name = "NOMBRE_C", length = 81)
    private String nombreC;

    @Column(name = "LIB_MILITAR", length = 15)
    private String libMilitar;

    @Column(name = "SEGURO_SOCIAL", length = 15)
    private String seguroSocial;

    @Convert(converter = SexoConverterJPA.class)
    @Column(name = "SEXO", length = 1)
    private Sexo sexo;

    @Convert(converter = EstadoCivilConverterJPA.class)
    @Column(name = "EST_CIVIL", length = 1)
    private EstadoCivil estCivil;

    @Convert(converter = GrupoSangreConverter.class)
    @Column(name = "G_SANGRE", length = 3)
    private GrupoSangre grupoSangre;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_NACIMIENTO")
    private Date fNacimiento;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_MUERTE")
    private Date fMuerte;

    @Column(name = "NO_LICENCIA", precision = 3, scale = 0)
    private Integer noLicencia;

    @Column(name = "NO_PROFESION", precision = 4, scale = 0)
    private Integer noProfesion;

    @Column(name = "DIRECCION", length = 200)
    private String direccion;

    @Column(name = "TELEFONO", length = 16)
    private String telefono;

    @Column(name = "TIPO", length = 1)
    private String tipo;

    @Column(name = "FOTO", length = 50)
    private String foto;

    @Column(name = "COLOR_PIEL", length = 1)
    private String colorPiel;

    @Column(name = "COLOR_CABELLO", length = 1)
    private String colorCabello;

    @Column(name = "COLOR_OJOS", length = 1)
    private String colorOjos;

    @Column(name = "ESTATURA", precision = 3, scale = 2)
    private BigDecimal estatura;

    @Column(name = "PESO", precision = 5, scale = 2)
    private BigDecimal peso;

    @Column(name = "TALLA_CAMISA", precision = 5, scale = 2)
    private BigDecimal tallaCamisa;

    @Column(name = "TALLA_PANTALON", precision = 5, scale = 2)
    private BigDecimal tallaPantalon;

    @Column(name = "NO_CALZADO", precision = 2, scale = 0)
    private Integer noCalzado;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_INGRESO")
    private Date fIngreso;

    @Column(name = "TELEFONO2", length = 16)
    private String telefono2;

    @Column(name = "L_VIV_PROPIA", precision = 1, scale = 0)
    private Integer lVivPropia;

    @Column(name = "ALERGIA", length = 100)
    private String alergia;

    @Column(name = "NO_LOC_NACE", precision = 6, scale = 0)
    private Integer noLocNace;

    @Column(name = "NO_LOC_DIR", precision = 6, scale = 0)
    private Integer noLocDir;

    @Column(name = "NO_CABEZA", precision = 5, scale = 2)
    private BigDecimal noCabeza;

    @Column(name = "L_USA_LENTES", precision = 1, scale = 0)
    private Integer lUsaLentes;

    @Column(name = "COMISARIATO", length = 1)
    private String comisariato;

    @Temporal(TemporalType.DATE)
    @Column(name = "F_REINGRESO")
    private Date fReingreso;

    @Column(name = "ALIAS_BASE_DATOS", length = 30)
    private String aliasBaseDatos;

    @Column(name = "PIE_FIRMA", length = 50)
    private String pieFirma;

    @Column(name = "NO_DIREC", length = 10)
    private String noDirec;

    @Column(name = "NIVEL", precision = 2, scale = 0)
    private Integer nivel;

    @Column(name = "CARGO_LOSSCA", length = 50)
    private String cargoLossca;

    @Column(name = "PATRONAL", precision = 14, scale = 2)
    private BigDecimal patronal;

    @Column(name = "PERSONAL", precision = 14, scale = 2)
    private BigDecimal personal;

    @Column(name = "IECE", precision = 14, scale = 2)
    private BigDecimal iece;

    @Column(name = "PROCESO", length = 100)
    private String proceso;

    @Column(name = "SUBPROCESO", length = 100)
    private String subproceso;

    @Column(name = "PARTIDA_PRESUPUESTARIA", length = 60)
    private String partidaPresupuestaria;

    @Column(name = "GESTION", length = 100)
    private String gestion;

    @Column(name = "UNIDAD", length = 100)
    private String unidad;

    @Column(name = "EMAIL", length = 60)
    private String email;

    @Column(name = "L_DISCAPACIDAD", precision = 1, scale = 0)
    private Integer lDiscapacidad;

    @Column(name = "NO_CONADIS", length = 10)
    private String noConadis;

    @Column(name = "ID_NACIONALIDAD", precision = 3, scale = 0)
    private Integer idNacionalidad;

    @Column(name = "AUTOIDENTIFICACION_ETNICA", length = 35)
    private String autoidentificacionEtnica;

    @Column(name = "NACIONALIDAD_INDIGENA", length = 10)
    private String nacionalidadIndigena;

    @Column(name = "L_CATASTROFICA", precision = 1, scale = 0)
    private Integer lCatastrofica;

    @Column(name = "NO_CONADIS_CATASTROFICA", length = 10)
    private String noConadisCatastrofica;

    @Column(name = "CALLE_SECUNDARIA", length = 100)
    private String calleSecundaria;

    @Column(name = "REFERENCIA", length = 200)
    private String referencia;

    @Column(name = "EXTENSION", length = 10)
    private String extension;

    @Column(name = "CONTACTO_APELLIDOS", length = 50)
    private String contactoApellidos;

    @Column(name = "CONTACTO_NOMBRES", length = 50)
    private String contactoNombres;

    @Column(name = "CONTACTO_TELEFONO", length = 20)
    private String contactoTelefono;

    @Column(name = "CONTACTO_CELULAR", length = 10)
    private String contactoCelular;

    @Column(name = "EMAIL_INSTITUCIONAL", length = 60)
    private String emailInstitucional;

    @Column(name = "ESTADO", length = 10)
    private String estado;

    // ===== Campo derivado (NO persistente) =====
    @Transient
    public String getApellidos() {
        String a1 = priApellido == null ? "" : priApellido;
        String a2 = segApellido == null ? "" : segApellido;
        return (a1 + " " + a2).trim();
    }

    @PrePersist
    @PreUpdate
    public void normalizar() {
        if (noCedula != null) noCedula = noCedula.trim();
        if (email != null) email = email.trim().toLowerCase();
        if (emailInstitucional != null) emailInstitucional = emailInstitucional.trim().toLowerCase();
    }

    // ==========================
    // Constructores
    // ==========================
    
    // Constructor vacío (requerido por JPA)
    public DatEmpleado() {
    }

    // Constructor completo (Reemplaza @AllArgsConstructor)
    public DatEmpleado(Integer noPersona, Integer noRelig, Integer noSeg, Long noProveedor, 
                       String codigo, String noCedula, String priApellido, String segApellido, 
                       String nombres, String nombreC, String libMilitar, String seguroSocial, 
                       Sexo sexo, EstadoCivil estCivil, GrupoSangre grupoSangre, Date fNacimiento, 
                       Date fMuerte, Integer noLicencia, Integer noProfesion, String direccion, 
                       String telefono, String tipo, String foto, String colorPiel, String colorCabello, 
                       String colorOjos, BigDecimal estatura, BigDecimal peso, BigDecimal tallaCamisa, 
                       BigDecimal tallaPantalon, Integer noCalzado, Date fIngreso, String telefono2, 
                       Integer lVivPropia, String alergia, Integer noLocNace, Integer noLocDir, 
                       BigDecimal noCabeza, Integer lUsaLentes, String comisariato, Date fReingreso, 
                       String aliasBaseDatos, String pieFirma, String noDirec, Integer nivel, 
                       String cargoLossca, BigDecimal patronal, BigDecimal personal, BigDecimal iece, 
                       String proceso, String subproceso, String partidaPresupuestaria, String gestion, 
                       String unidad, String email, Integer lDiscapacidad, String noConadis, 
                       Integer idNacionalidad, String autoidentificacionEtnica, String nacionalidadIndigena, 
                       Integer lCatastrofica, String noConadisCatastrofica, String calleSecundaria, 
                       String referencia, String extension, String contactoApellidos, 
                       String contactoNombres, String contactoTelefono, String contactoCelular, 
                       String emailInstitucional, String estado) {
        this.noPersona = noPersona;
        this.noRelig = noRelig;
        this.noSeg = noSeg;
        this.noProveedor = noProveedor;
        this.codigo = codigo;
        this.noCedula = noCedula;
        this.priApellido = priApellido;
        this.segApellido = segApellido;
        this.nombres = nombres;
        this.nombreC = nombreC;
        this.libMilitar = libMilitar;
        this.seguroSocial = seguroSocial;
        this.sexo = sexo;
        this.estCivil = estCivil;
        this.grupoSangre = grupoSangre;
        this.fNacimiento = fNacimiento;
        this.fMuerte = fMuerte;
        this.noLicencia = noLicencia;
        this.noProfesion = noProfesion;
        this.direccion = direccion;
        this.telefono = telefono;
        this.tipo = tipo;
        this.foto = foto;
        this.colorPiel = colorPiel;
        this.colorCabello = colorCabello;
        this.colorOjos = colorOjos;
        this.estatura = estatura;
        this.peso = peso;
        this.tallaCamisa = tallaCamisa;
        this.tallaPantalon = tallaPantalon;
        this.noCalzado = noCalzado;
        this.fIngreso = fIngreso;
        this.telefono2 = telefono2;
        this.lVivPropia = lVivPropia;
        this.alergia = alergia;
        this.noLocNace = noLocNace;
        this.noLocDir = noLocDir;
        this.noCabeza = noCabeza;
        this.lUsaLentes = lUsaLentes;
        this.comisariato = comisariato;
        this.fReingreso = fReingreso;
        this.aliasBaseDatos = aliasBaseDatos;
        this.pieFirma = pieFirma;
        this.noDirec = noDirec;
        this.nivel = nivel;
        this.cargoLossca = cargoLossca;
        this.patronal = patronal;
        this.personal = personal;
        this.iece = iece;
        this.proceso = proceso;
        this.subproceso = subproceso;
        this.partidaPresupuestaria = partidaPresupuestaria;
        this.gestion = gestion;
        this.unidad = unidad;
        this.email = email;
        this.lDiscapacidad = lDiscapacidad;
        this.noConadis = noConadis;
        this.idNacionalidad = idNacionalidad;
        this.autoidentificacionEtnica = autoidentificacionEtnica;
        this.nacionalidadIndigena = nacionalidadIndigena;
        this.lCatastrofica = lCatastrofica;
        this.noConadisCatastrofica = noConadisCatastrofica;
        this.calleSecundaria = calleSecundaria;
        this.referencia = referencia;
        this.extension = extension;
        this.contactoApellidos = contactoApellidos;
        this.contactoNombres = contactoNombres;
        this.contactoTelefono = contactoTelefono;
        this.contactoCelular = contactoCelular;
        this.emailInstitucional = emailInstitucional;
        this.estado = estado;
    }

    // ==========================
    // Equals y HashCode
    // Basado en noPersona (@EqualsAndHashCode.Include)
    // ==========================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatEmpleado that = (DatEmpleado) o;
        return Objects.equals(noPersona, that.noPersona);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noPersona);
    }

    // ==========================
    // ToString
    // Solo incluye noPersona (@ToString.Include)
    // ==========================
    @Override
    public String toString() {
        return "DatEmpleado{" +
                "noPersona=" + noPersona +
                '}';
    }

    // ==========================
    // Getters y Setters
    // ==========================
    public Integer getNoPersona() {
        return noPersona;
    }

    public void setNoPersona(Integer noPersona) {
        this.noPersona = noPersona;
    }

    public Integer getNoRelig() {
        return noRelig;
    }

    public void setNoRelig(Integer noRelig) {
        this.noRelig = noRelig;
    }

    public Integer getNoSeg() {
        return noSeg;
    }

    public void setNoSeg(Integer noSeg) {
        this.noSeg = noSeg;
    }

    public Long getNoProveedor() {
        return noProveedor;
    }

    public void setNoProveedor(Long noProveedor) {
        this.noProveedor = noProveedor;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public String getLibMilitar() {
        return libMilitar;
    }

    public void setLibMilitar(String libMilitar) {
        this.libMilitar = libMilitar;
    }

    public String getSeguroSocial() {
        return seguroSocial;
    }

    public void setSeguroSocial(String seguroSocial) {
        this.seguroSocial = seguroSocial;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public EstadoCivil getEstCivil() {
        return estCivil;
    }

    public void setEstCivil(EstadoCivil estCivil) {
        this.estCivil = estCivil;
    }

    public GrupoSangre getGrupoSangre() {
        return grupoSangre;
    }

    public void setGrupoSangre(GrupoSangre grupoSangre) {
        this.grupoSangre = grupoSangre;
    }

    public Date getfNacimiento() {
        return fNacimiento;
    }

    public void setfNacimiento(Date fNacimiento) {
        this.fNacimiento = fNacimiento;
    }

    public Date getfMuerte() {
        return fMuerte;
    }

    public void setfMuerte(Date fMuerte) {
        this.fMuerte = fMuerte;
    }

    public Integer getNoLicencia() {
        return noLicencia;
    }

    public void setNoLicencia(Integer noLicencia) {
        this.noLicencia = noLicencia;
    }

    public Integer getNoProfesion() {
        return noProfesion;
    }

    public void setNoProfesion(Integer noProfesion) {
        this.noProfesion = noProfesion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getColorPiel() {
        return colorPiel;
    }

    public void setColorPiel(String colorPiel) {
        this.colorPiel = colorPiel;
    }

    public String getColorCabello() {
        return colorCabello;
    }

    public void setColorCabello(String colorCabello) {
        this.colorCabello = colorCabello;
    }

    public String getColorOjos() {
        return colorOjos;
    }

    public void setColorOjos(String colorOjos) {
        this.colorOjos = colorOjos;
    }

    public BigDecimal getEstatura() {
        return estatura;
    }

    public void setEstatura(BigDecimal estatura) {
        this.estatura = estatura;
    }

    public BigDecimal getPeso() {
        return peso;
    }

    public void setPeso(BigDecimal peso) {
        this.peso = peso;
    }

    public BigDecimal getTallaCamisa() {
        return tallaCamisa;
    }

    public void setTallaCamisa(BigDecimal tallaCamisa) {
        this.tallaCamisa = tallaCamisa;
    }

    public BigDecimal getTallaPantalon() {
        return tallaPantalon;
    }

    public void setTallaPantalon(BigDecimal tallaPantalon) {
        this.tallaPantalon = tallaPantalon;
    }

    public Integer getNoCalzado() {
        return noCalzado;
    }

    public void setNoCalzado(Integer noCalzado) {
        this.noCalzado = noCalzado;
    }

    public Date getfIngreso() {
        return fIngreso;
    }

    public void setfIngreso(Date fIngreso) {
        this.fIngreso = fIngreso;
    }

    public String getTelefono2() {
        return telefono2;
    }

    public void setTelefono2(String telefono2) {
        this.telefono2 = telefono2;
    }

    public Integer getlVivPropia() {
        return lVivPropia;
    }

    public void setlVivPropia(Integer lVivPropia) {
        this.lVivPropia = lVivPropia;
    }

    public String getAlergia() {
        return alergia;
    }

    public void setAlergia(String alergia) {
        this.alergia = alergia;
    }

    public Integer getNoLocNace() {
        return noLocNace;
    }

    public void setNoLocNace(Integer noLocNace) {
        this.noLocNace = noLocNace;
    }

    public Integer getNoLocDir() {
        return noLocDir;
    }

    public void setNoLocDir(Integer noLocDir) {
        this.noLocDir = noLocDir;
    }

    public BigDecimal getNoCabeza() {
        return noCabeza;
    }

    public void setNoCabeza(BigDecimal noCabeza) {
        this.noCabeza = noCabeza;
    }

    public Integer getlUsaLentes() {
        return lUsaLentes;
    }

    public void setlUsaLentes(Integer lUsaLentes) {
        this.lUsaLentes = lUsaLentes;
    }

    public String getComisariato() {
        return comisariato;
    }

    public void setComisariato(String comisariato) {
        this.comisariato = comisariato;
    }

    public Date getfReingreso() {
        return fReingreso;
    }

    public void setfReingreso(Date fReingreso) {
        this.fReingreso = fReingreso;
    }

    public String getAliasBaseDatos() {
        return aliasBaseDatos;
    }

    public void setAliasBaseDatos(String aliasBaseDatos) {
        this.aliasBaseDatos = aliasBaseDatos;
    }

    public String getPieFirma() {
        return pieFirma;
    }

    public void setPieFirma(String pieFirma) {
        this.pieFirma = pieFirma;
    }

    public String getNoDirec() {
        return noDirec;
    }

    public void setNoDirec(String noDirec) {
        this.noDirec = noDirec;
    }

    public Integer getNivel() {
        return nivel;
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }

    public String getCargoLossca() {
        return cargoLossca;
    }

    public void setCargoLossca(String cargoLossca) {
        this.cargoLossca = cargoLossca;
    }

    public BigDecimal getPatronal() {
        return patronal;
    }

    public void setPatronal(BigDecimal patronal) {
        this.patronal = patronal;
    }

    public BigDecimal getPersonal() {
        return personal;
    }

    public void setPersonal(BigDecimal personal) {
        this.personal = personal;
    }

    public BigDecimal getIece() {
        return iece;
    }

    public void setIece(BigDecimal iece) {
        this.iece = iece;
    }

    public String getProceso() {
        return proceso;
    }

    public void setProceso(String proceso) {
        this.proceso = proceso;
    }

    public String getSubproceso() {
        return subproceso;
    }

    public void setSubproceso(String subproceso) {
        this.subproceso = subproceso;
    }

    public String getPartidaPresupuestaria() {
        return partidaPresupuestaria;
    }

    public void setPartidaPresupuestaria(String partidaPresupuestaria) {
        this.partidaPresupuestaria = partidaPresupuestaria;
    }

    public String getGestion() {
        return gestion;
    }

    public void setGestion(String gestion) {
        this.gestion = gestion;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getlDiscapacidad() {
        return lDiscapacidad;
    }

    public void setlDiscapacidad(Integer lDiscapacidad) {
        this.lDiscapacidad = lDiscapacidad;
    }

    public String getNoConadis() {
        return noConadis;
    }

    public void setNoConadis(String noConadis) {
        this.noConadis = noConadis;
    }

    public Integer getIdNacionalidad() {
        return idNacionalidad;
    }

    public void setIdNacionalidad(Integer idNacionalidad) {
        this.idNacionalidad = idNacionalidad;
    }

    public String getAutoidentificacionEtnica() {
        return autoidentificacionEtnica;
    }

    public void setAutoidentificacionEtnica(String autoidentificacionEtnica) {
        this.autoidentificacionEtnica = autoidentificacionEtnica;
    }

    public String getNacionalidadIndigena() {
        return nacionalidadIndigena;
    }

    public void setNacionalidadIndigena(String nacionalidadIndigena) {
        this.nacionalidadIndigena = nacionalidadIndigena;
    }

    public Integer getlCatastrofica() {
        return lCatastrofica;
    }

    public void setlCatastrofica(Integer lCatastrofica) {
        this.lCatastrofica = lCatastrofica;
    }

    public String getNoConadisCatastrofica() {
        return noConadisCatastrofica;
    }

    public void setNoConadisCatastrofica(String noConadisCatastrofica) {
        this.noConadisCatastrofica = noConadisCatastrofica;
    }

    public String getCalleSecundaria() {
        return calleSecundaria;
    }

    public void setCalleSecundaria(String calleSecundaria) {
        this.calleSecundaria = calleSecundaria;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getContactoApellidos() {
        return contactoApellidos;
    }

    public void setContactoApellidos(String contactoApellidos) {
        this.contactoApellidos = contactoApellidos;
    }

    public String getContactoNombres() {
        return contactoNombres;
    }

    public void setContactoNombres(String contactoNombres) {
        this.contactoNombres = contactoNombres;
    }

    public String getContactoTelefono() {
        return contactoTelefono;
    }

    public void setContactoTelefono(String contactoTelefono) {
        this.contactoTelefono = contactoTelefono;
    }

    public String getContactoCelular() {
        return contactoCelular;
    }

    public void setContactoCelular(String contactoCelular) {
        this.contactoCelular = contactoCelular;
    }

    public String getEmailInstitucional() {
        return emailInstitucional;
    }

    public void setEmailInstitucional(String emailInstitucional) {
        this.emailInstitucional = emailInstitucional;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}