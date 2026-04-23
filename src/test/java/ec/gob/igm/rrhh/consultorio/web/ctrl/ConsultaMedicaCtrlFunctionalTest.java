package ec.gob.igm.rrhh.consultorio.web.ctrl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import ec.gob.igm.rrhh.consultorio.domain.enums.Sexo;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaDiagnostico;
import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaMedica;
import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.RecetaMedica;
import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import ec.gob.igm.rrhh.consultorio.service.Cie10Service;
import ec.gob.igm.rrhh.consultorio.service.ConsultaMedicaService;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoRhService;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;
import ec.gob.igm.rrhh.consultorio.service.FichaOcupacionalService;
import ec.gob.igm.rrhh.consultorio.service.PersonaAuxService;
import ec.gob.igm.rrhh.consultorio.service.SignosVitalesService;
import ec.gob.igm.rrhh.consultorio.testsupport.TestFacesContext;
import ec.gob.igm.rrhh.consultorio.web.audit.CentroMedicoAuditService;
import ec.gob.igm.rrhh.consultorio.web.facade.CentroMedicoPdfFacade;
import ec.gob.igm.rrhh.consultorio.web.pdf.PdfResourceResolver;
import ec.gob.igm.rrhh.consultorio.web.service.Cie10LookupService;
import ec.gob.igm.rrhh.consultorio.web.service.UserContextService;
import ec.gob.igm.rrhh.consultorio.service.MedicalNotificationService;
import jakarta.faces.application.FacesMessage;

class ConsultaMedicaCtrlFunctionalTest {

    private final EmpleadoService empleadoService = mock(EmpleadoService.class);
    private final PersonaAuxService personaAuxService = mock(PersonaAuxService.class);
    private final FichaOcupacionalService fichaOcupacionalService = mock(FichaOcupacionalService.class);
    private final EmpleadoRhService empleadoRhService = mock(EmpleadoRhService.class);
    private final ConsultaMedicaService consultaMedicaService = mock(ConsultaMedicaService.class);
    private final CentroMedicoPdfFacade centroMedicoPdfFacade = mock(CentroMedicoPdfFacade.class);
    private final PdfResourceResolver pdfResourceResolver = mock(PdfResourceResolver.class);
    private final Cie10LookupService cie10LookupService = mock(Cie10LookupService.class);
    private final Cie10Service cie10Service = mock(Cie10Service.class);
    private final SignosVitalesService signosVitalesService = mock(SignosVitalesService.class);
    private final MedicalNotificationService medicalNotificationService = mock(MedicalNotificationService.class);
    private final UserContextService userContextService = mock(UserContextService.class);
    private final CentroMedicoAuditService auditService = mock(CentroMedicoAuditService.class);

    private ConsultaMedicaCtrl controller;
    private TestFacesContext facesContext;

    @BeforeEach
    void setUp() throws Exception {
        facesContext = new TestFacesContext();
        controller = new ConsultaMedicaCtrl();
        inject("empleadoService", empleadoService);
        inject("personaAuxService", personaAuxService);
        inject("fichaOcupacionalService", fichaOcupacionalService);
        inject("empleadoRhService", empleadoRhService);
        inject("consultaMedicaService", consultaMedicaService);
        inject("centroMedicoPdfFacade", centroMedicoPdfFacade);
        inject("pdfResourceResolver", pdfResourceResolver);
        inject("cie10LookupService", cie10LookupService);
        inject("cie10Service", cie10Service);
        inject("signosVitalesService", signosVitalesService);
        inject("medicalNotificationService", medicalNotificationService);
        inject("userContextService", userContextService);
        inject("auditService", auditService);
        when(userContextService.resolveCurrentUser()).thenReturn("WEB");
        controller.init();
        facesContext.clearMessages();
    }

    @AfterEach
    void tearDown() {
        facesContext.release();
    }

