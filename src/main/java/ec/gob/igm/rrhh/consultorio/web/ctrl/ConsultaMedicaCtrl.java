package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.dto.EmpleadoCargoDTO;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaMedica;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.domain.model.RecetaItem;
import ec.gob.igm.rrhh.consultorio.domain.model.RecetaMedica;
import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import ec.gob.igm.rrhh.consultorio.service.Cie10Service;
import ec.gob.igm.rrhh.consultorio.service.ConsultaMedicaService;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;
import ec.gob.igm.rrhh.consultorio.service.FichaOcupacionalService;
import ec.gob.igm.rrhh.consultorio.service.PersonaAuxService;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoRhService;
import ec.gob.igm.rrhh.consultorio.service.MedicalNotificationService;
import ec.gob.igm.rrhh.consultorio.service.SignosVitalesService;
import ec.gob.igm.rrhh.consultorio.web.audit.CentroMedicoAuditService;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;
import ec.gob.igm.rrhh.consultorio.web.service.Cie10LookupService;
import ec.gob.igm.rrhh.consultorio.web.service.UserContextService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.format.TextStyle;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import org.primefaces.event.SelectEvent;

@Named("consultaMedicaCtrl")
@SessionScoped
public class ConsultaMedicaCtrl implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String AUTH_ROLE_ODONTOLOGO = "ODONTOLOGO";
    private static final ZoneId CERTIFICADO_ZONE = ZoneId.of("America/Guayaquil");
    private static final String[] NUMEROS_BASE = {"", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve",
            "diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho",
            "diecinueve", "veinte", "veintiuno", "veintidós", "veintitrés", "veinticuatro", "veinticinco",
            "veintiséis", "veintisiete", "veintiocho", "veintinueve"};
    private static final String[] DECENAS = {"", "", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"};
    private static final String[] CENTENAS = {"", "ciento", "doscientos", "trescientos", "cuatrocientos",
            "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"};

    @Inject
    private transient EmpleadoService empleadoService;
    @Inject
    private transient PersonaAuxService personaAuxService;
    @Inject
    private transient FichaOcupacionalService fichaOcupacionalService;
    @Inject
    private transient EmpleadoRhService empleadoRhService;
    @Inject
    private transient ConsultaMedicaService consultaMedicaService;
    @Inject
    private transient CentroMedicoPdfFacade centroMedicoPdfFacade;
    @Inject
    private transient PdfResourceResolver pdfResourceResolver;
    @Inject
    private transient Cie10LookupService cie10LookupService;
    @Inject
    private transient Cie10Service cie10Service;
    @Inject
    private transient SignosVitalesService signosVitalesService;
    @Inject
    private transient MedicalNotificationService medicalNotificationService;
    @Inject
    private transient UserContextService userContextService;
    @Inject
    private transient CentroMedicoAuditService auditService;

    private String cedulaBusqueda;
    private DatEmpleado empleado;
    private PersonaAux personaAux;
    private FichaOcupacional fichaReferencia;
    private ConsultaMedica consulta;
    private List<ConsultaMedica> consultasAnteriores;
    private List<ConsultaDiagnostico> diagnosticos;
    private List<RecetaItemForm> recetas;
    private Date vigenciaReceta;
    private String recomendaciones;
    private String signosAlarma;
    private String alergias;
    private String tokenPdf;
    private String tokenPdfCertificado;
    private Date fechaNacimientoPaciente;
    private SignosVitales signosModel;
    private String paStr;
    private boolean generarCertificado;
    private Date certFechaInicio;
    private Date certFechaFin;
    private String certFechaInicioLetras;
    private String certFechaFinLetras;
    private String certDomicilio;
    private String certCargoPaciente;
    private String certAreaTrabajo;
    private String certTelefono;
    private String certTipoContingencia;
    private String certMedicoCargo;
    private String certMedicoTelefono;
    private String certMedicoCorreo;

    @PostConstruct
    public void init() {
        resetFormulario();
    }

    public void nuevaConsulta() {
        resetFormulario();
        addMessage(FacesMessage.SEVERITY_INFO, "Formulario reiniciado", "Puede buscar otro paciente.");
    }

    private void resetFormulario() {
        cedulaBusqueda = null;
        empleado = null;
        personaAux = null;
        fichaReferencia = null;
        consulta = new ConsultaMedica();
        consulta.setFechaConsulta(new Date());
        consulta.setEstado("ACTIVO");
        consulta.setDiagnosticos(new ArrayList<>());
        diagnosticos = new ArrayList<>();
        consultasAnteriores = new ArrayList<>();
        recetas = new ArrayList<>();
        vigenciaReceta = new Date();
        recomendaciones = null;
        signosAlarma = null;
        alergias = null;
        tokenPdf = null;
        tokenPdfCertificado = null;
        fechaNacimientoPaciente = null;
        signosModel = new SignosVitales();
        paStr = null;
        generarCertificado = false;
        certFechaInicio = null;
        certFechaFin = null;
        certFechaInicioLetras = null;
        certFechaFinLetras = null;
        certDomicilio = null;
        certCargoPaciente = null;
        certAreaTrabajo = null;
        certTelefono = null;
        certTipoContingencia = null;
        certMedicoCargo = "MEDICO SALUD OCUPACIONAL";
        certMedicoTelefono = null;
        certMedicoCorreo = null;
        agregarDiagnostico();
        agregarReceta();
    }

    public void buscarPorCedula() {
        if (isBlank(cedulaBusqueda)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Cédula requerida", "Ingrese la cédula del paciente.");
            return;
        }
        String cedula = cedulaBusqueda.trim();
        cargarPaciente(cedula);
        if (empleado == null && personaAux == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Sin resultados", "No existe un paciente con esa cédula.");
            return;
        }
        actualizarDatosPacienteCargado();
        addMessage(FacesMessage.SEVERITY_INFO, "Paciente cargado", getNombrePaciente());
    }

    private void cargarPaciente(String cedula) {
        empleado = empleadoService.buscarPorCedula(cedula);
        personaAux = null;
        fichaReferencia = fichaOcupacionalService.buscarFichaActivaOUltimaPorCedula(cedula);
        if (empleado != null) {
            return;
        }
        personaAux = personaAuxService.findByCedulaConFichaYCertificado(cedula);
        if (personaAux == null) {
            personaAux = personaAuxService.findByCedula(cedula);
        }
        vincularEmpleadoDesdePersonaAux();
    }

    private void vincularEmpleadoDesdePersonaAux() {
        if (personaAux == null || personaAux.getNoPersona() == null) {
            return;
        }
        Long noPersonaAux = personaAux.getNoPersona();
        if (noPersonaAux <= Integer.MAX_VALUE && noPersonaAux >= Integer.MIN_VALUE) {
            empleado = empleadoService.buscarPorId(noPersonaAux.intValue());
        }
    }

    private void actualizarDatosPacienteCargado() {
        consulta.setEmpleado(empleado);
        fechaNacimientoPaciente = empleado != null ? empleado.getfNacimiento() : personaAux.getFechaNac();
        alergias = obtenerAntecedenteClinicoQuirurgico();
        if (isBlank(certDomicilio)) certDomicilio = resolveDireccionPaciente();
        if (isBlank(certCargoPaciente)) certCargoPaciente = resolveCargoPaciente();
        if (isBlank(certAreaTrabajo)) certAreaTrabajo = getAreaTrabajoPaciente();
        if (empleado == null) {
            consultasAnteriores = new ArrayList<>();
            return;
        }
        consultasAnteriores = isUsuarioOdontologo()
                ? consultaMedicaService.buscarPorEmpleado(empleado.getNoPersona(), true)
                : consultaMedicaService.buscarPorEmpleado(empleado.getNoPersona());
    }

    public void agregarDiagnostico() {
        diagnosticos.add(new ConsultaDiagnostico());
    }

    public void eliminarDiagnostico(int index) {
        if (index >= 0 && index < diagnosticos.size()) {
            diagnosticos.remove(index);
        }
        if (diagnosticos.isEmpty()) {
            agregarDiagnostico();
        }
    }

    public void agregarReceta() {
        recetas.add(new RecetaItemForm());
    }

    public void eliminarReceta(int index) {
        if (index >= 0 && index < recetas.size()) {
            recetas.remove(index);
        }
        if (recetas.isEmpty()) {
            agregarReceta();
        }
    }

    public void guardarConsulta() {
        if (!hasPacienteSeleccionado()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Paciente requerido", "Busque un paciente antes de guardar.");
            return;
        }
        DatEmpleado empleadoAsociado = resolveEmpleadoParaConsulta();
        if (empleadoAsociado == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Paciente no vinculado",
                    "La persona auxiliar debe tener NO_PERSONA para registrar la consulta sin crear registros en DAT_EMPLEADO.");
            return;
        }
        consulta.setEmpleado(empleadoAsociado);

        List<ConsultaDiagnostico> limpios = limpiarDiagnosticos();
        if (limpios.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Diagnóstico requerido",
                    "Debe ingresar al menos un diagnóstico válido antes de guardar.");
            return;
        }

        normalizarTipoDiagnostico(limpios);
        consulta.setDiagnosticos(limpios);
        consulta.setRecetas(construirRecetasParaPersistencia());
        persistirSignosVitales();
        String user = userContextService.resolveCurrentUser();
        ConsultaMedica persisted = consultaMedicaService.guardar(consulta, user);
        if (persisted != null) {
            consulta = persisted;
        }
        registrarAuditoriaConsultaGuardada(user);
        addMessage(FacesMessage.SEVERITY_INFO, "Consulta guardada", "Se registró la consulta médica.");
        enviarRecetaPdfACorreoInstitucional();
    }

    private List<ConsultaDiagnostico> limpiarDiagnosticos() {
        List<ConsultaDiagnostico> limpios = new ArrayList<>();
        Set<String> codigosUsados = new HashSet<>();
        int duplicados = 0;
        for (ConsultaDiagnostico d : diagnosticos) {
            String codigo = validarYNormalizarDiagnostico(d, codigosUsados);
            if (codigo == null && (d == null || isBlank(d.getDescripcion()))) {
                continue;
            }
            if ("DUPLICADO".equals(codigo)) {
                duplicados++;
                continue;
            }
            d.setCodigo(codigo);
            d.setConsulta(consulta);
            d.setEstado("ACTIVO");
            limpios.add(d);
        }
        notificarDiagnosticosDuplicados(duplicados);
        return limpios;
    }

    private String validarYNormalizarDiagnostico(ConsultaDiagnostico d, Set<String> codigosUsados) {
        if (d == null) {
            return null;
        }
        String codigo = normalizarCodigo(d.getCodigo());
        if (codigo == null && isBlank(d.getDescripcion())) {
            return null;
        }
        if (codigo != null && !codigosUsados.add(codigo)) {
            return "DUPLICADO";
        }
        return codigo;
    }

    private void notificarDiagnosticosDuplicados(int duplicados) {
        if (duplicados <= 0) {
            return;
        }
        addMessage(FacesMessage.SEVERITY_WARN, "Diagnósticos duplicados omitidos",
                "Se omitieron " + duplicados + " diagnóstico(s) repetido(s) por código CIE10.");
    }

    private void normalizarTipoDiagnostico(List<ConsultaDiagnostico> lista) {
        if (lista == null || lista.isEmpty()) {
            return;
        }
        int idxPrincipal = -1;
        for (int i = 0; i < lista.size(); i++) {
            ConsultaDiagnostico d = lista.get(i);
            if (d == null) {
                continue;
            }
            String tipo = normalizarTipo(d.getTipoDiag());
            d.setTipoDiag(tipo);
            if ("P".equals(tipo) && idxPrincipal < 0) {
                idxPrincipal = i;
            }
        }

        if (idxPrincipal < 0) {
            idxPrincipal = 0;
        }

        for (ConsultaDiagnostico d : lista) {
            if (d == null) {
                continue;
            }
            d.setTipoDiag("S");
        }
        lista.get(idxPrincipal).setTipoDiag("P");
    }

    private String normalizarTipo(String tipo) {
        if (isBlank(tipo)) {
            return "S";
        }
        String value = tipo.trim().toUpperCase();
        return ("P".equals(value) || "D".equals(value)) ? "P" : "S";
    }

    private String normalizarCodigo(String codigo) {
        if (isBlank(codigo)) {
            return null;
        }
        return codigo.trim().toUpperCase();
    }

    private void persistirSignosVitales() {
        if (!tieneSignosVitalesIngresados()) {
            consulta.setSignos(null);
            return;
        }
        parsearPresionArterial();
        SignosVitales guardados = signosVitalesService.guardar(signosModel, userContextService.resolveCurrentUser());
        consulta.setSignos(guardados);
    }

    private void registrarAuditoriaConsultaGuardada(String user) {
        if (consulta == null) {
            return;
        }
        auditService.registrar(
                "GUARDAR_CONSULTA_MEDICA",
                "CONSULTA_MEDICA",
                "ID_CONSULTA",
                "ID_CONSULTA=" + consulta.getIdConsulta() + ", ID_SIGNOS="
                        + (consulta.getSignos() != null ? consulta.getSignos().getIdSignos() : null)
                        + ", RECETAS=" + (consulta.getRecetas() != null ? consulta.getRecetas().size() : 0),
                user
        );

        if (consulta.getRecetas() == null) {
            return;
        }
        for (RecetaMedica receta : consulta.getRecetas()) {
            auditService.registrar(
                    "GUARDAR_RECETA",
                    "RECETA_MEDICA",
                    "ID_RECETA",
                    "ID_CONSULTA=" + consulta.getIdConsulta() + ", ID_RECETA="
                            + (receta != null ? receta.getIdReceta() : null),
                    user
            );
            if (receta == null || receta.getItems() == null) {
                continue;
            }
            for (RecetaItem item : receta.getItems()) {
                auditService.registrar(
                        "GUARDAR_RECETA_ITEM",
                        "RECETA_ITEM",
                        "ID_ITEM",
                        "ID_RECETA=" + receta.getIdReceta() + ", ID_ITEM=" + item.getIdItem(),
                        user
                );
            }
        }
    }

    private boolean tieneSignosVitalesIngresados() {
        return signosModel != null
                && (signosModel.getTemperaturaC() != null
                || !isBlank(paStr)
                || signosModel.getFrecuenciaCard() != null
                || signosModel.getFrecuenciaResp() != null
                || signosModel.getSatO2() != null
                || signosModel.getPesoKg() != null
                || signosModel.getTallaM() != null
                || signosModel.getPerimetroAbdCm() != null);
    }

    private void parsearPresionArterial() {
        if (isBlank(paStr)) {
            signosModel.setPaSistolica(null);
            signosModel.setPaDiastolica(null);
            return;
        }
        String[] partes = paStr.trim().split("/");
        if (partes.length != 2) {
            return;
        }
        try {
            signosModel.setPaSistolica(Integer.valueOf(partes[0].trim()));
            signosModel.setPaDiastolica(Integer.valueOf(partes[1].trim()));
        } catch (NumberFormatException ignored) {
            // se conserva el texto ingresado por el usuario en paStr
        }
    }

    private List<RecetaMedica> construirRecetasParaPersistencia() {
        List<RecetaItem> items = mapearItemsReceta();
        if (items.isEmpty()) {
            return new ArrayList<>();
        }
        RecetaMedica recetaMedica = crearRecetaMedica(items);
        List<RecetaMedica> resultado = new ArrayList<>();
        resultado.add(recetaMedica);
        return resultado;
    }

    private List<RecetaItem> mapearItemsReceta() {
        List<RecetaItem> items = new ArrayList<>();
        for (RecetaItemForm fila : recetas) {
            if (fila == null || isBlank(fila.getMedicamento())) continue;
            RecetaItem item = new RecetaItem();
            item.setMedicamento(fila.getMedicamento().trim());
            item.setDosis(normalizarTexto(fila.getCantidad()));
            item.setFrecuencia(null);
            item.setVia(normalizarTexto(fila.getVia()));
            item.setDuracionDias(fila.getDuracionDias());
            item.setIndicaciones(normalizarTexto(fila.getIndicaciones()));
            item.setEstado("ACTIVO");
            items.add(item);
        }
        return items;
    }

    private RecetaMedica crearRecetaMedica(List<RecetaItem> items) {
        RecetaMedica recetaMedica = new RecetaMedica();
        recetaMedica.setConsulta(consulta);
        recetaMedica.setFechaEmision(vigenciaReceta != null ? vigenciaReceta : consulta.getFechaConsulta());
        recetaMedica.setIndicaciones(construirIndicacionesRecetaCabecera());
        recetaMedica.setEstado("ACTIVA");
        for (RecetaItem item : items) item.setReceta(recetaMedica);
        recetaMedica.setItems(items);
        return recetaMedica;
    }

    private String construirIndicacionesRecetaCabecera() {
        StringBuilder sb = new StringBuilder();
        if (!isBlank(recomendaciones)) {
            sb.append("Recomendaciones no farmacológicas: ").append(recomendaciones.trim());
        }
        if (!isBlank(signosAlarma)) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append("Signos de alarma: ").append(signosAlarma.trim());
        }
        return sb.toString();
    }

    public List<String> completarDiagnosticoCodigo(String query) {
        return cie10LookupService.completarFilaPorCodigo(query);
    }

    public List<String> completarDiagnosticoDescripcion(String query) {
        return cie10LookupService.completarFilaPorDescripcion(query, 20);
    }

    public void onDiagnosticoCodigoSelect(SelectEvent<String> event) {
        ConsultaDiagnostico diag = resolveDiagFromEvent(event != null ? event.getComponent() : null);
        if (diag == null || event == null) {
            return;
        }
        String codigo = cie10LookupService.extraerCodigoDeSugerencia(event.getObject());
        diag.setCodigo(codigo);
        var cie = cie10Service.buscarPorCodigo(codigo);
        if (cie != null) {
            diag.setDescripcion(cie.getDescripcion());
            diag.setCie10(cie);
        }
    }

    public void onDiagnosticoDescripcionSelect(SelectEvent<String> event) {
        ConsultaDiagnostico diag = resolveDiagFromEvent(event != null ? event.getComponent() : null);
        if (diag == null || event == null) {
            return;
        }
        String descripcion = event.getObject();
        diag.setDescripcion(descripcion);
        var cie = cie10Service.buscarPrimeroPorDescripcion(descripcion);
        if (cie != null) {
            diag.setCodigo(cie.getCodigo());
            diag.setCie10(cie);
        }
    }

    public void onDiagnosticoCodigoBlur(AjaxBehaviorEvent event) {
        ConsultaDiagnostico diag = resolveDiagFromEvent(event != null ? event.getComponent() : null);
        if (diag == null) {
            return;
        }
        String typed = getTypedAutoCompleteValue(event.getComponent());
        String codigo = cie10LookupService.extraerCodigoDeSugerencia(typed);
        if (isBlank(codigo)) {
            return;
        }
        diag.setCodigo(codigo);
        var cie = cie10Service.buscarPorCodigo(codigo);
        if (cie != null) {
            diag.setDescripcion(cie.getDescripcion());
            diag.setCie10(cie);
        }
    }

    public void onDiagnosticoDescripcionBlur(AjaxBehaviorEvent event) {
        ConsultaDiagnostico diag = resolveDiagFromEvent(event != null ? event.getComponent() : null);
        if (diag == null) {
            return;
        }
        String descripcion = getTypedAutoCompleteValue(event.getComponent());
        if (isBlank(descripcion)) {
            return;
        }
        diag.setDescripcion(descripcion);
        var cie = cie10Service.buscarPrimeroPorDescripcion(descripcion.trim());
        if (cie != null) {
            diag.setCodigo(cie.getCodigo());
            diag.setCie10(cie);
        }
    }

    private ConsultaDiagnostico resolveDiagFromEvent(UIComponent component) {
        if (component == null) {
            return null;
        }
        Object idxObj = component.getAttributes().get("idx");
        if (idxObj == null) {
            return null;
        }
        int idx;
        try {
            idx = Integer.parseInt(String.valueOf(idxObj));
        } catch (NumberFormatException ex) {
            return null;
        }
        if (idx < 0 || idx >= diagnosticos.size()) {
            return null;
        }
        return diagnosticos.get(idx);
    }

    private String getTypedAutoCompleteValue(UIComponent component) {
        if (component == null) {
            return null;
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null) {
            return null;
        }
        String base = component.getClientId(fc);
        Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
        String value = params.get(base + "_input");
        if (value == null) {
            value = params.get(base);
        }
        return value;
    }

    public void generarPdfReceta() {
        if (!hasPacienteSeleccionado()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Paciente requerido", "Busque un paciente para generar el PDF.");
            return;
        }
        String html = construirHtmlReceta();
        tokenPdf = centroMedicoPdfFacade.generarDesdeHtml(html, "CONS_");
        addMessage(FacesMessage.SEVERITY_INFO, "PDF generado", "Ya puede visualizar o descargar la receta.");
    }

    private void enviarRecetaPdfACorreoInstitucional() {
        DatEmpleado empleadoActual = resolveEmpleadoParaConsulta();
        if (empleadoActual == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Correo no enviado",
                    "No se encontró el registro DAT_EMPLEADO para notificar la atención.");
            return;
        }
        String correoInstitucional = normalizeEmail(empleadoActual.getEmailInstitucional());
        if (isBlank(correoInstitucional)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Correo no enviado",
                    "El paciente no tiene EMAIL_INSTITUCIONAL registrado en DAT_EMPLEADO.");
            return;
        }

        Date fechaAtencion = consulta != null && consulta.getFechaConsulta() != null ? consulta.getFechaConsulta() : new Date();
        boolean tieneRecetas = consulta != null && consulta.getRecetas() != null && !consulta.getRecetas().isEmpty();
        try {
            if (tieneRecetas) {
                String html = construirHtmlReceta();
                tokenPdf = centroMedicoPdfFacade.generarDesdeHtml(html, "CONS_");
                byte[] recetaPdf = centroMedicoPdfFacade.obtenerPdf(tokenPdf);
                if (recetaPdf == null || recetaPdf.length == 0) {
                    addMessage(FacesMessage.SEVERITY_WARN, "Correo no enviado",
                            "No se pudo adjuntar el PDF de la receta.");
                    return;
                }
                medicalNotificationService.enviarRecetaMedicaAtencion(
                        correoInstitucional,
                        getNombrePaciente(),
                        fechaAtencion,
                        recetaPdf,
                        buildRecetaPdfFileName(fechaAtencion));
                addMessage(FacesMessage.SEVERITY_INFO, "Correo enviado",
                        "La receta fue enviada con PDF adjunto a " + correoInstitucional + ".");
                return;
            }

            medicalNotificationService.enviarRecetaMedicaAtencion(
                    correoInstitucional,
                    getNombrePaciente(),
                    fechaAtencion);
            addMessage(FacesMessage.SEVERITY_INFO, "Correo enviado",
                    "Se notificó la atención realizada a " + correoInstitucional + ".");
        } catch (Exception ex) {
            addMessage(FacesMessage.SEVERITY_WARN, "Correo no enviado",
                    "No se pudo enviar la notificación al correo institucional.");
        }
    }

    private String buildRecetaPdfFileName(Date fechaAtencion) {
        Date fecha = fechaAtencion != null ? fechaAtencion : new Date();
        return "receta-" + new SimpleDateFormat("yyyyMMdd").format(fecha) + ".pdf";
    }

    public void prepararDialogoCertificado() {
        if (!hasPacienteSeleccionado()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Paciente requerido", "Busque un paciente antes de generar certificado.");
            return;
        }
        if (certFechaInicio == null) {
            certFechaInicio = consulta.getFechaConsulta() != null ? consulta.getFechaConsulta() : new Date();
        }
        if (certFechaFin == null) {
            certFechaFin = certFechaInicio;
        }
        if (isBlank(certMedicoCargo)) {
            certMedicoCargo = "MEDICO SALUD OCUPACIONAL";
        }
        if (isBlank(certDomicilio)) {
            certDomicilio = resolveDireccionPaciente();
        }
        if (isBlank(certCargoPaciente)) {
            certCargoPaciente = resolveCargoPaciente();
        }
        if (isBlank(certAreaTrabajo)) {
            certAreaTrabajo = getAreaTrabajoPaciente();
        }
    }

    public void generarPdfCertificado() {
        if (!hasPacienteSeleccionado()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Paciente requerido", "Busque un paciente para generar el certificado.");
            return;
        }
        if (certFechaInicio == null || certFechaFin == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Fechas requeridas", "Ingrese fecha inicio y fin para el certificado.");
            return;
        }
        if (certFechaFin.before(certFechaInicio)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Rango inválido", "La fecha fin no puede ser menor a la fecha inicio.");
            return;
        }
        if (!validarCamposObligatoriosCertificado()) {
            return;
        }
        certTelefono = normalizePhone(certTelefono);
        certMedicoTelefono = normalizePhone(certMedicoTelefono);
        certMedicoCorreo = normalizeEmail(certMedicoCorreo);
        String html = construirHtmlCertificado();
        tokenPdfCertificado = centroMedicoPdfFacade.generarDesdeHtml(html, "CERT_MED_");
        addMessage(FacesMessage.SEVERITY_INFO, "Certificado generado", "Ya puede visualizar o descargar el certificado.");
    }

    public String getPdfUrl() {
        String token = tokenPdfCertificado != null ? tokenPdfCertificado : tokenPdf;
        return token == null ? null : FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/pdf?token=" + token;
    }

    public String getPdfDownloadUrl() {
        String token = tokenPdfCertificado != null ? tokenPdfCertificado : tokenPdf;
        return token == null ? null : FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/pdf?download=1&amp;token=" + token;
    }

    public String getRecetaPdfUrl() {
        return tokenPdf == null ? null : FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/pdf?token=" + tokenPdf;
    }

    public String getRecetaPdfDownloadUrl() {
        return tokenPdf == null ? null : FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/pdf?download=1&amp;token=" + tokenPdf;
    }

    public String getCertificadoPdfUrl() {
        return tokenPdfCertificado == null ? null : FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/pdf?token=" + tokenPdfCertificado;
    }

    public String getCertificadoPdfDownloadUrl() {
        return tokenPdfCertificado == null ? null : FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/pdf?download=1&amp;token=" + tokenPdfCertificado;
    }

    private String construirHtmlReceta() {
        String fecha = fechaEnLetrasCompleta(consulta.getFechaConsulta());
        String medicoNombre = consulta.getMedicoNombre() == null ? "" : consulta.getMedicoNombre();
        String medicoMsp = consulta.getMedicoCodigo() == null ? "" : consulta.getMedicoCodigo();
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html xmlns='http://www.w3.org/1999/xhtml'><head><meta charset='UTF-8'/>")
                .append("<style>")
                .append(recetaStyles())
                .append("</style></head><body>");
        html.append(construirColumnaReceta(fecha, getNombrePaciente(), getCedulaPaciente(), calcularEdadTexto(fechaNacimientoPaciente),
                medicoNombre, medicoMsp, resolveLogo("LOGO_IGM_FULL_COLOR.png"), resolveLogo("LOGO_MIDENA.png")));
        html.append("</body></html>");
        return html.toString();
    }

    private String recetaStyles() {
        return "@page{size:A5 landscape;margin:7mm;}body{font-family:Arial,sans-serif;font-size:11px;margin:0;color:#000;}"
                + ".panel{position:relative;border:1px solid #9ca3af;padding:10px 12px 14px 12px;}.encabezado{border-bottom:2px solid #1f2937;margin-bottom:7px;padding-bottom:5px;}"
                + ".encabezado-table{width:100%;border-collapse:collapse;}.encabezado-table td{vertical-align:middle;}.logo-cell{width:105px;text-align:center;}"
                + ".logo{width:62px;height:62px;display:block;margin:0 auto;object-fit:contain;}.encabezado-titulo{text-align:center;font-size:12px;font-weight:700;letter-spacing:.3px;margin:1px 0 0 0;}"
                + ".encabezado-sub{font-size:10px;color:#374151;text-align:center;margin:2px 0 0 0;}.titulo{font-weight:bold;margin-top:8px;margin-bottom:3px;}.row{margin-bottom:3px;white-space:normal;word-break:break-word;overflow-wrap:anywhere;line-height:1.2;}"
                + ".texto-libre{font-size:10.5px;white-space:pre-wrap;word-break:break-word;overflow-wrap:anywhere;line-height:1.2;}"
                + "table{width:100%;border-collapse:collapse;table-layout:fixed;font-size:10.5px;}th,td{padding:2px 2px;vertical-align:top;word-wrap:break-word;overflow-wrap:anywhere;}"
                + ".firmante{margin-top:16px;text-align:center;}.firma-linea{border-top:1px solid #000;width:220px;margin:0 auto 6px auto;}.small{font-size:9px;}";
    }

    private String construirColumnaReceta(String fecha, String nombrePaciente, String cedula, String edad,
            String medicoNombre, String medicoMsp, String logoIgm, String logoMidena) {
        StringBuilder sb = new StringBuilder();
        appendRecetaEncabezado(sb, fecha, nombrePaciente, cedula, edad, logoIgm, logoMidena);
        appendRecetaMedicamentos(sb);
        appendRecetaPie(sb, medicoNombre, medicoMsp);
        return sb.toString();
    }

    private void appendRecetaEncabezado(StringBuilder sb, String fecha, String nombrePaciente, String cedula,
            String edad, String logoIgm, String logoMidena) {
        sb.append("<div class='panel'><div class='encabezado'><table class='encabezado-table'><tr>")
                .append("<td class='logo-cell'><img class='logo' alt='LOGO_IGM_FULL_COLOR_COMPLETA' src='").append(escape(logoIgm)).append("'/></td>")
                .append("<td><div class='encabezado-titulo'>INSTITUTO GEOGRÁFICO MILITAR</div><div class='encabezado-sub'>DISPENSARIO MÉDICO</div><div class='encabezado-sub'><b>RECETA</b></div></td>")
                .append("<td class='logo-cell'><img class='logo' alt='LOGO_MIDENA_FULL_COLO' src='").append(escape(logoMidena)).append("'/></td>")
                .append("</tr></table></div>")
                .append("<div class='row'>QUITO,").append(escape(fecha).toUpperCase()).append("</div>")
                .append("<div class='row'><b>Paciente:</b> ").append(escape(nombrePaciente)).append("</div>")
                .append("<div class='row'><b>Cédula:</b> ").append(escape(cedula)).append("</div>")
                .append("<div class='row'><b>Sexo:</b> ").append(escape(getSexoPaciente())).append("</div>")
                .append("<div class='row'><b>Edad:</b> ").append(escape(edad)).append("</div>");
    }

    private void appendRecetaMedicamentos(StringBuilder sb) {
        sb.append("<div class='titulo'>Antecedentes Patológicos Personales:</div>")
                .append("<div class='row'>").append(escape(resolveAlergiasTexto())).append("</div>")
                .append("<table><colgroup><col style='width:24%'/><col style='width:12%'/><col style='width:12%'/><col style='width:8%'/>")
                .append("<col style='width:44%'/></colgroup><thead><tr><th>Medicamento</th><th>Vía</th><th>Cantidad</th><th>Días</th><th>Indicaciones</th></tr></thead><tbody>");
        for (RecetaItemForm item : recetas) {
            if (item == null || isBlank(item.getMedicamento())) {
                continue;
            }
            sb.append("<tr><td>").append(escape(item.getMedicamento())).append("</td><td>")
                    .append(escape(item.getVia())).append("</td><td>")
                    .append(escape(item.getCantidad())).append("</td><td>")
                    .append(item.getDuracionDias() == null ? "" : item.getDuracionDias()).append("</td><td>")
                    .append(escape(item.getIndicaciones())).append("</td></tr>");
        }
    }

    private void appendRecetaPie(StringBuilder sb, String medicoNombre, String medicoMsp) {
        sb.append("</tbody></table><div class='titulo'>Recomendaciones no farmacológicas:</div>")
                .append("<div class='row texto-libre'>").append(escape(recomendaciones)).append("</div>")
                .append("<div class='titulo'>Signos de alarma:</div>")
                .append("<div class='row texto-libre'>").append(escape(signosAlarma)).append("</div>")
                .append("<div class='firmante'><div><b>Médico:</b> ").append(escape(medicoNombre)).append("</div>")
                .append("<div><b>MSP:</b> ").append(escape(medicoMsp)).append("</div></div></div>");
    }

    /**
     * Certificado médico de Consulta Médica (no usa la plantilla de aptitud).
     */
    private String construirHtmlCertificado() {
        CertificadoData data = buildCertificadoData();
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/><style>")
                .append(certificadoStyles())
                .append("</style></head><body>");
        appendCertificadoContenido(html, data);
        appendCertificadoMembrete(html, data.membreteBottom);
        html.append("</body></html>");
        return html.toString();
    }

    private CertificadoData buildCertificadoData() {
        CertificadoData data = new CertificadoData();
        data.nombrePaciente = getNombrePaciente();
        data.cedula = getCedulaPaciente();
        data.numeroHistoria = getCedulaPaciente();
        data.cargoPaciente = isBlank(certCargoPaciente) ? resolveCargoPaciente() : certCargoPaciente;
        data.areaTrabajo = isBlank(certAreaTrabajo) ? getAreaTrabajoPaciente() : certAreaTrabajo;
        data.diagnosticoPaciente = isBlank(getDiagnosticosTexto()) ? "NO REGISTRA" : getDiagnosticosTexto();
        data.sintomasPaciente = isBlank(consulta.getEnfermedadActual()) ? "NO REGISTRA" : consulta.getEnfermedadActual();
        data.fechaInicioTexto = formatoFechaDiaMesAnioConLetras(certFechaInicio);
        data.fechaFinTexto = formatoFechaDiaMesAnioConLetras(certFechaFin);
        data.diasReposo = getCertDiasReposo();
        data.telefonoContacto = valueOrNoRegistra(certTelefono);
        data.medicoMsp = consulta.getMedicoCodigo() == null ? "" : consulta.getMedicoCodigo();
        data.medicoTelefono = valueOrNoRegistra(certMedicoTelefono);
        data.medicoCorreo = isBlank(certMedicoCorreo) ? "NO REGISTRA" : certMedicoCorreo;
        data.tipoContingencia = valueOrNoRegistra(certTipoContingencia);
        data.domicilioPaciente = valueOrNoRegistra(certDomicilio);
        data.fechaEmision = formatoFechaNumericoYLetras(consulta.getFechaConsulta() != null ? consulta.getFechaConsulta() : new Date());
        data.logoIgm = resolveLogo("LOGO_IGM_FULL_COLOR_COMPLETA.png");
        data.logoMidena = resolveLogo("LOGO_MIDENA_FULL_COLO.png");
        data.membreteBottom = resolveLogo("membrete-bottom.png");
        return data;
    }

    private String certificadoStyles() {
        return "@page{size:A4;margin:10mm 12mm 8mm 12mm;}body{font-family:Arial,sans-serif;font-size:16px;line-height:1.2;margin:0;color:#000000;}"
                + ".contenido{padding:0 18mm 112px 18mm;box-sizing:border-box;}.encabezado{margin:0 0 12px 0;text-align:center;}.encabezado-table{width:100%;border-collapse:collapse;table-layout:fixed;}"
                + ".encabezado-table td{vertical-align:middle;text-align:center;padding:0 8px;}.header-logo-cell{width:50%;}.logo-midena{display:block;width:170px;height:auto;margin:0 auto;}"
                + ".logo-igm{display:block;width:170px;height:auto;margin:0 auto;}.titulo{text-align:center;font-size:22px;font-weight:700;letter-spacing:.2px;margin:6px 0 12px 0;}"
                + ".cuerpo-certificado{width:100%;margin:0;box-sizing:border-box;}.texto{font-size:16px;margin:0 0 6px 0;text-align:justify;}"
                + ".pie{width:100%;margin:8px 0 0 0;text-align:center;box-sizing:border-box;}.firma{text-align:center;font-size:16px;margin:0;}"
                + ".firma-bloque{text-align:center;margin:0;}.correo{color:#0000EE;text-decoration:underline;}.membrete-bottom{position:fixed;left:12mm;right:12mm;bottom:4mm;width:auto;}"
                + ".membrete-bottom table{width:100%;border-collapse:collapse;table-layout:fixed;}.membrete-bottom td{vertical-align:bottom;}.membrete-bottom .mb-img{width:185px;text-align:left;}"
                + ".membrete-bottom .mb-img img{display:block;width:165px;height:auto;margin:0;object-fit:contain;}.membrete-bottom .mb-text{text-align:right;font-family:Arial,sans-serif;font-size:11px;line-height:1.15;color:#b3b3b3;font-weight:600;padding-bottom:6px;}";
    }

    private void appendCertificadoContenido(StringBuilder html, CertificadoData data) {
        html.append("<div class='contenido'>");
        appendCertificadoEncabezado(html, data.logoMidena, data.logoIgm);
        appendCertificadoCuerpo(html, data);
        appendCertificadoFirma(html, data);
        html.append("</div>");
    }

    private void appendCertificadoEncabezado(StringBuilder html, String logoMidena, String logoIgm) {
        html.append("<div class='encabezado'><table class='encabezado-table'><tr>")
                .append("<td class='header-logo-cell'><img class='logo-midena' alt='LOGO_MIDENA' src='").append(escape(logoMidena)).append("'/></td>")
                .append("<td class='header-logo-cell'><img class='logo-igm' alt='LOGO_IGM_FULL_COLOR' src='").append(escape(logoIgm)).append("'/></td>")
                .append("</tr></table></div><div class='titulo'>CERTIFICADO MEDICO</div>");
    }

    private void appendCertificadoCuerpo(StringBuilder html, CertificadoData data) {
        html.append("<div class='cuerpo-certificado'><p class='texto'>EL MEDICO CERTIFICA:</p><p class='texto'>El señor ")
                .append(escape(data.nombrePaciente)).append(" con Cédula de Ciudadanía ").append(escape(data.cedula))
                .append(", presenta diagnóstico: ").append(escape(data.diagnosticoPaciente))
                .append("; por lo que requiere reposo médico absoluto durante ").append(data.diasReposo).append(" (")
                .append(escape(numeroEnLetras((int) data.diasReposo))).append(") día(s).</p><p class='texto'><b>Historia clínica:</b> ")
                .append(escape(data.numeroHistoria)).append("</p><p class='texto'><b>Desde:</b> ").append(escape(data.fechaInicioTexto))
                .append(".<br/><b>Hasta:</b> ").append(escape(data.fechaFinTexto))
                .append(".</p><p class='texto'>Labora en el <b>INSTITUTO GEOGRAFICO MILITAR (ubicado en Seniergues E4-676 y Gral. Telmo Paz y Miño Sector El Dorado)</b>, con el cargo de ")
                .append(escape(data.cargoPaciente)).append(", en el área de ").append(escape(data.areaTrabajo))
                .append(".</p><p class='texto'>Paciente sintomático quien presenta: ").append(escape(data.sintomasPaciente))
                .append(".</p><p class='texto'><b>Domicilio del paciente:</b> ").append(escape(data.domicilioPaciente))
                .append("<br/><b>Teléfono de contacto:</b> ").append(escape(data.telefonoContacto))
                .append("<br/><b>Tipo de contingencia:</b> <b>").append(escape(data.tipoContingencia))
                .append("</b></p><p class='texto'>Quito DM, ").append(escape(data.fechaEmision)).append(".</p></div>");
    }

    private void appendCertificadoFirma(StringBuilder html, CertificadoData data) {
        html.append("<div class='pie'><p class='firma'>Atentamente,<br/><br/><br/><br/><br/><br/><br/><br/><br/></p><p class='firma firma-bloque'>")
                .append(escape(consulta.getMedicoNombre())).append("<br/>").append(escape(certMedicoCargo)).append("<br/>MSP: ")
                .append(escape(data.medicoMsp)).append("<br/>Teléfono: ").append(escape(data.medicoTelefono))
                .append("<br/><span class='correo'>").append(escape(data.medicoCorreo)).append("</span></p></div>");
    }

    private void appendCertificadoMembrete(StringBuilder html, String membreteBottom) {
        html.append("<div class='membrete-bottom'><table><tr><td class='mb-img'><img alt='membrete-bottom' src='")
                .append(escape(membreteBottom)).append("'/></td><td class='mb-text'>")
                .append("QUITO: Seniergues E4-676 y Gral. Telmo Paz y Miño Sector El Dorado<br/>")
                .append("Teléf.: 593(2) 3975100 al 130 GUAYAQUIL: Av. Guillermo Pareja # 402 Ciudadela la Garzota<br/>")
                .append("Teléf.: 593(4) 26247 597 y 593(4) 2627829")
                .append("</td></tr></table></div>");
    }

    private static class CertificadoData {
        String nombrePaciente;
        String cedula;
        String numeroHistoria;
        String cargoPaciente;
        String areaTrabajo;
        String diagnosticoPaciente;
        String sintomasPaciente;
        String fechaInicioTexto;
        String fechaFinTexto;
        long diasReposo;
        String telefonoContacto;
        String medicoMsp;
        String medicoTelefono;
        String medicoCorreo;
        String tipoContingencia;
        String domicilioPaciente;
        String fechaEmision;
        String logoIgm;
        String logoMidena;
        String membreteBottom;
    }

    private String valueOrNoRegistra(String value) {
        return isBlank(value) ? "NO REGISTRA" : value.trim();
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy/MM/dd").format(date);
    }

    private String calcularEdadTexto(Date nacimiento) {
        if (nacimiento == null) {
            return "";
        }
        LocalDate fn = Instant.ofEpochMilli(nacimiento.getTime()).atZone(CERTIFICADO_ZONE).toLocalDate();
        int years = Period.between(fn, LocalDate.now(CERTIFICADO_ZONE)).getYears();
        return years + " Años";
    }

    private String fechaEnLetras(Date date) {
        if (date == null) {
            return "";
        }
        LocalDate local = toCertLocalDate(date);
        String mes = local.getMonth().getDisplayName(TextStyle.FULL, new java.util.Locale("es", "EC"));
        return local.getDayOfMonth() + " de " + mes + " de " + local.getYear();
    }

    private String fechaEnLetrasCompleta(Date date) {
        if (date == null) {
            return "";
        }
        LocalDate local = toCertLocalDate(date);
        String mes = local.getMonth().getDisplayName(TextStyle.FULL, new java.util.Locale("es", "EC"));
        return numeroEnLetras(local.getDayOfMonth()) + " de " + mes + " de " + anioEnLetras(local.getYear());
    }

    private String formatoFechaNumericoYLetras(Date date) {
        if (date == null) {
            return "";
        }
        LocalDate local = toCertLocalDate(date);
        String mes = local.getMonth().getDisplayName(TextStyle.FULL, new java.util.Locale("es", "EC"));
        return local.getDayOfMonth() + " (" + numeroEnLetras(local.getDayOfMonth()) + ") de " + mes
                + " de " + local.getYear() + " (" + anioEnLetras(local.getYear()) + ")";
    }

    private String formatoFechaDiaMesAnioConLetras(Date date) {
        if (date == null) {
            return "";
        }
        LocalDate local = toCertLocalDate(date);
        String fechaNumerica = String.format("%02d/%02d/%04d",
                local.getDayOfMonth(), local.getMonthValue(), local.getYear());
        return fechaNumerica + " (" + fechaEnLetrasCompleta(date) + ")";
    }

    private LocalDate toCertLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(CERTIFICADO_ZONE).toLocalDate();
    }

    private void sincronizarFechasCertificado() {
        if (certFechaInicio != null && certFechaFin != null && certFechaFin.before(certFechaInicio)) {
            certFechaFin = certFechaInicio;
            return;
        }
        if (certFechaFin == null) {
            certFechaFin = certFechaInicio != null ? certFechaInicio : truncateTime(new Date());
        }
    }

    public void onFechaInicioSelect() {
        if (certFechaInicio != null && certFechaFin != null && certFechaFin.before(certFechaInicio)) {
            certFechaFin = certFechaInicio;
        }
        if (certFechaFin == null) {
            certFechaFin = certFechaInicio != null ? certFechaInicio : truncateTime(new Date());
        }
    }

    public void onFechaFinSelect() {
        if (certFechaInicio != null && certFechaFin != null && certFechaFin.before(certFechaInicio)) {
            certFechaFin = certFechaInicio;
            addMessage(FacesMessage.SEVERITY_WARN, "Fecha fin ajustada",
                    "La fecha fin no puede ser menor a la fecha inicio.");
        }
        sincronizarFechasCertificado();
    }

    public void onFechaNacimientoPacienteChange() {
        // No-op: fuerza roundtrip AJAX para refrescar edad con la fecha nac. editada.
    }

    public Date getCertFechaFinMin() {
        return certFechaInicio;
    }

    public Date getCertFechaInicioMax() {
        return certFechaFin;
    }

    private Date sumarDias(Date base, int dias) {
        if (base == null) {
            return null;
        }
        LocalDate local = toCertLocalDate(base);
        LocalDate ajustada = local.plusDays(dias);
        return Date.from(ajustada.atStartOfDay(CERTIFICADO_ZONE).toInstant());
    }

    private Date truncateTime(Date fecha) {
        if (fecha == null) {
            return null;
        }
        LocalDate local = toCertLocalDate(fecha);
        return Date.from(local.atStartOfDay(CERTIFICADO_ZONE).toInstant());
    }

    private boolean validarCamposObligatoriosCertificado() {
        List<String> faltantes = new ArrayList<>();
        if (getCertDiasReposo() <= 0) {
            faltantes.add("días de reposo");
        }
        if (isBlank(certDomicilio)) {
            faltantes.add("domicilio");
        }
        if (isBlank(certTelefono)) {
            faltantes.add("teléfono de contacto");
        }
        if (isBlank(certTipoContingencia)) {
            faltantes.add("tipo de contingencia");
        }
        if (isBlank(certMedicoTelefono)) {
            faltantes.add("teléfono del médico");
        }
        if (isBlank(certMedicoCorreo)) {
            faltantes.add("correo del médico");
        }
        if (!faltantes.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Campos requeridos",
                    "Complete: " + String.join(", ", faltantes) + ".");
            return false;
        }
        return true;
    }

    private String resolveCargoPaciente() {
        if (empleado != null && !isBlank(empleado.getCargoLossca())) {
            return empleado.getCargoLossca();
        }
        var datosLaboralesRh = resolveDatosLaboralesRh();
        if (datosLaboralesRh != null && !isBlank(datosLaboralesRh.getCargoDescrip())) {
            return datosLaboralesRh.getCargoDescrip();
        }
        if (fichaReferencia != null && !isBlank(fichaReferencia.getCiiu())) {
            return fichaReferencia.getCiiu();
        }
        String cedula = getCedulaPaciente();
        if (empleadoRhService == null || isBlank(cedula)) {
            return "";
        }
        var cargoRh = empleadoRhService.buscarPorCedulaEnVista(cedula);
        return cargoRh != null && !isBlank(cargoRh.getCargoDescrip()) ? cargoRh.getCargoDescrip() : "";
    }

    public long getCertDiasReposo() {
        if (certFechaInicio == null || certFechaFin == null) {
            return 0L;
        }
        LocalDate inicio = toCertLocalDate(certFechaInicio);
        LocalDate fin = toCertLocalDate(certFechaFin);
        long dias = ChronoUnit.DAYS.between(inicio, fin) + 1;
        return Math.max(dias, 0L);
    }

    public String getCertDiasReposoLetras() {
        return numeroEnLetras((int) getCertDiasReposo());
    }

    private String normalizePhone(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D", "");
    }

    private String normalizeEmail(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public String getCertCargoPaciente() {
        return certCargoPaciente;
    }

    public void setCertCargoPaciente(String certCargoPaciente) {
        this.certCargoPaciente = certCargoPaciente;
    }

    public String getCertAreaTrabajo() {
        return certAreaTrabajo;
    }

    public void setCertAreaTrabajo(String certAreaTrabajo) {
        this.certAreaTrabajo = certAreaTrabajo;
    }

    public String getDireccionPaciente() {
        return resolveDireccionPaciente();
    }

    public String getAreaTrabajoPaciente() {
        if (!isBlank(certAreaTrabajo)) {
            return certAreaTrabajo;
        }
        var datosLaboralesRh = resolveDatosLaboralesRh();
        if (datosLaboralesRh != null && !isBlank(datosLaboralesRh.getAreaDescrip())) {
            return datosLaboralesRh.getAreaDescrip();
        }
        if (fichaReferencia != null && !isBlank(fichaReferencia.getEstablecimientoCt())) {
            return fichaReferencia.getEstablecimientoCt();
        }
        return "";
    }

    public void setAreaTrabajoPaciente(String areaTrabajoPaciente) {
        this.certAreaTrabajo = areaTrabajoPaciente;
    }

    public String getNombrePaciente() {
        if (empleado != null && !isBlank(empleado.getNombreC())) {
            return empleado.getNombreC();
        }
        if (personaAux == null) {
            return "";
        }
        StringBuilder nombre = new StringBuilder();
        appendParte(nombre, personaAux.getApellido1());
        appendParte(nombre, personaAux.getApellido2());
        appendParte(nombre, personaAux.getNombre1());
        appendParte(nombre, personaAux.getNombre2());
        if (nombre.length() == 0) {
            appendParte(nombre, personaAux.getApellidos());
            appendParte(nombre, personaAux.getNombres());
        }
        return nombre.toString();
    }

    private void appendParte(StringBuilder sb, String valor) {
        if (isBlank(valor)) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(valor.trim());
    }

    public String getSexoPaciente() {
        if (empleado != null && empleado.getSexo() != null) {
            return empleado.getSexo().getDescripcion();
        }
        if (personaAux == null || isBlank(personaAux.getSexo())) {
            return "";
        }
        return "M".equalsIgnoreCase(personaAux.getSexo()) ? "MASCULINO" : "FEMENINO";
    }

    public String getCedulaPaciente() {
        if (empleado != null && !isBlank(empleado.getNoCedula())) {
            return empleado.getNoCedula();
        }
        return personaAux != null ? personaAux.getCedula() : "";
    }

    private String resolveDireccionPaciente() {
        if (empleado != null && !isBlank(empleado.getDireccion())) {
            return empleado.getDireccion();
        }
        return certDomicilio;
    }

    private String resolveDireccionLaboral() {
        var datosLaboralesRh = resolveDatosLaboralesRh();
        if (datosLaboralesRh != null && !isBlank(datosLaboralesRh.getAreaDescrip())) {
            return datosLaboralesRh.getAreaDescrip();
        }
        return "Seniergues E4-676 y Gral. Telmo Paz y Miño Sector El Dorado";
    }

    private EmpleadoCargoDTO resolveDatosLaboralesRh() {
        String cedula = getCedulaPaciente();
        if (empleadoRhService == null || isBlank(cedula)) {
            return null;
        }
        return empleadoRhService.buscarDatosLaboralesVigentesPorCedula(cedula);
    }

    private String obtenerAntecedenteClinicoQuirurgico() {
        if (fichaReferencia != null && !isBlank(fichaReferencia.getAntClinicoQuir())) {
            return fichaReferencia.getAntClinicoQuir();
        }
        if (empleado != null && !isBlank(empleado.getAlergia())) {
            return empleado.getAlergia();
        }
        return null;
    }

    private boolean hasPacienteSeleccionado() {
        return empleado != null || personaAux != null;
    }

    private DatEmpleado resolveEmpleadoParaConsulta() {
        if (empleado != null) {
            return empleado;
        }
        if (personaAux != null && personaAux.getNoPersona() != null) {
            Long noPersonaAux = personaAux.getNoPersona();
            if (noPersonaAux <= Integer.MAX_VALUE && noPersonaAux >= Integer.MIN_VALUE) {
                DatEmpleado encontrado = empleadoService.buscarPorId(noPersonaAux.intValue());
                if (encontrado != null) {
                    empleado = encontrado;
                    return encontrado;
                }
                DatEmpleado referencia = new DatEmpleado();
                referencia.setNoPersona(noPersonaAux.intValue());
                empleado = referencia;
                return referencia;
            }
        }
        if (fichaReferencia != null && fichaReferencia.getEmpleado() != null) {
            empleado = fichaReferencia.getEmpleado();
            return empleado;
        }
        return null;
    }

    private String buildNombreCompleto(PersonaAux aux) {
        StringBuilder sb = new StringBuilder();
        appendParte(sb, aux.getApellido1());
        appendParte(sb, aux.getApellido2());
        appendParte(sb, aux.getNombre1());
        appendParte(sb, aux.getNombre2());
        if (sb.length() == 0) {
            appendParte(sb, aux.getApellidos());
            appendParte(sb, aux.getNombres());
        }
        return sb.length() == 0 ? "EXTERNO" : sb.toString();
    }

    private String buildNombres(PersonaAux aux) {
        StringBuilder sb = new StringBuilder();
        appendParte(sb, aux.getNombre1());
        appendParte(sb, aux.getNombre2());
        if (sb.length() == 0) {
            appendParte(sb, aux.getNombres());
        }
        return sb.length() == 0 ? "EXTERNO" : sb.toString();
    }

    private String firstNonBlank(String first, String second) {
        if (!isBlank(first)) {
            return first.trim();
        }
        if (!isBlank(second)) {
            return second.trim();
        }
        return "EXTERNO";
    }

    public String getDiagnosticoPrincipal() {
        if (diagnosticos == null) {
            return "";
        }
        for (ConsultaDiagnostico d : diagnosticos) {
            if (d != null && (!isBlank(d.getCodigo()) || !isBlank(d.getDescripcion()))) {
                return (d.getCodigo() == null ? "" : d.getCodigo() + " ") + (d.getDescripcion() == null ? "" : d.getDescripcion());
            }
        }
        return "";
    }

    public String getDiagnosticosTexto() {
        if (diagnosticos == null) {
            return "";
        }
        List<String> valores = new ArrayList<>();
        for (ConsultaDiagnostico d : diagnosticos) {
            if (d == null || (isBlank(d.getCodigo()) && isBlank(d.getDescripcion()))) {
                continue;
            }
            String texto = ((d.getCodigo() == null ? "" : d.getCodigo().trim()) + " "
                    + (d.getDescripcion() == null ? "" : d.getDescripcion().trim())).trim();
            if (!texto.isEmpty()) {
                valores.add(texto);
            }
        }
        return String.join("; ", valores);
    }

    public String obtenerDiagnosticosConsulta(ConsultaMedica consultaHistorica) {
        if (consultaHistorica == null || consultaHistorica.getDiagnosticos() == null) {
            return "";
        }
        List<String> valores = new ArrayList<>();
        for (ConsultaDiagnostico d : consultaHistorica.getDiagnosticos()) {
            if (d == null || (isBlank(d.getCodigo()) && isBlank(d.getDescripcion()))) {
                continue;
            }
            String texto = ((d.getCodigo() == null ? "" : d.getCodigo().trim()) + " "
                    + (d.getDescripcion() == null ? "" : d.getDescripcion().trim())).trim();
            if (!texto.isEmpty()) {
                valores.add(texto);
            }
        }
        return String.join("; ", valores);
    }

    public String obtenerRecetaConsulta(ConsultaMedica consultaHistorica) {
        if (consultaHistorica == null || consultaHistorica.getRecetas() == null) {
            return "";
        }
        List<String> valores = new ArrayList<>();
        for (RecetaMedica recetaHistorica : consultaHistorica.getRecetas()) {
            if (recetaHistorica == null || recetaHistorica.getItems() == null) {
                continue;
            }
            for (RecetaItem item : recetaHistorica.getItems()) {
                if (item == null || isBlank(item.getMedicamento())) {
                    continue;
                }
                StringBuilder texto = new StringBuilder(item.getMedicamento().trim());
                if (!isBlank(item.getVia())) {
                    texto.append(" (").append(item.getVia().trim()).append(")");
                }
                if (!isBlank(item.getDosis())) {
                    texto.append(" ").append(item.getDosis().trim());
                }
                if (item.getDuracionDias() != null) {
                    texto.append(" ").append(item.getDuracionDias()).append(" día(s)");
                }
                if (!isBlank(item.getIndicaciones())) {
                    texto.append(": ").append(item.getIndicaciones().trim());
                }
                valores.add(texto.toString());
            }
        }
        return String.join(" | ", valores);
    }

    private String anioEnLetras(int anio) {
        if (anio >= 2001 && anio <= 2099) {
            return "dos mil " + decenasConConector(anio % 100);
        }
        int miles = anio / 1000;
        int resto = anio % 1000;
        String milesTxt = (miles == 2) ? "dos mil" : (numeroEnLetras(miles) + " mil");
        if (resto == 0) {
            return milesTxt;
        }
        return milesTxt + " " + numeroEnLetras(resto);
    }

    private String decenasConConector(int numero) {
        if (numero <= 0) {
            return "";
        }
        if (numero < 30) {
            return numeroEnLetras(numero);
        }
        int d = numero / 10;
        int u = numero % 10;
        String[] decenas = {"", "", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"};
        if (u == 0) {
            return decenas[d];
        }
        return decenas[d] + " y " + numeroEnLetras(u);
    }

    private String numeroEnLetras(int numero) {
        if (numero <= 0) {
            return "cero";
        }
        if (numero < 30) return NUMEROS_BASE[numero];
        if (numero < 100) return numeroDecenasEnLetras(numero);
        if (numero == 100) return "cien";
        if (numero < 1000) return numeroCentenasEnLetras(numero);
        return Integer.toString(numero);
    }

    private String numeroDecenasEnLetras(int numero) {
        int d = numero / 10;
        int u = numero % 10;
        if (u == 0) return DECENAS[d];
        return DECENAS[d] + " y " + numeroEnLetras(u);
    }

    private String numeroCentenasEnLetras(int numero) {
        int c = numero / 100;
        int resto = numero % 100;
        if (resto == 0) return CENTENAS[c];
        return CENTENAS[c] + " " + numeroEnLetras(resto);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizarTexto(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    private String resolveAlergiasTexto() {
        if (!isBlank(alergias)) {
            return alergias;
        }
        if (consulta != null && consulta.getEmpleado() != null && !isBlank(consulta.getEmpleado().getAlergia())) {
            return consulta.getEmpleado().getAlergia();
        }
        return "SIN REGISTRO";
    }

    private String resolveLogo(String fileName) {
        if (pdfResourceResolver == null) {
            return "";
        }
        return pdfResourceResolver.buildLogoDataUri(fileName);
    }

    public String getCedulaBusqueda() { return cedulaBusqueda; }
    public void setCedulaBusqueda(String cedulaBusqueda) { this.cedulaBusqueda = cedulaBusqueda; }
    public DatEmpleado getEmpleado() { return empleado; }
    public ConsultaMedica getConsulta() { return consulta; }
    public List<ConsultaMedica> getConsultasAnteriores() { return consultasAnteriores; }
    public List<ConsultaDiagnostico> getDiagnosticos() { return diagnosticos; }
    public List<RecetaItemForm> getRecetas() { return recetas; }
    public Date getVigenciaReceta() { return vigenciaReceta; }
    public void setVigenciaReceta(Date vigenciaReceta) { this.vigenciaReceta = vigenciaReceta; }
    public String getRecomendaciones() { return recomendaciones; }
    public void setRecomendaciones(String recomendaciones) { this.recomendaciones = recomendaciones; }
    public String getSignosAlarma() { return signosAlarma; }
    public void setSignosAlarma(String signosAlarma) { this.signosAlarma = signosAlarma; }
    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }
    public Date getFechaNacimientoPaciente() { return fechaNacimientoPaciente; }
    public void setFechaNacimientoPaciente(Date fechaNacimientoPaciente) { this.fechaNacimientoPaciente = fechaNacimientoPaciente; }
    public String getEdadPaciente() { return calcularEdadTexto(fechaNacimientoPaciente); }
    public SignosVitales getSignosModel() { return signosModel; }
    public String getPaStr() { return paStr; }
    public void setPaStr(String paStr) { this.paStr = paStr; }

    public BigDecimal getTallaCm() {
        if (signosModel == null || signosModel.getTallaM() == null) {
            return null;
        }
        return signosModel.getTallaM().multiply(BigDecimal.valueOf(100));
    }

    public void setTallaCm(BigDecimal tallaCm) {
        if (signosModel == null) {
            signosModel = new SignosVitales();
        }
        if (tallaCm == null) {
            signosModel.setTallaM(null);
            return;
        }
        signosModel.setTallaM(tallaCm.divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP));
    }

    public boolean isOcultarPanelSignosVitales() {
        return isUsuarioOdontologo();
    }

    private boolean isUsuarioOdontologo() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return false;
        }
        ExternalContext externalContext = context.getExternalContext();
        if (externalContext == null) {
            return false;
        }
        Object role = externalContext.getSessionMap().get("AUTH_ROLE");
        return AUTH_ROLE_ODONTOLOGO.equals(String.valueOf(role));
    }

    public static class RecetaItemForm implements Serializable {
        private String medicamento;
        private String diagnostico;
        private String via;
        private String cantidad;
        private Integer duracionDias;
        private String indicaciones;

        public String getMedicamento() { return medicamento; }
        public void setMedicamento(String medicamento) { this.medicamento = medicamento; }
        public String getDiagnostico() { return diagnostico; }
        public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
        public String getVia() { return via; }
        public void setVia(String via) { this.via = via; }
        public String getCantidad() { return cantidad; }
        public void setCantidad(String cantidad) { this.cantidad = cantidad; }
        public Integer getDuracionDias() { return duracionDias; }
        public void setDuracionDias(Integer duracionDias) { this.duracionDias = duracionDias; }
        public String getIndicaciones() { return indicaciones; }
        public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
    }

    public boolean isGenerarCertificado() { return generarCertificado; }
    public void setGenerarCertificado(boolean generarCertificado) { this.generarCertificado = generarCertificado; }
    public Date getCertFechaInicio() { return certFechaInicio; }
    public void setCertFechaInicio(Date certFechaInicio) {
        this.certFechaInicio = certFechaInicio;
        sincronizarFechasCertificado();
    }
    public Date getCertFechaFin() { return certFechaFin; }
    public void setCertFechaFin(Date certFechaFin) {
        this.certFechaFin = certFechaFin;
        sincronizarFechasCertificado();
    }
    public String getCertFechaInicioLetras() { return fechaEnLetrasCompleta(certFechaInicio); }
    public void setCertFechaInicioLetras(String certFechaInicioLetras) { this.certFechaInicioLetras = certFechaInicioLetras; }
    public String getCertFechaFinLetras() { return fechaEnLetrasCompleta(certFechaFin); }
    public void setCertFechaFinLetras(String certFechaFinLetras) { this.certFechaFinLetras = certFechaFinLetras; }
    public String getCertDomicilio() { return certDomicilio; }
    public void setCertDomicilio(String certDomicilio) { this.certDomicilio = certDomicilio; }
    public String getCertTelefono() { return certTelefono; }
    public void setCertTelefono(String certTelefono) { this.certTelefono = certTelefono; }
    public String getCertTipoContingencia() { return certTipoContingencia; }
    public void setCertTipoContingencia(String certTipoContingencia) { this.certTipoContingencia = certTipoContingencia; }
    public String getCertMedicoCargo() { return certMedicoCargo; }
    public void setCertMedicoCargo(String certMedicoCargo) { this.certMedicoCargo = certMedicoCargo; }
    public String getCertMedicoTelefono() { return certMedicoTelefono; }
    public void setCertMedicoTelefono(String certMedicoTelefono) { this.certMedicoTelefono = certMedicoTelefono; }
    public String getCertMedicoCorreo() { return certMedicoCorreo; }
    public void setCertMedicoCorreo(String certMedicoCorreo) { this.certMedicoCorreo = certMedicoCorreo; }
}
