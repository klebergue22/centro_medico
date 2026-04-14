package ec.gob.igm.rrhh.consultorio.web.ctrl;

import ec.gob.igm.rrhh.consultorio.domain.dto.EmpleadoCargoDTO;
import ec.gob.igm.rrhh.consultorio.domain.enums.Sexo;
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
import ec.gob.igm.rrhh.consultorio.service.SignosVitalesService;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;
import ec.gob.igm.rrhh.consultorio.web.service.Cie10LookupService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
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
    private static final ZoneId CERTIFICADO_ZONE = ZoneId.of("America/Guayaquil");

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
        consulta = new ConsultaMedica();
        consulta.setFechaConsulta(new Date());
        consulta.setEstado("ACTIVO");
        consulta.setDiagnosticos(new ArrayList<>());
        diagnosticos = new ArrayList<>();
        consultasAnteriores = new ArrayList<>();
        recetas = new ArrayList<>();
        vigenciaReceta = new Date();
        signosModel = new SignosVitales();
        certMedicoCargo = "MEDICO SALUD OCUPACIONAL";
        agregarDiagnostico();
        agregarReceta();
    }

    public void buscarPorCedula() {
        if (cedulaBusqueda == null || cedulaBusqueda.isBlank()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Cédula requerida", "Ingrese la cédula del paciente.");
            return;
        }
        String cedula = cedulaBusqueda.trim();
        empleado = empleadoService.buscarPorCedula(cedula);
        personaAux = null;
        fichaReferencia = fichaOcupacionalService.buscarFichaActivaOUltimaPorCedula(cedula);

        if (empleado == null) {
            personaAux = personaAuxService.findByCedulaConFichaYCertificado(cedula);
            if (personaAux == null) {
                personaAux = personaAuxService.findByCedula(cedula);
            }
            if (personaAux != null && personaAux.getNoPersona() != null) {
                Long noPersonaAux = personaAux.getNoPersona();
                if (noPersonaAux <= Integer.MAX_VALUE && noPersonaAux >= Integer.MIN_VALUE) {
                    empleado = empleadoService.buscarPorId(noPersonaAux.intValue());
                }
            }
        }

        if (empleado == null && personaAux == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Sin resultados", "No existe un paciente con esa cédula.");
            return;
        }

        consulta.setEmpleado(empleado);
        fechaNacimientoPaciente = (empleado != null) ? empleado.getfNacimiento() : personaAux.getFechaNac();
        alergias = obtenerAntecedenteClinicoQuirurgico();
        if (isBlank(certDomicilio)) {
            certDomicilio = resolveDireccionPaciente();
        }
        if (isBlank(certCargoPaciente)) {
            certCargoPaciente = resolveCargoPaciente();
        }
        if (isBlank(certAreaTrabajo)) {
            certAreaTrabajo = getAreaTrabajoPaciente();
        }
        consultasAnteriores = (empleado != null)
                ? consultaMedicaService.buscarPorEmpleado(empleado.getNoPersona())
                : new ArrayList<>();
        addMessage(FacesMessage.SEVERITY_INFO, "Paciente cargado", getNombrePaciente());
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
                    "La persona auxiliar seleccionada no está vinculada a un empleado para registrar la consulta.");
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
        consultaMedicaService.guardar(consulta, "WEB");
        addMessage(FacesMessage.SEVERITY_INFO, "Consulta guardada", "Se registró la consulta médica.");
    }

    private List<ConsultaDiagnostico> limpiarDiagnosticos() {
        List<ConsultaDiagnostico> limpios = new ArrayList<>();
        Set<String> codigosUsados = new HashSet<>();
        int duplicados = 0;

        for (ConsultaDiagnostico d : diagnosticos) {
            if (d == null) {
                continue;
            }
            String codigo = normalizarCodigo(d.getCodigo());
            if (codigo == null && isBlank(d.getDescripcion())) {
                continue;
            }
            if (codigo != null && !codigosUsados.add(codigo)) {
                duplicados++;
                continue;
            }

            d.setCodigo(codigo);
            d.setConsulta(consulta);
            d.setEstado("ACTIVO");
            limpios.add(d);
        }

        if (duplicados > 0) {
            addMessage(FacesMessage.SEVERITY_WARN, "Diagnósticos duplicados omitidos",
                    "Se omitieron " + duplicados + " diagnóstico(s) repetido(s) por código CIE10.");
        }

        return limpios;
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
        SignosVitales guardados = signosVitalesService.guardar(signosModel);
        consulta.setSignos(guardados);
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
        List<RecetaItem> items = new ArrayList<>();
        for (RecetaItemForm fila : recetas) {
            if (fila == null || isBlank(fila.getMedicamento())) {
                continue;
            }
            RecetaItem item = new RecetaItem();
            item.setMedicamento(fila.getMedicamento().trim());
            item.setDosis(null);
            item.setFrecuencia(null);
            item.setVia(normalizarTexto(fila.getVia()));
            item.setDuracionDias(fila.getDuracionDias());
            item.setIndicaciones(normalizarTexto(fila.getIndicaciones()));
            item.setEstado("ACTIVO");
            items.add(item);
        }
        if (items.isEmpty()) {
            return new ArrayList<>();
        }

        RecetaMedica recetaMedica = new RecetaMedica();
        recetaMedica.setConsulta(consulta);
        recetaMedica.setFechaEmision(vigenciaReceta != null ? vigenciaReceta : consulta.getFechaConsulta());
        recetaMedica.setIndicaciones(construirIndicacionesRecetaCabecera());
        recetaMedica.setEstado("ACTIVA");

        for (RecetaItem item : items) {
            item.setReceta(recetaMedica);
        }
        recetaMedica.setItems(items);

        List<RecetaMedica> resultado = new ArrayList<>();
        resultado.add(recetaMedica);
        return resultado;
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
        Date hoy = truncateTime(new Date());
        if (!truncateTime(certFechaFin).after(hoy)) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Rango inválido", "La fecha fin debe ser mayor a la fecha actual.");
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
        String nombrePaciente = getNombrePaciente();
        String cedula = getCedulaPaciente();
        String edad = calcularEdadTexto(fechaNacimientoPaciente);
        String medicoNombre = consulta.getMedicoNombre() == null ? "" : consulta.getMedicoNombre();
        String medicoMsp = consulta.getMedicoCodigo() == null ? "" : consulta.getMedicoCodigo();
        String logoIgm = resolveLogo("LOGO_IGM_FULL_COLOR.png");
        String logoMidena = resolveLogo("LOGO_MIDENA.png");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html xmlns='http://www.w3.org/1999/xhtml'><head><meta charset='UTF-8'/>")
                .append("<style>")
                .append("body{font-family:Arial,sans-serif;font-size:12px;margin:22px;color:#000;}")
                .append(".panel{position:relative;border:1px solid #9ca3af;padding:14px 16px 28px 16px;}")
                .append(".encabezado{border-bottom:2px solid #1f2937;margin-bottom:10px;padding-bottom:7px;}")
                .append(".encabezado-table{width:100%;border-collapse:collapse;}")
                .append(".encabezado-table td{vertical-align:middle;}")
                .append(".logo-cell{width:120px;text-align:center;}")
                .append(".logo{width:85px;height:85px;display:block;margin:0 auto;object-fit:contain;}")
                .append(".encabezado-titulo{text-align:center;font-size:14px;font-weight:700;letter-spacing:.5px;margin:2px 0 0 0;}")
                .append(".encabezado-sub{font-size:11px;color:#374151;text-align:center;margin:3px 0 0 0;}")
                .append(".titulo{font-weight:bold;margin-top:15px;margin-bottom:6px;}")
                .append(".row{margin-bottom:6px;}")
                .append("table{width:100%;border-collapse:collapse;}th,td{padding:4px 2px;vertical-align:top;}")
                .append("table{table-layout:fixed;}")
                .append("th,td{word-wrap:break-word;overflow-wrap:anywhere;}")
                .append(".firmante{margin-top:42px;text-align:center;}")
                .append(".firma-linea{border-top:1px solid #000;width:260px;margin:0 auto 8px auto;}")
                .append(".small{font-size:10px;}")
                .append("</style></head><body>");

        html.append(construirColumnaReceta(fecha, nombrePaciente, cedula, edad, medicoNombre, medicoMsp, logoIgm, logoMidena));

        html.append("</body></html>");
        return html.toString();
    }

    private String construirColumnaReceta(String fecha, String nombrePaciente, String cedula, String edad,
            String medicoNombre, String medicoMsp, String logoIgm, String logoMidena) {
        String sexo = getSexoPaciente();
        StringBuilder sb = new StringBuilder();
        sb.append("<div class='panel'>")
                .append("<div class='encabezado'>")
                .append("<table class='encabezado-table'><tr>")
                .append("<td class='logo-cell'><img class='logo' alt='LOGO_IGM_FULL_COLOR' src='").append(escape(logoIgm)).append("'/></td>")
                .append("<td>")
                .append("<div class='encabezado-titulo'>INSTITUTO GEOGRÁFICO MILITAR</div>")
                .append("<div class='encabezado-sub'>DISPENSARIO MÉDICO</div>")
                .append("<div class='encabezado-sub'><b>RECETA</b></div>")
                .append("</td>")
                .append("<td class='logo-cell'><img class='logo' alt='LOGO_MIDENA' src='").append(escape(logoMidena)).append("'/></td>")
                .append("</tr></table>")
                .append("</div>")
                .append("<div class='row'>QUITO,").append(escape(fecha).toUpperCase()).append("</div>")
                .append("<div class='row'><b>Paciente:</b> ").append(escape(nombrePaciente)).append("</div>")
                .append("<div class='row'><b>Cédula:</b> ").append(escape(cedula)).append("</div>")
                .append("<div class='row'><b>Sexo:</b> ").append(escape(sexo)).append("</div>")
                .append("<div class='row'><b>Edad:</b> ").append(escape(edad)).append("</div>");

        sb.append("<div class='titulo'>Antecedente de Alergias:</div>")
                .append("<div class='row'>").append(escape(resolveAlergiasTexto())).append("</div>")
                .append("<table><thead><tr><th>Medicamento</th><th>Diagnóstico</th><th>Vía</th><th>Días</th><th>Indicaciones</th></tr></thead><tbody>");

        for (RecetaItemForm item : recetas) {
            if (item == null || isBlank(item.getMedicamento())) {
                continue;
            }
            sb.append("<tr><td>").append(escape(item.getMedicamento())).append("</td><td>")
                    .append(escape(item.getDiagnostico())).append("</td><td>")
                    .append(escape(item.getVia())).append("</td><td>")
                    .append(item.getDuracionDias() == null ? "" : item.getDuracionDias()).append("</td><td>")
                    .append(escape(item.getIndicaciones())).append("</td></tr>");
        }

        sb.append("</tbody></table>")
                .append("<div class='titulo'>DIAGNÓSTICO</div>");

        int count = 1;
        for (ConsultaDiagnostico d : diagnosticos) {
            if (d == null || (isBlank(d.getCodigo()) && isBlank(d.getDescripcion()))) {
                continue;
            }
            sb.append("<div class='row'>").append(count++)
                    .append(". ").append(escape(d.getCodigo()))
                    .append(" ").append(escape(d.getDescripcion()))
                    .append("</div>");
        }

        sb.append("<div class='titulo'>Recomendaciones no farmacológicas:</div>")
                .append("<div class='row'>").append(escape(recomendaciones)).append("</div>")
                .append("<div class='titulo'>Signos de alarma:</div>")
                .append("<div class='row'>").append(escape(signosAlarma)).append("</div>")
                .append("<div class='firmante'>")
                .append("<div><b>Médico:</b> ").append(escape(medicoNombre)).append("</div>")
                .append("<div><b>MSP:</b> ").append(escape(medicoMsp)).append("</div>")
                .append("</div></div>");

        return sb.toString();
    }

    private String construirHtmlCertificado() {
        String nombrePaciente = getNombrePaciente();
        String cedula = getCedulaPaciente();
        String numeroHistoria = getCedulaPaciente();
        String cargoPaciente = isBlank(certCargoPaciente) ? resolveCargoPaciente() : certCargoPaciente;
        String areaTrabajo = getAreaTrabajoPaciente();
        String diagnosticoPaciente = isBlank(getDiagnosticosTexto()) ? "NO REGISTRA" : getDiagnosticosTexto();
        String sintomasPaciente = isBlank(consulta.getMotivoConsulta()) ? "NO REGISTRA" : consulta.getMotivoConsulta();
        String fechaInicioTexto = fechaEnLetras(certFechaInicio);
        String fechaFinTexto = fechaEnLetras(certFechaFin);
        long diasReposo = getCertDiasReposo();
        String telefonoContacto = valueOrNoRegistra(certTelefono);
        String medicoMsp = consulta.getMedicoCodigo() == null ? "" : consulta.getMedicoCodigo();
        String medicoTelefono = valueOrNoRegistra(certMedicoTelefono);
        String medicoCorreo = isBlank(certMedicoCorreo) ? "NO REGISTRA" : certMedicoCorreo;
        String tipoContingencia = valueOrNoRegistra(certTipoContingencia);
        String domicilioPaciente = valueOrNoRegistra(certDomicilio);
        String fechaEmision = formatoFechaNumericoYLetras(
                consulta.getFechaConsulta() != null ? consulta.getFechaConsulta() : new Date());
        String logoIgm = resolveLogo("LOGO_IGM_FULL_COLOR.png");
        String logoMidena = resolveLogo("LOGO_MIDENA.png");
        String membreteBottom = resolveLogo("membrete-bottom.png");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/>")
                .append("<style>")
                .append("@page{size:A4;margin:10mm 12mm 4mm 12mm;}")
                .append("body{font-family:Arial,sans-serif;font-size:12px;line-height:1.2;margin:0;color:#000000;padding-bottom:52px;}")
                .append(".encabezado{margin-bottom:6px;}")
                .append(".encabezado-table{width:100%;border-collapse:collapse;}")
                .append(".encabezado-table td{vertical-align:middle;}")
                .append(".logo-cell{width:180px;text-align:center;}")
                .append(".logo{width:150px;height:68px;display:block;object-fit:contain;margin:0 auto;}")
                .append(".titulo{text-align:center;font-size:20px;font-weight:700;letter-spacing:.2px;margin:4px 0 8px 0;}")
                .append(".texto{font-size:12px;margin:0 0 6px 0;text-align:justify;}")
                .append(".firma{text-align:center;font-size:12px;margin:0 0 6px 0;}")
                .append(".firma-espacio{height:42px;}")
                .append(".firma-bloque{text-align:center;}")
                .append(".correo{color:#0000EE;text-decoration:underline;}")
                .append(".pie{position:fixed;left:12mm;right:12mm;bottom:0mm;text-align:left;}")
                .append(".membrete-bottom{margin-top:4px;text-align:left;}")
                .append(".membrete-bottom img{width:100%;max-height:88px;object-fit:contain;}")
                .append("</style></head><body>")
                .append("<div class='contenido'>")
                .append("<div class='encabezado'>")
                .append("<table class='encabezado-table'><tr>")
                .append("<td class='logo-cell'><img class='logo' alt='LOGO_MIDENA' src='").append(escape(logoMidena)).append("'/></td>")
                .append("<td></td>")
                .append("<td class='logo-cell'><img class='logo' alt='LOGO_IGM_FULL_COLOR' src='").append(escape(logoIgm)).append("'/></td>")
                .append("</tr></table>")
                .append("</div>")
                .append("<div class='titulo'>CERTIFICADO MEDICO</div>")
                .append("<p class='texto'>EL MEDICO CERTIFICA:</p>")
                .append("<p class='texto'>El señor ").append(escape(nombrePaciente)).append(" con Cédula de Ciudadanía ")
                .append(escape(cedula)).append(", presenta diagnóstico: ")
                .append(escape(diagnosticoPaciente)).append("; por lo que requiere reposo médico absoluto durante ")
                .append(diasReposo).append(" (").append(escape(numeroEnLetras((int) diasReposo))).append(") día(s).</p>")
                .append("<p class='texto'><b>Historia clínica:</b> ").append(escape(numeroHistoria)).append("</p>")
                .append("<p class='texto'><b>Desde:</b> ").append(escape(fechaInicioTexto)).append(".<br/>")
                .append("<b>Hasta:</b> ").append(escape(fechaFinTexto)).append(".</p>")
                .append("<p class='texto'>Labora en el <b>INSTITUTO GEOGRAFICO MILITAR (ubicado en Seniergues E4-676 y Gral. Telmo Paz y Miño Sector El Dorado)</b>, ")
                .append("con el cargo de ").append(escape(cargoPaciente))
                .append(", en el área de ").append(escape(areaTrabajo)).append(".</p>")
                .append("<p class='texto'>Paciente sintomático quien presenta: ").append(escape(sintomasPaciente)).append(".</p>")
                .append("<p class='texto'><b>Domicilio del paciente:</b> ").append(escape(domicilioPaciente)).append("<br/>")
                .append("<b>Teléfono de contacto:</b> ").append(escape(telefonoContacto)).append("<br/>")
                .append("<b>Tipo de contingencia:</b> <b>").append(escape(tipoContingencia)).append("</b></p>")
                .append("</div>")
                .append("<div class='pie'>")
                .append("<p class='texto'>")
                .append("Quito DM, ").append(escape(fechaEmision)).append(".</p>")
                .append("<p class='firma'>Atentamente,</p>")
                .append("<div class='firma-espacio'></div>")
                .append("<p class='firma firma-bloque'>").append(escape(consulta.getMedicoNombre())).append("<br/>")
                .append(escape(certMedicoCargo)).append("<br/>")
                .append("MSP: ").append(escape(medicoMsp)).append("<br/>")
                .append("Teléfono: ").append(escape(medicoTelefono)).append("<br/>")
                .append("<span class='correo'>").append(escape(medicoCorreo)).append("</span></p>")
                .append("<div class='membrete-bottom'>")
                .append("<img alt='membrete-bottom' src='").append(escape(membreteBottom)).append("'/>")
                .append("</div>")
                .append("</div>")
                .append("</body></html>");
        return html.toString();
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
        LocalDate local = Instant.ofEpochMilli(date.getTime()).atZone(CERTIFICADO_ZONE).toLocalDate();
        String mes = local.getMonth().getDisplayName(TextStyle.FULL, new java.util.Locale("es", "EC"));
        return local.getDayOfMonth() + " de " + mes + " de " + local.getYear();
    }

    private String fechaEnLetrasCompleta(Date date) {
        if (date == null) {
            return "";
        }
        LocalDate local = Instant.ofEpochMilli(date.getTime()).atZone(CERTIFICADO_ZONE).toLocalDate();
        String mes = local.getMonth().getDisplayName(TextStyle.FULL, new java.util.Locale("es", "EC"));
        return numeroEnLetras(local.getDayOfMonth()) + " de " + mes + " de " + anioEnLetras(local.getYear());
    }

    private String formatoFechaNumericoYLetras(Date date) {
        if (date == null) {
            return "";
        }
        LocalDate local = Instant.ofEpochMilli(date.getTime()).atZone(CERTIFICADO_ZONE).toLocalDate();
        String mes = local.getMonth().getDisplayName(TextStyle.FULL, new java.util.Locale("es", "EC"));
        return local.getDayOfMonth() + " (" + numeroEnLetras(local.getDayOfMonth()) + ") de " + mes
                + " de " + local.getYear() + " (" + anioEnLetras(local.getYear()) + ")";
    }

    private void sincronizarFechasCertificado() {
        Date hoy = truncateTime(new Date());
        Date minimoFin = sumarDias(hoy, 1);
        if (certFechaInicio != null && certFechaFin != null && certFechaFin.before(certFechaInicio)) {
            certFechaFin = certFechaInicio;
            return;
        }
        if (certFechaFin == null) {
            certFechaFin = certFechaInicio != null ? certFechaInicio : minimoFin;
            return;
        }
        if (!truncateTime(certFechaFin).after(hoy)) {
            certFechaFin = minimoFin;
            if (certFechaInicio != null && certFechaFin.before(certFechaInicio)) {
                certFechaFin = certFechaInicio;
            }
        }
    }

    public void onFechaInicioSelect() {
        Date hoy = truncateTime(new Date());
        Date minimoFin = sumarDias(hoy, 1);
        if (certFechaInicio != null && certFechaFin != null && certFechaFin.before(certFechaInicio)) {
            certFechaFin = certFechaInicio;
        }
        if (certFechaFin == null) {
            certFechaFin = certFechaInicio != null ? certFechaInicio : minimoFin;
        }
        if (certFechaFin != null && !truncateTime(certFechaFin).after(hoy)) {
            certFechaFin = minimoFin;
            if (certFechaInicio != null && certFechaFin.before(certFechaInicio)) {
                certFechaFin = certFechaInicio;
            }
            addMessage(FacesMessage.SEVERITY_WARN, "Fecha fin ajustada",
                    "La fecha fin debe ser mayor a la fecha actual.");
        }
    }

    public void onFechaFinSelect() {
        Date hoy = truncateTime(new Date());
        if (certFechaFin != null && !truncateTime(certFechaFin).after(hoy)) {
            certFechaFin = sumarDias(hoy, 1);
            addMessage(FacesMessage.SEVERITY_WARN, "Fecha fin ajustada",
                    "La fecha fin debe ser mayor a la fecha actual.");
        }
        if (certFechaInicio != null && certFechaFin != null && certFechaFin.before(certFechaInicio)) {
            certFechaFin = certFechaInicio;
            addMessage(FacesMessage.SEVERITY_WARN, "Fecha fin ajustada",
                    "La fecha fin no puede ser menor a la fecha inicio.");
        }
        sincronizarFechasCertificado();
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
        LocalDate local = Instant.ofEpochMilli(base.getTime()).atZone(CERTIFICADO_ZONE).toLocalDate();
        LocalDate ajustada = local.plusDays(dias);
        return Date.from(ajustada.atStartOfDay(CERTIFICADO_ZONE).toInstant());
    }

    private Date truncateTime(Date fecha) {
        if (fecha == null) {
            return null;
        }
        LocalDate local = Instant.ofEpochMilli(fecha.getTime()).atZone(CERTIFICADO_ZONE).toLocalDate();
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
        LocalDate inicio = Instant.ofEpochMilli(certFechaInicio.getTime()).atZone(CERTIFICADO_ZONE).toLocalDate();
        LocalDate fin = Instant.ofEpochMilli(certFechaFin.getTime()).atZone(CERTIFICADO_ZONE).toLocalDate();
        long dias = ChronoUnit.DAYS.between(inicio, fin);
        return Math.max(dias, 0L) + 1L;
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
        var datosLaboralesRh = resolveDatosLaboralesRh();
        if (datosLaboralesRh != null && !isBlank(datosLaboralesRh.getAreaDescrip())) {
            return datosLaboralesRh.getAreaDescrip();
        }
        if (fichaReferencia != null && !isBlank(fichaReferencia.getEstablecimientoCt())) {
            return fichaReferencia.getEstablecimientoCt();
        }
        return "";
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
            }
        }
        if (fichaReferencia != null && fichaReferencia.getEmpleado() != null) {
            empleado = fichaReferencia.getEmpleado();
            return empleado;
        }
        if (personaAux != null) {
            DatEmpleado externo = crearEmpleadoExternoDesdePersonaAux(personaAux);
            if (externo != null) {
                empleado = externo;
                return externo;
            }
        }
        return null;
    }

    private DatEmpleado crearEmpleadoExternoDesdePersonaAux(PersonaAux aux) {
        if (aux == null || isBlank(aux.getCedula())) {
            return null;
        }

        DatEmpleado existentePorCedula = empleadoService.buscarPorCedula(aux.getCedula().trim());
        if (existentePorCedula != null) {
            vincularPersonaAuxConEmpleado(aux, existentePorCedula);
            return existentePorCedula;
        }

        DatEmpleado externo = new DatEmpleado();
        externo.setNoPersona(empleadoService.obtenerSiguienteNoPersona());
        externo.setNoCedula(aux.getCedula().trim());
        externo.setPriApellido(firstNonBlank(aux.getApellido1(), aux.getApellidos()));
        externo.setSegApellido(aux.getApellido2());
        externo.setNombres(buildNombres(aux));
        externo.setNombreC(buildNombreCompleto(aux));
        externo.setfNacimiento(aux.getFechaNac());
        externo.setSexo(mapSexo(aux.getSexo()));
        externo.setTipo("E");
        externo.setDireccion(certDomicilio);
        externo.setTelefono(certTelefono);
        DatEmpleado guardado = empleadoService.guardar(externo);
        vincularPersonaAuxConEmpleado(aux, guardado);
        return guardado;
    }

    private void vincularPersonaAuxConEmpleado(PersonaAux aux, DatEmpleado emp) {
        if (aux == null || emp == null || emp.getNoPersona() == null) {
            return;
        }
        Long noPersonaActual = aux.getNoPersona();
        Long noPersonaEmp = emp.getNoPersona().longValue();
        if (noPersonaActual != null && noPersonaActual.equals(noPersonaEmp)) {
            return;
        }
        aux.setNoPersona(noPersonaEmp);
        personaAuxService.guardar(aux);
    }

    private Sexo mapSexo(String sexoAux) {
        if (isBlank(sexoAux)) {
            return null;
        }
        return "M".equalsIgnoreCase(sexoAux) ? Sexo.MASCULINO : Sexo.FEMENINO;
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
        if (numero < 30) {
            String[] base = {"", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve",
                    "diez", "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho",
                    "diecinueve", "veinte", "veintiuno", "veintidós", "veintitrés", "veinticuatro", "veinticinco",
                    "veintiséis", "veintisiete", "veintiocho", "veintinueve"};
            return base[numero];
        }
        if (numero < 100) {
            String[] decenas = {"", "", "veinte", "treinta", "cuarenta", "cincuenta", "sesenta", "setenta", "ochenta", "noventa"};
            int d = numero / 10;
            int u = numero % 10;
            if (u == 0) {
                return decenas[d];
            }
            return decenas[d] + " y " + numeroEnLetras(u);
        }
        if (numero == 100) {
            return "cien";
        }
        if (numero < 1000) {
            String[] centenas = {"", "ciento", "doscientos", "trescientos", "cuatrocientos",
                    "quinientos", "seiscientos", "setecientos", "ochocientos", "novecientos"};
            int c = numero / 100;
            int resto = numero % 100;
            if (resto == 0) {
                return centenas[c];
            }
            return centenas[c] + " " + numeroEnLetras(resto);
        }
        return Integer.toString(numero);
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

    public static class RecetaItemForm implements Serializable {
        private String medicamento;
        private String diagnostico;
        private String via;
        private Integer duracionDias;
        private String indicaciones;

        public String getMedicamento() { return medicamento; }
        public void setMedicamento(String medicamento) { this.medicamento = medicamento; }
        public String getDiagnostico() { return diagnostico; }
        public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
        public String getVia() { return via; }
        public void setVia(String via) { this.via = via; }
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