    @Test
    void buscarPorCedulaDebeCargarPacienteYConsultasPrevias() {
        DatEmpleado empleado = crearEmpleadoBase();
        List<ConsultaMedica> consultasPrevias = List.of(new ConsultaMedica());

        when(empleadoService.buscarPorCedula("0102030405")).thenReturn(empleado);
        when(fichaOcupacionalService.buscarFichaActivaOUltimaPorCedula("0102030405")).thenReturn(null);
        when(consultaMedicaService.buscarPorEmpleado(empleado.getNoPersona())).thenReturn(consultasPrevias);

        controller.setCedulaBusqueda("0102030405");
        controller.buscarPorCedula();

        assertSame(empleado, controller.getEmpleado());
        assertSame(empleado, controller.getConsulta().getEmpleado());
        assertSame(consultasPrevias, controller.getConsultasAnteriores());
        assertEquals("Penicilina", controller.getAlergias());
        assertLastMessage(FacesMessage.SEVERITY_INFO, "Paciente cargado");
        verify(personaAuxService, never()).findByCedula(any());
    }

    @Test
    void guardarConsultaDebeFallarSiNoHayPacienteSeleccionado() {
        controller.guardarConsulta();

        assertLastMessage(FacesMessage.SEVERITY_ERROR, "Paciente requerido");
        verifyNoInteractions(consultaMedicaService);
        verifyNoInteractions(signosVitalesService);
    }

    @Test
    void guardarConsultaDebeNormalizarDiagnosticosRecetaYSignosVitales() {
        DatEmpleado empleado = cargarPacienteBase();

        controller.getDiagnosticos().clear();
        controller.getDiagnosticos().add(crearDiagnostico("j00", "Resfriado comun"));
        controller.getDiagnosticos().add(crearDiagnostico("J00", "Diagnostico duplicado"));
        controller.getDiagnosticos().add(crearDiagnostico("R05", "Tos"));

        controller.getRecetas().clear();
        ConsultaMedicaCtrl.RecetaItemForm item = new ConsultaMedicaCtrl.RecetaItemForm();
        item.setMedicamento(" Ibuprofeno ");
        item.setVia(" oral ");
        item.setDuracionDias(5);
        item.setIndicaciones(" cada 8 horas ");
        controller.getRecetas().add(item);

        controller.setRecomendaciones("Reposo e hidratacion");
        controller.setSignosAlarma("Fiebre persistente");
        controller.getSignosModel().setTemperaturaC(new BigDecimal("37.2"));
        controller.getSignosModel().setFrecuenciaCard(78);
        controller.setPaStr("120/80");

        SignosVitales signosGuardados = new SignosVitales();
        signosGuardados.setIdSignos(44L);
        when(signosVitalesService.guardar(any(SignosVitales.class), eq("WEB"))).thenReturn(signosGuardados);

        controller.guardarConsulta();

        ArgumentCaptor<SignosVitales> signosCaptor = ArgumentCaptor.forClass(SignosVitales.class);
        verify(signosVitalesService).guardar(signosCaptor.capture(), eq("WEB"));
        assertEquals(Integer.valueOf(120), signosCaptor.getValue().getPaSistolica());
        assertEquals(Integer.valueOf(80), signosCaptor.getValue().getPaDiastolica());

        ArgumentCaptor<ConsultaMedica> consultaCaptor = ArgumentCaptor.forClass(ConsultaMedica.class);
        verify(consultaMedicaService).guardar(consultaCaptor.capture(), eq("WEB"));

        ConsultaMedica consultaGuardada = consultaCaptor.getValue();
        assertSame(empleado, consultaGuardada.getEmpleado());
        assertSame(signosGuardados, consultaGuardada.getSignos());
        assertEquals(2, consultaGuardada.getDiagnosticos().size());
        assertEquals("J00", consultaGuardada.getDiagnosticos().get(0).getCodigo());
        assertEquals("P", consultaGuardada.getDiagnosticos().get(0).getTipoDiag());
        assertEquals("S", consultaGuardada.getDiagnosticos().get(1).getTipoDiag());
        assertSame(consultaGuardada, consultaGuardada.getDiagnosticos().get(0).getConsulta());
        assertSame(consultaGuardada, consultaGuardada.getDiagnosticos().get(1).getConsulta());

        assertEquals(1, consultaGuardada.getRecetas().size());
        RecetaMedica receta = consultaGuardada.getRecetas().get(0);
        assertNotNull(receta.getFechaEmision());
        assertTrue(receta.getIndicaciones().contains("Recomendaciones no farmacol"));
        assertTrue(receta.getIndicaciones().contains("Signos de alarma: Fiebre persistente"));
        assertEquals(1, receta.getItems().size());
        assertEquals("Ibuprofeno", receta.getItems().get(0).getMedicamento());
        assertEquals("oral", receta.getItems().get(0).getVia());
        assertEquals(Integer.valueOf(5), receta.getItems().get(0).getDuracionDias());
        assertEquals("cada 8 horas", receta.getItems().get(0).getIndicaciones());
        assertSame(receta, receta.getItems().get(0).getReceta());

        assertContainsMessage("Diagn");
        assertContainsMessage("Consulta guardada");
    }

