package ec.gob.igm.rrhh.consultorio.domain.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "FICHA_OCUPACIONAL", schema = "CONSULTORIO")
@Access(AccessType.FIELD)
public class FichaOcupacional implements Serializable {

    private static final long serialVersionUID = 1L;

    // PK
    @Id
    @SequenceGenerator(
            name = "FICHA_OCUP_GEN",
            sequenceName = "CONSULTORIO.SQ_FICHA",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FICHA_OCUP_GEN")
    @Column(name = "ID_FICHA", nullable = false)
    private Long idFicha;

    // Relaciones (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NO_PERSONA")
    private DatEmpleado empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PERSONA_AUX")
    private PersonaAux personaAux;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SIGNOS")
    private SignosVitales signos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COD_CIE10_PPAL", referencedColumnName = "CODIGO")
    private Cie10 cie10Principal;

    // Campos generales
    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_EVALUACION", nullable = false)
    private Date fechaEvaluacion;

    @Column(name = "TIPO_EVALUACION", length = 20, nullable = false)
    private String tipoEvaluacion;

    @Column(name = "OBSERVACION", length = 2000)
    private String observacion;

    // Atención prioritaria (S/N)
    @Column(name = "AP_EMBARAZADA", length = 1)
    private String apEmbarazada;

    @Column(name = "AP_DISCAPACIDAD", length = 1)
    private String apDiscapacidad;

    @Column(name = "AP_CATASTROFICA", length = 1)
    private String apCatastrofica;

    @Column(name = "AP_LACTANCIA", length = 1)
    private String apLactancia;

    @Column(name = "AP_ADULTO_MAYOR", length = 1)
    private String apAdultoMayor;
// Discapacidad (detalle)
    @Column(name = "DIS_TIPO", length = 20)
    private String disTipo; // Fisico, Auditivo, Intelectual, Lenguaje, Visual, Psicosocial

    @Column(name = "DIS_DESCRIPCION", length = 1000)
    private String disDescripcion;

    @Column(name = "DIS_PORCENTAJE")
    private Integer disPorcentaje;

// Enfermedad Catastrófica, Huérfana y Rara (detalle)
    @Column(name = "CAT_DIAGNOSTICO", length = 2000)
    private String catDiagnostico;

    @Column(name = "CAT_CALIFICADA", length = 1)
    private String catCalificada; // S/N

    // Antecedentes
    @Column(name = "ANT_CLINICO_QUIR", length = 2000)
    private String antClinicoQuir;

    @Column(name = "ANT_FAMILIARES", length = 2000)
    private String antFamiliares;

    @Column(name = "CONDICION_ESPECIAL", length = 500)
    private String condicionEspecial;

    @Column(name = "AUTORIZA_TRANSFUSION", length = 2)
    private String autorizaTransfusion;

    @Column(name = "TRAT_HORMONAL", length = 2)
    private String tratHormonal;

    @Column(name = "TRAT_HORMONAL_CUAL", length = 500)
    private String tratHormonalCual;

    @Column(name = "EXAM_REPRO_MASC", length = 500)
    private String examReproMasc;

    @Column(name = "TIEMPO_REPRO_MASC")
    private Integer tiempoReproMasc;

    // Gineco-obstétricos
    @Temporal(TemporalType.DATE)
    @Column(name = "FUM")
    private Date fum;

    @Column(name = "GESTAS")
    private Integer gestas;

    @Column(name = "PARTOS")
    private Integer partos;

    @Column(name = "CESAREAS")
    private Integer cesareas;

    @Column(name = "ABORTOS")
    private Integer abortos;

    @Column(name = "PLANIFICACION", length = 50)
    private String planificacion;

    @Column(name = "PLANIFICACION_CUAL", length = 200)
    private String planificacionCual;

    @Column(name = "GINECO_EXAMEN1", length = 200)
    private String ginecoExamen1;

    @Column(name = "GINECO_TIEMPO1", length = 50)
    private String ginecoTiempo1;

    @Column(name = "GINECO_RESULTADO1", length = 500)
    private String ginecoResultado1;
    @Column(name = "GINECO_EXAMEN2", length = 200)
    private String ginecoExamen2;

    @Column(name = "GINECO_TIEMPO2", length = 50)
    private String ginecoTiempo2;

    @Column(name = "GINECO_RESULTADO2", length = 500)
    private String ginecoResultado2;

    @Column(name = "GINECO_OBSERVACION", length = 2000)
    private String ginecoObservacion;

// ... (similar para las otras)
    // Consumo / Vida / Condiciones
    // TABACO
    @Column(name = "TAB_CONS_MESES")
    private Integer tabConsMeses;

    @Column(name = "TAB_EX_CONS", length = 1)
    private String tabExCons;

    @Column(name = "TAB_ABS_MESES")
    private Integer tabAbsMeses;

    @Column(name = "TAB_NO_CONS", length = 1)
    private String tabNoCons;

    // ALCOHOL
    @Column(name = "ALC_CONS_MESES")
    private Integer alcConsMeses;

    @Column(name = "ALC_EX_CONS", length = 1)
    private String alcExCons;

    @Column(name = "ALC_ABS_MESES")
    private Integer alcAbsMeses;

    @Column(name = "ALC_NO_CONS", length = 1)
    private String alcNoCons;

    // OTRAS
    @Column(name = "OTR_CUAL", length = 200)
    private String otrCual;

    @Column(name = "OTR_CONS_MESES")
    private Integer otrConsMeses;

    @Column(name = "OTR_EX_CONS", length = 1)
    private String otrExCons;

    @Column(name = "OTR_ABS_MESES")
    private Integer otrAbsMeses;

    @Column(name = "OTR_NO_CONS", length = 1)
    private String otrNoCons;

    // ACTIVIDAD FISICA (3) DDL: 200
    @Column(name = "AF_CUAL_1", length = 200)
    private String afCual1;

    @Column(name = "AF_TIEMPO_1", length = 200)
    private String afTiempo1;

    @Column(name = "AF_CUAL_2", length = 200)
    private String afCual2;

    @Column(name = "AF_TIEMPO_2", length = 200)
    private String afTiempo2;

    @Column(name = "AF_CUAL_3", length = 200)
    private String afCual3;

    @Column(name = "AF_TIEMPO_3", length = 200)
    private String afTiempo3;

    // MEDICACION (3) DDL: 200
    @Column(name = "MED_CUAL_1", length = 200)
    private String medCual1;

    @Column(name = "MED_CANT_1")
    private Integer medCant1;

    @Column(name = "MED_CUAL_2", length = 200)
    private String medCual2;

    @Column(name = "MED_CANT_2")
    private Integer medCant2;

    @Column(name = "MED_CUAL_3", length = 200)
    private String medCual3;

    @Column(name = "MED_CANT_3")
    private Integer medCant3;

    @Column(name = "OBS_CONSUMO_VIDA_COND", length = 2000)
    private String obsConsumoVidaCond;

    @Column(name = "INST_SISTEMA", length = 200)
    private String instSistema;

    @Column(name = "RUC_ESTABLECIMIENTO", length = 20)
    private String rucEstablecimiento;

    @Column(name = "CIIU", length = 20)
    private String ciiu;

    @Column(name = "ESTABLECIMIENTO_CT", length = 250)
    private String establecimientoCt;

    @Column(name = "NO_HISTORIA_CLINICA", length = 30)
    private String noHistoriaClinica;

    @Column(name = "NO_ARCHIVO", length = 30)
    private String noArchivo;

    @Column(name = "AREA_TRABAJO", length = 200)
    private String areaTrabajo;

    @Column(name = "PUESTO_TRABAJO_TXT", length = 200)
    private String puestoTrabajoTxt;

    @Column(name = "EXTRA_LAB_DESC", length = 2000)
    private String extraLabDesc;

    @Temporal(TemporalType.DATE)
    @Column(name = "EXTRA_LAB_FECHA")
    private Date extraLabFecha;

    @Column(name = "ENFERMEDAD_PROB_ACTUAL", length = 2000)
    private String enfermedadProbActual;

    @Column(name = "OBS_EXAMEN_FISICO_REGIONAL", length = 2000)
    private String obsExamenFisicoRegional;

    @Column(name = "OBS_EXAMEN_FISICO_REG", length = 2000)
    private String obsExamenFisicoReg;

    @Column(name = "EXF_PIEL_CICATRICES", length = 1, nullable = false)
    private String exfPielCicatrices;

    @Column(name = "EXF_OJOS_PARPADOS", length = 1, nullable = false)
    private String exfOjosParpados;

    @Column(name = "EXF_OJOS_CONJUNTIVAS", length = 1, nullable = false)
    private String exfOjosConjuntivas;

    @Column(name = "EXF_OJOS_PUPILAS", length = 1, nullable = false)
    private String exfOjosPupilas;

    @Column(name = "EXF_OJOS_CORNEA", length = 1, nullable = false)
    private String exfOjosCornea;

    @Column(name = "EXF_OJOS_MOTILIDAD", length = 1, nullable = false)
    private String exfOjosMotilidad;

    @Column(name = "EXF_OIDO_CONDUCTO", length = 1, nullable = false)
    private String exfOidoConducto;

    @Column(name = "EXF_OIDO_PABELLON", length = 1, nullable = false)
    private String exfOidoPabellon;

    @Column(name = "EXF_OIDO_TIMPANOS", length = 1, nullable = false)
    private String exfOidoTimpanos;

    @Column(name = "EXF_ORO_LABIOS", length = 1, nullable = false)
    private String exfOroLabios;

    @Column(name = "EXF_ORO_LENGUA", length = 1, nullable = false)
    private String exfOroLengua;

    @Column(name = "EXF_ORO_FARINGE", length = 1, nullable = false)
    private String exfOroFaringe;

    @Column(name = "EXF_ORO_AMIGDALAS", length = 1, nullable = false)
    private String exfOroAmigdalas;

    @Column(name = "EXF_ORO_DENTADURA", length = 1, nullable = false)
    private String exfOroDentadura;

    @Column(name = "EXF_NARIZ_TABIQUE", length = 1, nullable = false)
    private String exfNarizTabique;

    @Column(name = "EXF_NARIZ_CORNETES", length = 1, nullable = false)
    private String exfNarizCornetes;

    @Column(name = "EXF_NARIZ_MUCOSAS", length = 1, nullable = false)
    private String exfNarizMucosas;

    @Column(name = "EXF_NARIZ_SENOS_PARANASA", length = 1, nullable = false)
    private String exfNarizSenosParanasa;

    @Column(name = "EXF_CUELLO_TIROIDES_MASAS", length = 1, nullable = false)
    private String exfCuelloTiroidesMasas;

    @Column(name = "EXF_CUELLO_MOVILIDAD", length = 1, nullable = false)
    private String exfCuelloMovilidad;

    @Column(name = "EXF_TORAX_MAMAS", length = 1, nullable = false)
    private String exfToraxMamas;

    @Column(name = "EXF_TORAX_PULMONES", length = 1, nullable = false)
    private String exfToraxPulmones;

    @Column(name = "EXF_TORAX_CORAZON", length = 1, nullable = false)
    private String exfToraxCorazon;

    @Column(name = "EXF_TORAX_PARRILLA_COSTAL", length = 1, nullable = false)
    private String exfToraxParrillaCostal;

    @Column(name = "EXF_ABD_VISCERAS", length = 1, nullable = false)
    private String exfAbdVisceras;

    @Column(name = "EXF_ABD_PARED_ABDOMINAL", length = 1, nullable = false)
    private String exfAbdParedAbdominal;

    @Column(name = "EXF_COL_FLEXIBILIDAD", length = 1, nullable = false)
    private String exfColFlexibilidad;

    @Column(name = "EXF_COL_DESVIACION", length = 1, nullable = false)
    private String exfColDesviacion;

    @Column(name = "EXF_COL_DOLOR", length = 1, nullable = false)
    private String exfColDolor;

    @Column(name = "EXF_PELVIS_PELVIS", length = 1, nullable = false)
    private String exfPelvisPelvis;

    @Column(name = "EXF_PELVIS_GENITALES", length = 1, nullable = false)
    private String exfPelvisGenitales;

    @Column(name = "EXF_EXT_VASCULAR", length = 1, nullable = false)
    private String exfExtVascular;

    @Column(name = "EXF_EXT_MIEMBROS_SUP", length = 1, nullable = false)
    private String exfExtMiembrosSup;

    @Column(name = "EXF_EXT_MIEMBROS_INF", length = 1, nullable = false)
    private String exfExtMiembrosInf;

    @Column(name = "EXF_NEURO_FUERZA", length = 1, nullable = false)
    private String exfNeuroFuerza;

    @Column(name = "EXF_NEURO_SENSIBILIDAD", length = 1, nullable = false)
    private String exfNeuroSensibilidad;

    @Column(name = "EXF_NEURO_MARCHA", length = 1, nullable = false)
    private String exfNeuroMarcha;

    @Column(name = "EXF_NEURO_REFLEJOS", length = 1, nullable = false)
    private String exfNeuroReflejos;

    @Column(name = "N_RET_EVAL", length = 1, nullable = false)
    private String nRetEval;

    @Column(name = "N_RET_REL_TRAB", length = 1, nullable = false)
    private String nRetRelTrab;

    @Column(name = "N_RET_OBS", length = 2000)
    private String nRetObs;

    @Column(name = "APTITUD_SEL", length = 20)
    private String aptitudSel;

    @Column(name = "DETALLE_OBS", length = 2000)
    private String detalleObs;

    @Column(name = "RECOMENDACIONES", length = 2000)
    private String recomendaciones;

    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_EMISION")
    private Date fechaEmision;

    @Column(name = "MEDICO_NOMBRE", length = 150)
    private String medicoNombre;

    @Column(name = "MEDICO_CODIGO", length = 50)
    private String medicoCodigo;

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

    public FichaOcupacional() {
    }

    // Constructor completo (Reemplaza @AllArgsConstructor)
    public FichaOcupacional(Long idFicha, DatEmpleado empleado, PersonaAux personaAux,
            SignosVitales signos, Cie10 cie10Principal, Date fechaEvaluacion,
            String tipoEvaluacion, String observacion, String apEmbarazada,
            String apDiscapacidad, String apCatastrofica, String apLactancia,
            String apAdultoMayor, String antClinicoQuir, String antFamiliares,
            String condicionEspecial, String autorizaTransfusion, String tratHormonal,
            String tratHormonalCual, String examReproMasc, Integer tiempoReproMasc,
            Date fum, Integer gestas, Integer partos, Integer cesareas,
            Integer abortos, String planificacion, String planificacionCual,
            Integer tabConsMeses, String tabExCons, Integer tabAbsMeses,
            String tabNoCons, Integer alcConsMeses, String alcExCons,
            Integer alcAbsMeses, String alcNoCons, String otrCual,
            Integer otrConsMeses, String otrExCons, Integer otrAbsMeses,
            String otrNoCons, String afCual1, String afTiempo1, String afCual2,
            String afTiempo2, String afCual3, String afTiempo3, String medCual1,
            Integer medCant1, String medCual2, Integer medCant2, String medCual3,
            Integer medCant3, String obsConsumoVidaCond, String instSistema,
            String rucEstablecimiento, String ciiu, String establecimientoCt,
            String noHistoriaClinica, String noArchivo, String areaTrabajo,
            String puestoTrabajoTxt, String extraLabDesc, Date extraLabFecha,
            String enfermedadProbActual, String obsExamenFisicoRegional,
            String obsExamenFisicoReg, String exfPielCicatrices, String exfOjosParpados,
            String exfOjosConjuntivas, String exfOjosPupilas, String exfOjosCornea,
            String exfOjosMotilidad, String exfOidoConducto, String exfOidoPabellon,
            String exfOidoTimpanos, String exfOroLabios, String exfOroLengua,
            String exfOroFaringe, String exfOroAmigdalas, String exfOroDentadura,
            String exfNarizTabique, String exfNarizCornetes, String exfNarizMucosas,
            String exfNarizSenosParanasa, String exfCuelloTiroidesMasas,
            String exfCuelloMovilidad, String exfToraxMamas, String exfToraxPulmones,
            String exfToraxCorazon, String exfToraxParrillaCostal, String exfAbdVisceras,
            String exfAbdParedAbdominal, String exfColFlexibilidad, String exfColDesviacion,
            String exfColDolor, String exfPelvisPelvis, String exfPelvisGenitales,
            String exfExtVascular, String exfExtMiembrosSup, String exfExtMiembrosInf,
            String exfNeuroFuerza, String exfNeuroSensibilidad, String exfNeuroMarcha,
            String exfNeuroReflejos, String nRetEval, String nRetRelTrab,
            String nRetObs, String aptitudSel, String detalleObs, String recomendaciones,
            Date fechaEmision, String medicoNombre, String medicoCodigo, String estado,
            Date fechaCreacion, String usrCreacion, Date fechaActualizacion,
            String usrActualizacion) {
        this.idFicha = idFicha;
        this.empleado = empleado;
        this.personaAux = personaAux;
        this.signos = signos;
        this.cie10Principal = cie10Principal;
        this.fechaEvaluacion = fechaEvaluacion;
        this.tipoEvaluacion = tipoEvaluacion;
        this.observacion = observacion;
        this.apEmbarazada = apEmbarazada;
        this.apDiscapacidad = apDiscapacidad;
        this.apCatastrofica = apCatastrofica;
        this.apLactancia = apLactancia;
        this.apAdultoMayor = apAdultoMayor;
        this.antClinicoQuir = antClinicoQuir;
        this.antFamiliares = antFamiliares;
        this.condicionEspecial = condicionEspecial;
        this.autorizaTransfusion = autorizaTransfusion;
        this.tratHormonal = tratHormonal;
        this.tratHormonalCual = tratHormonalCual;
        this.examReproMasc = examReproMasc;
        this.tiempoReproMasc = tiempoReproMasc;
        this.fum = fum;
        this.gestas = gestas;
        this.partos = partos;
        this.cesareas = cesareas;
        this.abortos = abortos;
        this.planificacion = planificacion;
        this.planificacionCual = planificacionCual;
        this.tabConsMeses = tabConsMeses;
        this.tabExCons = tabExCons;
        this.tabAbsMeses = tabAbsMeses;
        this.tabNoCons = tabNoCons;
        this.alcConsMeses = alcConsMeses;
        this.alcExCons = alcExCons;
        this.alcAbsMeses = alcAbsMeses;
        this.alcNoCons = alcNoCons;
        this.otrCual = otrCual;
        this.otrConsMeses = otrConsMeses;
        this.otrExCons = otrExCons;
        this.otrAbsMeses = otrAbsMeses;
        this.otrNoCons = otrNoCons;
        this.afCual1 = afCual1;
        this.afTiempo1 = afTiempo1;
        this.afCual2 = afCual2;
        this.afTiempo2 = afTiempo2;
        this.afCual3 = afCual3;
        this.afTiempo3 = afTiempo3;
        this.medCual1 = medCual1;
        this.medCant1 = medCant1;
        this.medCual2 = medCual2;
        this.medCant2 = medCant2;
        this.medCual3 = medCual3;
        this.medCant3 = medCant3;
        this.obsConsumoVidaCond = obsConsumoVidaCond;
        this.instSistema = instSistema;
        this.rucEstablecimiento = rucEstablecimiento;
        this.ciiu = ciiu;
        this.establecimientoCt = establecimientoCt;
        this.noHistoriaClinica = noHistoriaClinica;
        this.noArchivo = noArchivo;
        this.areaTrabajo = areaTrabajo;
        this.puestoTrabajoTxt = puestoTrabajoTxt;
        this.extraLabDesc = extraLabDesc;
        this.extraLabFecha = extraLabFecha;
        this.enfermedadProbActual = enfermedadProbActual;
        this.obsExamenFisicoRegional = obsExamenFisicoRegional;
        this.obsExamenFisicoReg = obsExamenFisicoReg;
        this.exfPielCicatrices = exfPielCicatrices;
        this.exfOjosParpados = exfOjosParpados;
        this.exfOjosConjuntivas = exfOjosConjuntivas;
        this.exfOjosPupilas = exfOjosPupilas;
        this.exfOjosCornea = exfOjosCornea;
        this.exfOjosMotilidad = exfOjosMotilidad;
        this.exfOidoConducto = exfOidoConducto;
        this.exfOidoPabellon = exfOidoPabellon;
        this.exfOidoTimpanos = exfOidoTimpanos;
        this.exfOroLabios = exfOroLabios;
        this.exfOroLengua = exfOroLengua;
        this.exfOroFaringe = exfOroFaringe;
        this.exfOroAmigdalas = exfOroAmigdalas;
        this.exfOroDentadura = exfOroDentadura;
        this.exfNarizTabique = exfNarizTabique;
        this.exfNarizCornetes = exfNarizCornetes;
        this.exfNarizMucosas = exfNarizMucosas;
        this.exfNarizSenosParanasa = exfNarizSenosParanasa;
        this.exfCuelloTiroidesMasas = exfCuelloTiroidesMasas;
        this.exfCuelloMovilidad = exfCuelloMovilidad;
        this.exfToraxMamas = exfToraxMamas;
        this.exfToraxPulmones = exfToraxPulmones;
        this.exfToraxCorazon = exfToraxCorazon;
        this.exfToraxParrillaCostal = exfToraxParrillaCostal;
        this.exfAbdVisceras = exfAbdVisceras;
        this.exfAbdParedAbdominal = exfAbdParedAbdominal;
        this.exfColFlexibilidad = exfColFlexibilidad;
        this.exfColDesviacion = exfColDesviacion;
        this.exfColDolor = exfColDolor;
        this.exfPelvisPelvis = exfPelvisPelvis;
        this.exfPelvisGenitales = exfPelvisGenitales;
        this.exfExtVascular = exfExtVascular;
        this.exfExtMiembrosSup = exfExtMiembrosSup;
        this.exfExtMiembrosInf = exfExtMiembrosInf;
        this.exfNeuroFuerza = exfNeuroFuerza;
        this.exfNeuroSensibilidad = exfNeuroSensibilidad;
        this.exfNeuroMarcha = exfNeuroMarcha;
        this.exfNeuroReflejos = exfNeuroReflejos;
        this.nRetEval = nRetEval;
        this.nRetRelTrab = nRetRelTrab;
        this.nRetObs = nRetObs;
        this.aptitudSel = aptitudSel;
        this.detalleObs = detalleObs;
        this.recomendaciones = recomendaciones;
        this.fechaEmision = fechaEmision;
        this.medicoNombre = medicoNombre;
        this.medicoCodigo = medicoCodigo;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.usrCreacion = usrCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.usrActualizacion = usrActualizacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FichaOcupacional that = (FichaOcupacional) o;
        return Objects.equals(idFicha, that.idFicha);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idFicha);
    }

