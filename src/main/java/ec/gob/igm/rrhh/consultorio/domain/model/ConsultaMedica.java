package ec.gob.igm.rrhh.consultorio.domain.model;





import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "CONSULTA_MEDICA", schema = "CONSULTORIO")
public class ConsultaMedica implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(
        name = "CONSULTA_GEN",
        sequenceName = "CONSULTORIO.SQ_CONSULTA",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONSULTA_GEN")
    @Column(name = "ID_CONSULTA", nullable = false)
    private Long idConsulta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NO_PERSONA", nullable = false)
    private DatEmpleado empleado;

    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_CONSULTA", nullable = false)
    private Date fechaConsulta;

    @Column(name = "MOTIVO_CONSULTA", length = 1000)
    private String motivoConsulta;

    @Column(name = "ENFERMEDAD_ACTUAL", length = 2000)
    private String enfermedadActual;

    @Column(name = "EXAMEN_FISICO", length = 2000)
    private String examenFisico;

    // Señala a la tabla maestra de signos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SIGNOS")
    private SignosVitales signos;

    @Column(name = "MEDICO_NOMBRE", length = 150)
    private String medicoNombre;

    @Column(name = "MEDICO_CODIGO", length = 50)
    private String medicoCodigo;

    @Column(name = "ESTADO", length = 20, nullable = false)
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

    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsultaDiagnostico> diagnosticos;


    // Constructor vacío (requerido por JPA)
    public ConsultaMedica() {
        this.diagnosticos = new ArrayList<>();
    }

    // Constructor completo (Reemplaza @AllArgsConstructor)
    public ConsultaMedica(Long idConsulta, DatEmpleado empleado, Date fechaConsulta, String motivoConsulta,
                          String enfermedadActual, String examenFisico, SignosVitales signos,
                          String medicoNombre, String medicoCodigo, String estado,
                          Date fechaCreacion, String usrCreacion, Date fechaActualizacion,
                          String usrActualizacion, List<ConsultaDiagnostico> diagnosticos) {
        this.idConsulta = idConsulta;
        this.empleado = empleado;
        this.fechaConsulta = fechaConsulta;
        this.motivoConsulta = motivoConsulta;
        this.enfermedadActual = enfermedadActual;
        this.examenFisico = examenFisico;
        this.signos = signos;
        this.medicoNombre = medicoNombre;
        this.medicoCodigo = medicoCodigo;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.usrCreacion = usrCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.usrActualizacion = usrActualizacion;
        this.diagnosticos = diagnosticos;
        // Evitar NPE si la lista es nula
        if (this.diagnosticos == null) {
            this.diagnosticos = new ArrayList<>();
        }
    }

    @Override
    public String toString() {
        return "ConsultaMedica{" +
                "idConsulta=" + idConsulta +
                ", fechaConsulta=" + fechaConsulta +
                ", motivoConsulta='" + motivoConsulta + '\'' +
                ", enfermedadActual='" + enfermedadActual + '\'' +
                ", examenFisico='" + examenFisico + '\'' +
                ", medicoNombre='" + medicoNombre + '\'' +
                ", medicoCodigo='" + medicoCodigo + '\'' +
                ", estado='" + estado + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", usrCreacion='" + usrCreacion + '\'' +
                ", fechaActualizacion=" + fechaActualizacion +
                ", usrActualizacion='" + usrActualizacion + '\'' +
                '}';
    }

    public Long getIdConsulta() {
        return idConsulta;
    }

    public void setIdConsulta(Long idConsulta) {
        this.idConsulta = idConsulta;
    }

    public DatEmpleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(DatEmpleado empleado) {
        this.empleado = empleado;
    }

    public Date getFechaConsulta() {
        return fechaConsulta;
    }

    public void setFechaConsulta(Date fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }

    public String getMotivoConsulta() {
        return motivoConsulta;
    }

    public void setMotivoConsulta(String motivoConsulta) {
        this.motivoConsulta = motivoConsulta;
    }

    public String getEnfermedadActual() {
        return enfermedadActual;
    }

    public void setEnfermedadActual(String enfermedadActual) {
        this.enfermedadActual = enfermedadActual;
    }

    public String getExamenFisico() {
        return examenFisico;
    }

    public void setExamenFisico(String examenFisico) {
        this.examenFisico = examenFisico;
    }

    public SignosVitales getSignos() {
        return signos;
    }

    public void setSignos(SignosVitales signos) {
        this.signos = signos;
    }

    public String getMedicoNombre() {
        return medicoNombre;
    }

    public void setMedicoNombre(String medicoNombre) {
        this.medicoNombre = medicoNombre;
    }

    public String getMedicoCodigo() {
        return medicoCodigo;
    }

    public void setMedicoCodigo(String medicoCodigo) {
        this.medicoCodigo = medicoCodigo;
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

    public List<ConsultaDiagnostico> getDiagnosticos() {
        return diagnosticos;
    }

    public void setDiagnosticos(List<ConsultaDiagnostico> diagnosticos) {
        this.diagnosticos = diagnosticos;
    }
}