    @Test
    void prepararDialogoCertificadoDebeCompletarDatosBaseDelPaciente() {
        cargarPacienteBase();
        Date fechaConsulta = dateOf(2026, 4, 20);
        controller.getConsulta().setFechaConsulta(fechaConsulta);

        controller.prepararDialogoCertificado();

        assertEquals(fechaConsulta, controller.getCertFechaInicio());
        assertEquals(fechaConsulta, controller.getCertFechaFin());
        assertEquals("MEDICO SALUD OCUPACIONAL", controller.getCertMedicoCargo());
        assertEquals("Av. Amazonas N34-451", controller.getCertDomicilio());
        assertEquals("Analista SIG", controller.getCertCargoPaciente());
    }

    private DatEmpleado cargarPacienteBase() {
        DatEmpleado empleado = crearEmpleadoBase();
        when(empleadoService.buscarPorCedula("0102030405")).thenReturn(empleado);
        when(fichaOcupacionalService.buscarFichaActivaOUltimaPorCedula("0102030405")).thenReturn(null);
        when(consultaMedicaService.buscarPorEmpleado(empleado.getNoPersona())).thenReturn(new ArrayList<>());
        controller.setCedulaBusqueda("0102030405");
        controller.buscarPorCedula();
        facesContext.clearMessages();
        return empleado;
    }

    private DatEmpleado crearEmpleadoBase() {
        DatEmpleado empleado = new DatEmpleado();
        empleado.setNoPersona(15);
        empleado.setNoCedula("0102030405");
        empleado.setNombreC("Ana Perez");
        empleado.setCargoLossca("Analista SIG");
        empleado.setDireccion("Av. Amazonas N34-451");
        empleado.setAlergia("Penicilina");
        empleado.setSexo(Sexo.FEMENINO);
        empleado.setfNacimiento(dateOf(1993, 7, 18));
        return empleado;
    }

    private ConsultaDiagnostico crearDiagnostico(String codigo, String descripcion) {
        ConsultaDiagnostico diagnostico = new ConsultaDiagnostico();
        diagnostico.setCodigo(codigo);
        diagnostico.setDescripcion(descripcion);
        return diagnostico;
    }

    private Date dateOf(int year, int month, int day) {
        return Date.from(LocalDate.of(year, month, day).atStartOfDay(ZoneOffset.UTC).toInstant());
    }

    private void assertLastMessage(FacesMessage.Severity severity, String summary) {
        List<FacesMessage> messages = facesContext.getCapturedMessages();
        assertTrue(!messages.isEmpty(), "Se esperaba al menos un mensaje JSF.");
        FacesMessage last = messages.get(messages.size() - 1);
        assertEquals(severity, last.getSeverity());
        assertEquals(summary, last.getSummary());
    }

    private void assertContainsMessage(String summaryFragment) {
        boolean found = facesContext.getCapturedMessages().stream()
                .map(FacesMessage::getSummary)
                .anyMatch(summary -> summary != null && summary.contains(summaryFragment));
        assertTrue(found, "No se encontro un mensaje con resumen que contenga: " + summaryFragment);
    }

    private void inject(String fieldName, Object value) throws Exception {
        Field field = ConsultaMedicaCtrl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(controller, value);
    }
}