    @Override
    public String toString() {
        return "FichaOcupacional{"
                + "idFicha=" + idFicha
                + ", fechaEvaluacion=" + fechaEvaluacion
                + ", tipoEvaluacion='" + tipoEvaluacion + '\''
                + ", observacion='" + observacion + '\''
                + ", apEmbarazada='" + apEmbarazada + '\''
                + ", apDiscapacidad='" + apDiscapacidad + '\''
                + ", apCatastrofica='" + apCatastrofica + '\''
                + ", apLactancia='" + apLactancia + '\''
                + ", apAdultoMayor='" + apAdultoMayor + '\''
                + ", antClinicoQuir='" + antClinicoQuir + '\''
                + ", antFamiliares='" + antFamiliares + '\''
                + ", condicionEspecial='" + condicionEspecial + '\''
                + ", autorizaTransfusion='" + autorizaTransfusion + '\''
                + ", tratHormonal='" + tratHormonal + '\''
                + ", tratHormonalCual='" + tratHormonalCual + '\''
                + ", examReproMasc='" + examReproMasc + '\''
                + ", tiempoReproMasc=" + tiempoReproMasc
                + ", fum=" + fum
                + ", gestas=" + gestas
                + ", partos=" + partos
                + ", cesareas=" + cesareas
                + ", abortos=" + abortos
                + ", planificacion='" + planificacion + '\''
                + ", planificacionCual='" + planificacionCual + '\''
                + ", tabConsMeses=" + tabConsMeses
                + ", tabExCons='" + tabExCons + '\''
                + ", tabAbsMeses=" + tabAbsMeses
                + ", tabNoCons='" + tabNoCons + '\''
                + ", alcConsMeses=" + alcConsMeses
                + ", alcExCons='" + alcExCons + '\''
                + ", alcAbsMeses=" + alcAbsMeses
                + ", alcNoCons='" + alcNoCons + '\''
                + ", otrCual='" + otrCual + '\''
                + ", otrConsMeses=" + otrConsMeses
                + ", otrExCons='" + otrExCons + '\''
                + ", otrAbsMeses=" + otrAbsMeses
                + ", otrNoCons='" + otrNoCons + '\''
                + ", afCual1='" + afCual1 + '\''
                + ", afTiempo1='" + afTiempo1 + '\''
                + ", afCual2='" + afCual2 + '\''
                + ", afTiempo2='" + afTiempo2 + '\''
                + ", afCual3='" + afCual3 + '\''
                + ", afTiempo3='" + afTiempo3 + '\''
                + ", medCual1='" + medCual1 + '\''
                + ", medCant1=" + medCant1
                + ", medCual2='" + medCual2 + '\''
                + ", medCant2=" + medCant2
                + ", medCual3='" + medCual3 + '\''
                + ", medCant3=" + medCant3
                + ", obsConsumoVidaCond='" + obsConsumoVidaCond + '\''
                + ", instSistema='" + instSistema + '\''
                + ", rucEstablecimiento='" + rucEstablecimiento + '\''
                + ", ciiu='" + ciiu + '\''
                + ", establecimientoCt='" + establecimientoCt + '\''
                + ", noHistoriaClinica='" + noHistoriaClinica + '\''
                + ", noArchivo='" + noArchivo + '\''
                + ", areaTrabajo='" + areaTrabajo + '\''
                + ", puestoTrabajoTxt='" + puestoTrabajoTxt + '\''
                + ", extraLabDesc='" + extraLabDesc + '\''
                + ", extraLabFecha=" + extraLabFecha
                + ", enfermedadProbActual='" + enfermedadProbActual + '\''
                + ", obsExamenFisicoRegional='" + obsExamenFisicoRegional + '\''
                + ", obsExamenFisicoReg='" + obsExamenFisicoReg + '\''
                + ", aptitudSel='" + aptitudSel + '\''
                + ", detalleObs='" + detalleObs + '\''
                + ", recomendaciones='" + recomendaciones + '\''
                + ", fechaEmision=" + fechaEmision
                + ", medicoNombre='" + medicoNombre + '\''
                + ", medicoCodigo='" + medicoCodigo + '\''
                + ", estado='" + estado + '\''
                + ", fechaCreacion=" + fechaCreacion
                + ", usrCreacion='" + usrCreacion + '\''
                + ", fechaActualizacion=" + fechaActualizacion
                + ", usrActualizacion='" + usrActualizacion + '\''
                + '}';
    }

    private static boolean snToBool(String v) {
        return "S".equalsIgnoreCase(v);
    }

    private static String boolToSn(Boolean b) {
        return (b != null && b) ? "S" : "N";
    }

    public Boolean getApEmbarazadaBool() {
        return snToBool(apEmbarazada);
    }

    public void setApEmbarazadaBool(Boolean v) {
        this.apEmbarazada = boolToSn(v);
    }

    @PrePersist
    public void prePersist() {
        final Date ahora = new Date();

        if (fechaCreacion == null) {
            fechaCreacion = ahora;
        }

        if (estado == null || estado.trim().isEmpty()) {
            estado = "BORRADOR";
        }

        apEmbarazada = defaultN(apEmbarazada);
        apDiscapacidad = defaultN(apDiscapacidad);
        apCatastrofica = defaultN(apCatastrofica);
        apLactancia = defaultN(apLactancia);
        apAdultoMayor = defaultN(apAdultoMayor);
        catCalificada = defaultN(catCalificada);

        tabExCons = defaultN(tabExCons);
        tabNoCons = defaultN(tabNoCons);
        alcExCons = defaultN(alcExCons);
        alcNoCons = defaultN(alcNoCons);
        otrExCons = defaultN(otrExCons);
        otrNoCons = defaultN(otrNoCons);

        exfPielCicatrices = defaultN(exfPielCicatrices);
        exfOjosParpados = defaultN(exfOjosParpados);
        exfOjosConjuntivas = defaultN(exfOjosConjuntivas);
        exfOjosPupilas = defaultN(exfOjosPupilas);
        exfOjosCornea = defaultN(exfOjosCornea);
        exfOjosMotilidad = defaultN(exfOjosMotilidad);
        exfOidoConducto = defaultN(exfOidoConducto);
        exfOidoPabellon = defaultN(exfOidoPabellon);
        exfOidoTimpanos = defaultN(exfOidoTimpanos);

        exfOroLabios = defaultN(exfOroLabios);
        exfOroLengua = defaultN(exfOroLengua);
        exfOroFaringe = defaultN(exfOroFaringe);
        exfOroAmigdalas = defaultN(exfOroAmigdalas);
        exfOroDentadura = defaultN(exfOroDentadura);

        exfNarizTabique = defaultN(exfNarizTabique);
        exfNarizCornetes = defaultN(exfNarizCornetes);
        exfNarizMucosas = defaultN(exfNarizMucosas);
        exfNarizSenosParanasa = defaultN(exfNarizSenosParanasa);

        exfCuelloTiroidesMasas = defaultN(exfCuelloTiroidesMasas);
        exfCuelloMovilidad = defaultN(exfCuelloMovilidad);

        exfToraxMamas = defaultN(exfToraxMamas);
        exfToraxPulmones = defaultN(exfToraxPulmones);
        exfToraxCorazon = defaultN(exfToraxCorazon);
        exfToraxParrillaCostal = defaultN(exfToraxParrillaCostal);

        exfAbdVisceras = defaultN(exfAbdVisceras);
        exfAbdParedAbdominal = defaultN(exfAbdParedAbdominal);

        exfColFlexibilidad = defaultN(exfColFlexibilidad);
        exfColDesviacion = defaultN(exfColDesviacion);
        exfColDolor = defaultN(exfColDolor);

        exfPelvisPelvis = defaultN(exfPelvisPelvis);
        exfPelvisGenitales = defaultN(exfPelvisGenitales);

        exfExtVascular = defaultN(exfExtVascular);
        exfExtMiembrosSup = defaultN(exfExtMiembrosSup);
        exfExtMiembrosInf = defaultN(exfExtMiembrosInf);

        exfNeuroFuerza = defaultN(exfNeuroFuerza);
        exfNeuroSensibilidad = defaultN(exfNeuroSensibilidad);
        exfNeuroMarcha = defaultN(exfNeuroMarcha);
        exfNeuroReflejos = defaultN(exfNeuroReflejos);

        nRetEval = defaultN(nRetEval);
        nRetRelTrab = defaultN(nRetRelTrab);

        // Protección BORRADOR
        if ("BORRADOR".equalsIgnoreCase(estado)) {
            fechaEmision = null;
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = new Date();
        // Seguridad: si vuelve a BORRADOR, limpia emisión
        if ("BORRADOR".equalsIgnoreCase(estado)) {
            fechaEmision = null;
        }
    }

    private static String defaultN(String v) {
        if (v == null || v.trim().isEmpty()) {
            return "N";
        }
        return v;
    }

    public Long getIdFicha() {
        return idFicha;
    }

    public void setIdFicha(Long idFicha) {
        this.idFicha = idFicha;
    }

    public DatEmpleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(DatEmpleado empleado) {
        this.empleado = empleado;
    }

    public PersonaAux getPersonaAux() {
        return personaAux;
    }

    public void setPersonaAux(PersonaAux personaAux) {
        this.personaAux = personaAux;
    }

    public SignosVitales getSignos() {
        return signos;
    }

    public void setSignos(SignosVitales signos) {
        this.signos = signos;
    }

    public Cie10 getCie10Principal() {
        return cie10Principal;
    }

    public void setCie10Principal(Cie10 cie10Principal) {
        this.cie10Principal = cie10Principal;
    }

    public Date getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public void setFechaEvaluacion(Date fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }

    public String getTipoEvaluacion() {
        return tipoEvaluacion;
    }

    public void setTipoEvaluacion(String tipoEvaluacion) {
        this.tipoEvaluacion = tipoEvaluacion;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getApEmbarazada() {
        return apEmbarazada;
    }

    public void setApEmbarazada(String apEmbarazada) {
        this.apEmbarazada = apEmbarazada;
    }

    public String getApDiscapacidad() {
        return apDiscapacidad;
    }

    public void setApDiscapacidad(String apDiscapacidad) {
        this.apDiscapacidad = apDiscapacidad;
    }

    public String getApCatastrofica() {
        return apCatastrofica;
    }

    public void setApCatastrofica(String apCatastrofica) {
        this.apCatastrofica = apCatastrofica;
    }

    public String getApLactancia() {
        return apLactancia;
    }

    public void setApLactancia(String apLactancia) {
        this.apLactancia = apLactancia;
    }

    public String getApAdultoMayor() {
        return apAdultoMayor;
    }

    public void setApAdultoMayor(String apAdultoMayor) {
        this.apAdultoMayor = apAdultoMayor;
    }
    public String getDisTipo() { return disTipo; }
public void setDisTipo(String disTipo) { this.disTipo = disTipo; }

public String getDisDescripcion() { return disDescripcion; }
public void setDisDescripcion(String disDescripcion) { this.disDescripcion = disDescripcion; }

public Integer getDisPorcentaje() { return disPorcentaje; }
public void setDisPorcentaje(Integer disPorcentaje) { this.disPorcentaje = disPorcentaje; }

public String getCatDiagnostico() { return catDiagnostico; }
public void setCatDiagnostico(String catDiagnostico) { this.catDiagnostico = catDiagnostico; }

public String getCatCalificada() { return catCalificada; }
public void setCatCalificada(String catCalificada) { this.catCalificada = catCalificada; }

    public String getAntClinicoQuir() {
        return antClinicoQuir;
    }

    public void setAntClinicoQuir(String antClinicoQuir) {
        this.antClinicoQuir = antClinicoQuir;
    }

    public String getAntFamiliares() {
        return antFamiliares;
    }

    public void setAntFamiliares(String antFamiliares) {
        this.antFamiliares = antFamiliares;
    }

    public String getCondicionEspecial() {
        return condicionEspecial;
    }

    public void setCondicionEspecial(String condicionEspecial) {
        this.condicionEspecial = condicionEspecial;
    }

    public String getAutorizaTransfusion() {
        return autorizaTransfusion;
    }

    public void setAutorizaTransfusion(String autorizaTransfusion) {
        this.autorizaTransfusion = autorizaTransfusion;
    }

    public String getTratHormonal() {
        return tratHormonal;
    }

    public void setTratHormonal(String tratHormonal) {
        this.tratHormonal = tratHormonal;
    }

    public String getTratHormonalCual() {
        return tratHormonalCual;
    }

    public void setTratHormonalCual(String tratHormonalCual) {
        this.tratHormonalCual = tratHormonalCual;
    }

    public String getExamReproMasc() {
        return examReproMasc;
    }

    public void setExamReproMasc(String examReproMasc) {
        this.examReproMasc = examReproMasc;
    }

    public Integer getTiempoReproMasc() {
        return tiempoReproMasc;
    }

    public void setTiempoReproMasc(Integer tiempoReproMasc) {
        this.tiempoReproMasc = tiempoReproMasc;
    }

    public Date getFum() {
        return fum;
    }

    public void setFum(Date fum) {
        this.fum = fum;
    }

    public Integer getGestas() {
        return gestas;
    }

    public void setGestas(Integer gestas) {
        this.gestas = gestas;
    }

    public Integer getPartos() {
        return partos;
    }

    public void setPartos(Integer partos) {
        this.partos = partos;
    }

    public Integer getCesareas() {
        return cesareas;
    }

    public void setCesareas(Integer cesareas) {
        this.cesareas = cesareas;
    }

    public Integer getAbortos() {
        return abortos;
    }

    public void setAbortos(Integer abortos) {
        this.abortos = abortos;
    }

    public String getPlanificacion() {
        return planificacion;
    }

    public void setPlanificacion(String planificacion) {
        this.planificacion = planificacion;

        if (!"SI".equalsIgnoreCase(this.planificacion)) {
            this.planificacionCual = null;
        }
    }

    public String getPlanificacionCual() {
        return planificacionCual;
    }

    public void setPlanificacionCual(String planificacionCual) {
        this.planificacionCual = planificacionCual;
    }

    public Integer getTabConsMeses() {
        return tabConsMeses;
    }

    public void setTabConsMeses(Integer tabConsMeses) {
        this.tabConsMeses = tabConsMeses;
    }

    public String getTabExCons() {
        return tabExCons;
    }

    public void setTabExCons(String tabExCons) {
        this.tabExCons = tabExCons;
    }

    public Integer getTabAbsMeses() {
        return tabAbsMeses;
    }

    public void setTabAbsMeses(Integer tabAbsMeses) {
        this.tabAbsMeses = tabAbsMeses;
    }

    public String getTabNoCons() {
        return tabNoCons;
    }

    public void setTabNoCons(String tabNoCons) {
        this.tabNoCons = tabNoCons;
    }

    public Integer getAlcConsMeses() {
        return alcConsMeses;
    }

    public void setAlcConsMeses(Integer alcConsMeses) {
        this.alcConsMeses = alcConsMeses;
    }

    public String getAlcExCons() {
        return alcExCons;
    }

    public void setAlcExCons(String alcExCons) {
        this.alcExCons = alcExCons;
    }

    public Integer getAlcAbsMeses() {
        return alcAbsMeses;
    }

    public void setAlcAbsMeses(Integer alcAbsMeses) {
        this.alcAbsMeses = alcAbsMeses;
    }

    public String getAlcNoCons() {
        return alcNoCons;
    }

    public void setAlcNoCons(String alcNoCons) {
        this.alcNoCons = alcNoCons;
    }

    public String getOtrCual() {
        return otrCual;
    }

    public void setOtrCual(String otrCual) {
        this.otrCual = otrCual;
    }

    public Integer getOtrConsMeses() {
        return otrConsMeses;
    }

    public void setOtrConsMeses(Integer otrConsMeses) {
        this.otrConsMeses = otrConsMeses;
    }

    public String getOtrExCons() {
        return otrExCons;
    }

    public void setOtrExCons(String otrExCons) {
        this.otrExCons = otrExCons;
    }

    public Integer getOtrAbsMeses() {
        return otrAbsMeses;
    }

    public void setOtrAbsMeses(Integer otrAbsMeses) {
        this.otrAbsMeses = otrAbsMeses;
    }

    public String getOtrNoCons() {
        return otrNoCons;
    }

    public void setOtrNoCons(String otrNoCons) {
        this.otrNoCons = otrNoCons;
    }

    public String getAfCual1() {
        return afCual1;
    }

    public void setAfCual1(String afCual1) {
        this.afCual1 = afCual1;
    }

    public String getAfTiempo1() {
        return afTiempo1;
    }

    public void setAfTiempo1(String afTiempo1) {
        this.afTiempo1 = afTiempo1;
    }

    public String getAfCual2() {
        return afCual2;
    }

    public void setAfCual2(String afCual2) {
        this.afCual2 = afCual2;
    }

    public String getAfTiempo2() {
        return afTiempo2;
    }

    public void setAfTiempo2(String afTiempo2) {
        this.afTiempo2 = afTiempo2;
    }

    public String getAfCual3() {
        return afCual3;
    }

    public void setAfCual3(String afCual3) {
        this.afCual3 = afCual3;
    }

    public String getAfTiempo3() {
        return afTiempo3;
    }

    public void setAfTiempo3(String afTiempo3) {
        this.afTiempo3 = afTiempo3;
    }

    public String getMedCual1() {
        return medCual1;
    }

    public void setMedCual1(String medCual1) {
        this.medCual1 = medCual1;
    }

    public Integer getMedCant1() {
        return medCant1;
    }

    public void setMedCant1(Integer medCant1) {
        this.medCant1 = medCant1;
    }

    public String getMedCual2() {
        return medCual2;
    }

    public void setMedCual2(String medCual2) {
        this.medCual2 = medCual2;
    }

    public Integer getMedCant2() {
        return medCant2;
    }

    public void setMedCant2(Integer medCant2) {
        this.medCant2 = medCant2;
    }

    public String getMedCual3() {
        return medCual3;
    }

    public void setMedCual3(String medCual3) {
        this.medCual3 = medCual3;
    }

    public Integer getMedCant3() {
        return medCant3;
    }

    public void setMedCant3(Integer medCant3) {
        this.medCant3 = medCant3;
    }

    public String getObsConsumoVidaCond() {
        return obsConsumoVidaCond;
    }

    public void setObsConsumoVidaCond(String obsConsumoVidaCond) {
        this.obsConsumoVidaCond = obsConsumoVidaCond;
    }

    public String getInstSistema() {
        return instSistema;
    }

    public void setInstSistema(String instSistema) {
        this.instSistema = instSistema;
    }

    public String getRucEstablecimiento() {
        return rucEstablecimiento;
    }

    public void setRucEstablecimiento(String rucEstablecimiento) {
        this.rucEstablecimiento = rucEstablecimiento;
    }

    public String getCiiu() {
        return ciiu;
    }

    public void setCiiu(String ciiu) {
        this.ciiu = ciiu;
    }

    public String getEstablecimientoCt() {
        return establecimientoCt;
    }

    public void setEstablecimientoCt(String establecimientoCt) {
        this.establecimientoCt = establecimientoCt;
    }

    public String getNoHistoriaClinica() {
        return noHistoriaClinica;
    }

    public void setNoHistoriaClinica(String noHistoriaClinica) {
        this.noHistoriaClinica = noHistoriaClinica;
    }

    public String getNoArchivo() {
        return noArchivo;
    }

    public void setNoArchivo(String noArchivo) {
        this.noArchivo = noArchivo;
    }

    public String getAreaTrabajo() {
        return areaTrabajo;
    }

    public void setAreaTrabajo(String areaTrabajo) {
        this.areaTrabajo = areaTrabajo;
    }

    public String getPuestoTrabajoTxt() {
        return puestoTrabajoTxt;
    }

    public void setPuestoTrabajoTxt(String puestoTrabajoTxt) {
        this.puestoTrabajoTxt = puestoTrabajoTxt;
    }

    public String getExtraLabDesc() {
        return extraLabDesc;
    }

    public void setExtraLabDesc(String extraLabDesc) {
        this.extraLabDesc = extraLabDesc;
    }

    public Date getExtraLabFecha() {
        return extraLabFecha;
    }

    public void setExtraLabFecha(Date extraLabFecha) {
        this.extraLabFecha = extraLabFecha;
    }

    public String getEnfermedadProbActual() {
        return enfermedadProbActual;
    }

    public void setEnfermedadProbActual(String enfermedadProbActual) {
        this.enfermedadProbActual = enfermedadProbActual;
    }

    public String getObsExamenFisicoRegional() {
        return obsExamenFisicoRegional;
    }

    public void setObsExamenFisicoRegional(String obsExamenFisicoRegional) {
        this.obsExamenFisicoRegional = obsExamenFisicoRegional;
    }

    public String getObsExamenFisicoReg() {
        return obsExamenFisicoReg;
    }

    public void setObsExamenFisicoReg(String obsExamenFisicoReg) {
        this.obsExamenFisicoReg = obsExamenFisicoReg;
    }

    public String getExfPielCicatrices() {
        return exfPielCicatrices;
    }

    public void setExfPielCicatrices(String exfPielCicatrices) {
        this.exfPielCicatrices = exfPielCicatrices;
    }

    public String getExfOjosParpados() {
        return exfOjosParpados;
    }

    public void setExfOjosParpados(String exfOjosParpados) {
        this.exfOjosParpados = exfOjosParpados;
    }

    public String getExfOjosConjuntivas() {
        return exfOjosConjuntivas;
    }

    public void setExfOjosConjuntivas(String exfOjosConjuntivas) {
        this.exfOjosConjuntivas = exfOjosConjuntivas;
    }

    public String getExfOjosPupilas() {
        return exfOjosPupilas;
    }

    public void setExfOjosPupilas(String exfOjosPupilas) {
        this.exfOjosPupilas = exfOjosPupilas;
    }

    public String getExfOjosCornea() {
        return exfOjosCornea;
    }

    public void setExfOjosCornea(String exfOjosCornea) {
        this.exfOjosCornea = exfOjosCornea;
    }

    public String getExfOjosMotilidad() {
        return exfOjosMotilidad;
    }

    public void setExfOjosMotilidad(String exfOjosMotilidad) {
        this.exfOjosMotilidad = exfOjosMotilidad;
    }

    public String getExfOidoConducto() {
        return exfOidoConducto;
    }

    public void setExfOidoConducto(String exfOidoConducto) {
        this.exfOidoConducto = exfOidoConducto;
    }

    public String getExfOidoPabellon() {
        return exfOidoPabellon;
    }

    public void setExfOidoPabellon(String exfOidoPabellon) {
        this.exfOidoPabellon = exfOidoPabellon;
    }

    public String getExfOidoTimpanos() {
        return exfOidoTimpanos;
    }

    public void setExfOidoTimpanos(String exfOidoTimpanos) {
        this.exfOidoTimpanos = exfOidoTimpanos;
    }

    public String getExfOroLabios() {
        return exfOroLabios;
    }

    public void setExfOroLabios(String exfOroLabios) {
        this.exfOroLabios = exfOroLabios;
    }

    public String getExfOroLengua() {
        return exfOroLengua;
    }

    public void setExfOroLengua(String exfOroLengua) {
        this.exfOroLengua = exfOroLengua;
    }

    public String getExfOroFaringe() {
        return exfOroFaringe;
    }

    public void setExfOroFaringe(String exfOroFaringe) {
        this.exfOroFaringe = exfOroFaringe;
    }

    public String getExfOroAmigdalas() {
        return exfOroAmigdalas;
    }

    public void setExfOroAmigdalas(String exfOroAmigdalas) {
        this.exfOroAmigdalas = exfOroAmigdalas;
    }

    public String getExfOroDentadura() {
        return exfOroDentadura;
    }

    public void setExfOroDentadura(String exfOroDentadura) {
        this.exfOroDentadura = exfOroDentadura;
    }

    public String getExfNarizTabique() {
        return exfNarizTabique;
    }

    public void setExfNarizTabique(String exfNarizTabique) {
        this.exfNarizTabique = exfNarizTabique;
    }

    public String getExfNarizCornetes() {
        return exfNarizCornetes;
    }

    public void setExfNarizCornetes(String exfNarizCornetes) {
        this.exfNarizCornetes = exfNarizCornetes;
    }

    public String getExfNarizMucosas() {
        return exfNarizMucosas;
    }

    public void setExfNarizMucosas(String exfNarizMucosas) {
        this.exfNarizMucosas = exfNarizMucosas;
    }

    public String getExfNarizSenosParanasa() {
        return exfNarizSenosParanasa;
    }

    public void setExfNarizSenosParanasa(String exfNarizSenosParanasa) {
        this.exfNarizSenosParanasa = exfNarizSenosParanasa;
    }

    public String getExfCuelloTiroidesMasas() {
        return exfCuelloTiroidesMasas;
    }

    public void setExfCuelloTiroidesMasas(String exfCuelloTiroidesMasas) {
        this.exfCuelloTiroidesMasas = exfCuelloTiroidesMasas;
    }

    public String getExfCuelloMovilidad() {
        return exfCuelloMovilidad;
    }

    public void setExfCuelloMovilidad(String exfCuelloMovilidad) {
        this.exfCuelloMovilidad = exfCuelloMovilidad;
    }

    public String getExfToraxMamas() {
        return exfToraxMamas;
    }

    public void setExfToraxMamas(String exfToraxMamas) {
        this.exfToraxMamas = exfToraxMamas;
    }

    public String getExfToraxPulmones() {
        return exfToraxPulmones;
    }

    public void setExfToraxPulmones(String exfToraxPulmones) {
        this.exfToraxPulmones = exfToraxPulmones;
    }

    public String getExfToraxCorazon() {
        return exfToraxCorazon;
    }

    public void setExfToraxCorazon(String exfToraxCorazon) {
        this.exfToraxCorazon = exfToraxCorazon;
    }

    public String getExfToraxParrillaCostal() {
        return exfToraxParrillaCostal;
    }

    public void setExfToraxParrillaCostal(String exfToraxParrillaCostal) {
        this.exfToraxParrillaCostal = exfToraxParrillaCostal;
    }

    public String getExfAbdVisceras() {
        return exfAbdVisceras;
    }

    public void setExfAbdVisceras(String exfAbdVisceras) {
        this.exfAbdVisceras = exfAbdVisceras;
    }

    public String getExfAbdParedAbdominal() {
        return exfAbdParedAbdominal;
    }

    public void setExfAbdParedAbdominal(String exfAbdParedAbdominal) {
        this.exfAbdParedAbdominal = exfAbdParedAbdominal;
    }

    public String getExfColFlexibilidad() {
        return exfColFlexibilidad;
    }

    public void setExfColFlexibilidad(String exfColFlexibilidad) {
        this.exfColFlexibilidad = exfColFlexibilidad;
    }

    public String getExfColDesviacion() {
        return exfColDesviacion;
    }

    public void setExfColDesviacion(String exfColDesviacion) {
        this.exfColDesviacion = exfColDesviacion;
    }

    public String getExfColDolor() {
        return exfColDolor;
    }

    public void setExfColDolor(String exfColDolor) {
        this.exfColDolor = exfColDolor;
    }

    public String getExfPelvisPelvis() {
        return exfPelvisPelvis;
    }

    public void setExfPelvisPelvis(String exfPelvisPelvis) {
        this.exfPelvisPelvis = exfPelvisPelvis;
    }

    public String getExfPelvisGenitales() {
        return exfPelvisGenitales;
    }

    public void setExfPelvisGenitales(String exfPelvisGenitales) {
        this.exfPelvisGenitales = exfPelvisGenitales;
    }

    public String getExfExtVascular() {
        return exfExtVascular;
    }

    public void setExfExtVascular(String exfExtVascular) {
        this.exfExtVascular = exfExtVascular;
    }

    public String getExfExtMiembrosSup() {
        return exfExtMiembrosSup;
    }

    public void setExfExtMiembrosSup(String exfExtMiembrosSup) {
        this.exfExtMiembrosSup = exfExtMiembrosSup;
    }

    public String getExfExtMiembrosInf() {
        return exfExtMiembrosInf;
    }

    public void setExfExtMiembrosInf(String exfExtMiembrosInf) {
        this.exfExtMiembrosInf = exfExtMiembrosInf;
    }

    public String getExfNeuroFuerza() {
        return exfNeuroFuerza;
    }

    public void setExfNeuroFuerza(String exfNeuroFuerza) {
        this.exfNeuroFuerza = exfNeuroFuerza;
    }

    public String getExfNeuroSensibilidad() {
        return exfNeuroSensibilidad;
    }

    public void setExfNeuroSensibilidad(String exfNeuroSensibilidad) {
        this.exfNeuroSensibilidad = exfNeuroSensibilidad;
    }

    public String getExfNeuroMarcha() {
        return exfNeuroMarcha;
    }

    public void setExfNeuroMarcha(String exfNeuroMarcha) {
        this.exfNeuroMarcha = exfNeuroMarcha;
    }

    public String getExfNeuroReflejos() {
        return exfNeuroReflejos;
    }

    public void setExfNeuroReflejos(String exfNeuroReflejos) {
        this.exfNeuroReflejos = exfNeuroReflejos;
    }

    public String getnRetEval() {
        return nRetEval;
    }

    public void setnRetEval(String nRetEval) {
        this.nRetEval = nRetEval;
    }

    public String getnRetRelTrab() {
        return nRetRelTrab;
    }

    public void setnRetRelTrab(String nRetRelTrab) {
        this.nRetRelTrab = nRetRelTrab;
    }

    public String getnRetObs() {
        return nRetObs;
    }

    public void setnRetObs(String nRetObs) {
        this.nRetObs = nRetObs;
    }

    public String getAptitudSel() {
        return aptitudSel;
    }

    public void setAptitudSel(String aptitudSel) {
        this.aptitudSel = aptitudSel;
    }

    public String getDetalleObs() {
        return detalleObs;
    }

    public void setDetalleObs(String detalleObs) {
        this.detalleObs = detalleObs;
    }

    public String getRecomendaciones() {
        return recomendaciones;
    }

    public void setRecomendaciones(String recomendaciones) {
        this.recomendaciones = recomendaciones;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
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

    private static Boolean toBool(String v) {
        if (v == null) {
            return null;
        }
        v = v.trim();
        if (v.isEmpty()) {
            return null;
        }
        return "S".equalsIgnoreCase(v) || "1".equals(v) || "Y".equalsIgnoreCase(v) || "T".equalsIgnoreCase(v) || "TRUE".equalsIgnoreCase(v);
    }

    private static String fromBool(Boolean b) {
        if (b == null) {
            return null;
        }
        return b ? "S" : "N";
    }

    @Transient
    public Boolean getExfCuelloMovilidadBool() {
        return toBool(this.exfCuelloMovilidad);
    }

    public void setExfCuelloMovilidadBool(Boolean value) {
        this.exfCuelloMovilidad = fromBool(value);
    }

    @Transient
    public Boolean getExfExtVascularBool() {
        return toBool(this.exfExtVascular);
    }

    public void setExfExtVascularBool(Boolean value) {
        this.exfExtVascular = fromBool(value);
    }

    @Transient
    public Boolean getExfNarizCornetesBool() {
        return toBool(this.exfNarizCornetes);
    }

    public void setExfNarizCornetesBool(Boolean value) {
        this.exfNarizCornetes = fromBool(value);
    }

    @Transient
    public Boolean getExfNarizMucosasBool() {
        return toBool(this.exfNarizMucosas);
    }

    public void setExfNarizMucosasBool(Boolean value) {
        this.exfNarizMucosas = fromBool(value);
    }

    @Transient
    public Boolean getExfNarizTabiqueBool() {
        return toBool(this.exfNarizTabique);
    }

    public void setExfNarizTabiqueBool(Boolean value) {
        this.exfNarizTabique = fromBool(value);
    }

    @Transient
    public Boolean getExfNeuroFuerzaBool() {
        return toBool(this.exfNeuroFuerza);
    }

    public void setExfNeuroFuerzaBool(Boolean value) {
        this.exfNeuroFuerza = fromBool(value);
    }

    @Transient
    public Boolean getExfNeuroMarchaBool() {
        return toBool(this.exfNeuroMarcha);
    }

    public void setExfNeuroMarchaBool(Boolean value) {
        this.exfNeuroMarcha = fromBool(value);
    }

    @Transient
    public Boolean getExfNeuroReflejosBool() {
        return toBool(this.exfNeuroReflejos);
    }

    public void setExfNeuroReflejosBool(Boolean value) {
        this.exfNeuroReflejos = fromBool(value);
    }

    @Transient
    public Boolean getExfNeuroSensibilidadBool() {
        return toBool(this.exfNeuroSensibilidad);
    }

    public void setExfNeuroSensibilidadBool(Boolean value) {
        this.exfNeuroSensibilidad = fromBool(value);
    }

    @Transient
    public Boolean getExfOidoConductoBool() {
        return toBool(this.exfOidoConducto);
    }

    public void setExfOidoConductoBool(Boolean value) {
        this.exfOidoConducto = fromBool(value);
    }

    @Transient
    public Boolean getExfOidoPabellonBool() {
        return toBool(this.exfOidoPabellon);
    }

    public void setExfOidoPabellonBool(Boolean value) {
        this.exfOidoPabellon = fromBool(value);
    }

    @Transient
    public Boolean getExfOidoTimpanosBool() {
        return toBool(this.exfOidoTimpanos);
    }

    public void setExfOidoTimpanosBool(Boolean value) {
        this.exfOidoTimpanos = fromBool(value);
    }

    @Transient
    public Boolean getExfOjosConjuntivasBool() {
        return toBool(this.exfOjosConjuntivas);
    }

    public void setExfOjosConjuntivasBool(Boolean value) {
        this.exfOjosConjuntivas = fromBool(value);
    }

    @Transient
    public Boolean getExfOjosCorneaBool() {
        return toBool(this.exfOjosCornea);
    }

    public void setExfOjosCorneaBool(Boolean value) {
        this.exfOjosCornea = fromBool(value);
    }

    @Transient
    public Boolean getExfOjosMotilidadBool() {
        return toBool(this.exfOjosMotilidad);
    }

    public void setExfOjosMotilidadBool(Boolean value) {
        this.exfOjosMotilidad = fromBool(value);
    }

    @Transient
    public Boolean getExfOjosParpadosBool() {
        return toBool(this.exfOjosParpados);
    }

    public void setExfOjosParpadosBool(Boolean value) {
        this.exfOjosParpados = fromBool(value);
    }

    @Transient
    public Boolean getExfOjosPupilasBool() {
        return toBool(this.exfOjosPupilas);
    }

    public void setExfOjosPupilasBool(Boolean value) {
        this.exfOjosPupilas = fromBool(value);
    }

    @Transient
    public Boolean getExfOroAmigdalasBool() {
        return toBool(this.exfOroAmigdalas);
    }

    public void setExfOroAmigdalasBool(Boolean value) {
        this.exfOroAmigdalas = fromBool(value);
    }

    @Transient
    public Boolean getExfOroDentaduraBool() {
        return toBool(this.exfOroDentadura);
    }

    public void setExfOroDentaduraBool(Boolean value) {
        this.exfOroDentadura = fromBool(value);
    }

    @Transient
    public Boolean getExfOroFaringeBool() {
        return toBool(this.exfOroFaringe);
    }

    public void setExfOroFaringeBool(Boolean value) {
        this.exfOroFaringe = fromBool(value);
    }

    @Transient
    public Boolean getExfOroLabiosBool() {
        return toBool(this.exfOroLabios);
    }

    public void setExfOroLabiosBool(Boolean value) {
        this.exfOroLabios = fromBool(value);
    }

    @Transient
    public Boolean getExfOroLenguaBool() {
        return toBool(this.exfOroLengua);
    }

    public void setExfOroLenguaBool(Boolean value) {
        this.exfOroLengua = fromBool(value);
    }

    @Transient
    public Boolean getExfPelvisGenitalesBool() {
        return toBool(this.exfPelvisGenitales);
    }

    public void setExfPelvisGenitalesBool(Boolean value) {
        this.exfPelvisGenitales = fromBool(value);
    }

    @Transient
    public Boolean getExfPelvisPelvisBool() {
        return toBool(this.exfPelvisPelvis);
    }

    public void setExfPelvisPelvisBool(Boolean value) {
        this.exfPelvisPelvis = fromBool(value);
    }

    @Transient
    public Boolean getExfPielCicatricesBool() {
        return toBool(this.exfPielCicatrices);
    }

    public void setExfPielCicatricesBool(Boolean value) {
        this.exfPielCicatrices = fromBool(value);
    }

    @Transient
    public Boolean getExfToraxCorazonBool() {
        return toBool(this.exfToraxCorazon);
    }

    public void setExfToraxCorazonBool(Boolean value) {
        this.exfToraxCorazon = fromBool(value);
    }

    @Transient
    public Boolean getExfToraxMamasBool() {
        return toBool(this.exfToraxMamas);
    }

    public void setExfToraxMamasBool(Boolean value) {
        this.exfToraxMamas = fromBool(value);
    }

    @Transient
    public Boolean getExfToraxPulmonesBool() {
        return toBool(this.exfToraxPulmones);
    }

    public void setExfToraxPulmonesBool(Boolean value) {
        this.exfToraxPulmones = fromBool(value);
    }

    // Abdomen: Vísceras / Pared abdominal
    @Transient
    public Boolean getExfAbdomenViscerasBool() {
        return toBool(this.exfAbdVisceras);
    }

    public void setExfAbdomenViscerasBool(Boolean value) {
        this.exfAbdVisceras = fromBool(value);
    }

    @Transient
    public Boolean getExfAbdomenParedBool() {
        return toBool(this.exfAbdParedAbdominal);
    }

    public void setExfAbdomenParedBool(Boolean value) {
        this.exfAbdParedAbdominal = fromBool(value);
    }

    // Columna: Flexibilidad / Desviación / Dolor
    @Transient
    public Boolean getExfColumnaFlexibilidadBool() {
        return toBool(this.exfColFlexibilidad);
    }

    public void setExfColumnaFlexibilidadBool(Boolean value) {
        this.exfColFlexibilidad = fromBool(value);
    }

    @Transient
    public Boolean getExfColumnaDesviacionBool() {
        return toBool(this.exfColDesviacion);
    }

    public void setExfColumnaDesviacionBool(Boolean value) {
        this.exfColDesviacion = fromBool(value);
    }

    @Transient
    public Boolean getExfColumnaDolorBool() {
        return toBool(this.exfColDolor);
    }

    public void setExfColumnaDolorBool(Boolean value) {
        this.exfColDolor = fromBool(value);
    }

    // Cuello: Tiroides/masas
    @Transient
    public Boolean getExfCuelloTiroidesBool() {
        return toBool(this.exfCuelloTiroidesMasas);
    }

    public void setExfCuelloTiroidesBool(Boolean value) {
        this.exfCuelloTiroidesMasas = fromBool(value);
    }

    // Nariz: Senos paranasales
    @Transient
    public Boolean getExfNarizSenosBool() {
        return toBool(this.exfNarizSenosParanasa);
    }

    public void setExfNarizSenosBool(Boolean value) {
        this.exfNarizSenosParanasa = fromBool(value);
    }

    // Tórax: Parrilla costal
    @Transient
    public Boolean getExfToraxParrillaBool() {
        return toBool(this.exfToraxParrillaCostal);
    }

    public void setExfToraxParrillaBool(Boolean value) {
        this.exfToraxParrillaCostal = fromBool(value);
    }

    // Extremidades: Miembros Sup/Inf
    @Transient
    public Boolean getExfExtSupBool() {
        return toBool(this.exfExtMiembrosSup);
    }

    public void setExfExtSupBool(Boolean value) {
        this.exfExtMiembrosSup = fromBool(value);
    }

    @Transient
    public Boolean getExfExtInfBool() {
        return toBool(this.exfExtMiembrosInf);
    }

    public void setExfExtInfBool(Boolean value) {
        this.exfExtMiembrosInf = fromBool(value);
    }

    @Transient
    public Boolean getnRetEvalBool() {
        return toBool(this.nRetEval);
    }

    public void setnRetEvalBool(Boolean value) {
        this.nRetEval = fromBool(value);
    }

    @Transient
    public Boolean getnRetRelTrabBool() {
        return toBool(this.nRetRelTrab);
    }

    public void setnRetRelTrabBool(Boolean value) {
        this.nRetRelTrab = fromBool(value);
    }

    public String getGinecoExamen1() {
        return ginecoExamen1;
    }

    public void setGinecoExamen1(String ginecoExamen1) {
        this.ginecoExamen1 = ginecoExamen1;
    }

    public String getGinecoTiempo1() {
        return ginecoTiempo1;
    }

    public void setGinecoTiempo1(String ginecoTiempo1) {
        this.ginecoTiempo1 = ginecoTiempo1;
    }

    public String getGinecoResultado1() {
        return ginecoResultado1;
    }

    public void setGinecoResultado1(String ginecoResultado1) {
        this.ginecoResultado1 = ginecoResultado1;
    }

    public String getGinecoExamen2() {
        return ginecoExamen2;
    }

    public void setGinecoExamen2(String ginecoExamen2) {
        this.ginecoExamen2 = ginecoExamen2;
    }

    public String getGinecoTiempo2() {
        return ginecoTiempo2;
    }

    public void setGinecoTiempo2(String ginecoTiempo2) {
        this.ginecoTiempo2 = ginecoTiempo2;
    }

    public String getGinecoResultado2() {
        return ginecoResultado2;
    }

    public void setGinecoResultado2(String ginecoResultado2) {
        this.ginecoResultado2 = ginecoResultado2;
    }

    public String getGinecoObservacion() {
        return ginecoObservacion;
    }

    public void setGinecoObservacion(String ginecoObservacion) {
        this.ginecoObservacion = ginecoObservacion;
    }



}
