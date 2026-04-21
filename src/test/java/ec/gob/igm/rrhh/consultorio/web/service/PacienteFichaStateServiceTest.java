package ec.gob.igm.rrhh.consultorio.web.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import ec.gob.igm.rrhh.consultorio.service.EmpleadoService;
import ec.gob.igm.rrhh.consultorio.service.FichaOcupacionalService;
import ec.gob.igm.rrhh.consultorio.service.PersonaAuxService;

class PacienteFichaStateServiceTest {

    private final EmpleadoService empleadoService = mock(EmpleadoService.class);
    private final FichaOcupacionalService fichaOcupacionalService = mock(FichaOcupacionalService.class);
    private final PersonaAuxService personaAuxService = mock(PersonaAuxService.class);

    private PacienteFichaStateService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new PacienteFichaStateService();
        inject("empleadoService", empleadoService);
        inject("fichaService", fichaOcupacionalService);
        inject("personaAuxService", personaAuxService);
    }

    @Test
    void syncPatientStateAfterStep1DebeRehidratarPersonaAuxProxyAntesDeExponerloALaVista() {
        FichaOcupacional ficha = new FichaOcupacional();
        PersonaAux proxy = Mockito.mock(
                PersonaAux.class,
                Mockito.withSettings().extraInterfaces(HibernateProxy.class));
        when(proxy.getIdPersonaAux()).thenReturn(304L);
        ficha.setPersonaAux(proxy);

        PersonaAux personaAuxCompleta = new PersonaAux();
        personaAuxCompleta.setIdPersonaAux(304L);
        personaAuxCompleta.setApellido1("Perez");
        personaAuxCompleta.setNombre1("Ana");
        when(personaAuxService.find(304L)).thenReturn(personaAuxCompleta);

        PacienteFichaStateService.PatientState state = service.syncPatientStateAfterStep1(
                true,
                null,
                null,
                null,
                ficha);

        assertSame(personaAuxCompleta, state.getPersonaAux());
        assertSame(personaAuxCompleta, ficha.getPersonaAux());
        verify(personaAuxService).find(304L);
    }

    @Test
    void syncPatientStateAfterStep1NoDebeRecargarPersonaAuxYaMaterializada() {
        FichaOcupacional ficha = new FichaOcupacional();
        PersonaAux personaAux = new PersonaAux();
        personaAux.setIdPersonaAux(305L);
        personaAux.setApellido1("Vera");
        ficha.setPersonaAux(personaAux);

        PacienteFichaStateService.PatientState state = service.syncPatientStateAfterStep1(
                true,
                null,
                null,
                null,
                ficha);

        assertSame(personaAux, state.getPersonaAux());
        assertSame(personaAux, ficha.getPersonaAux());
        verifyNoInteractions(personaAuxService);
    }

    private void inject(String fieldName, Object value) throws Exception {
        Field field = PacienteFichaStateService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(service, value);
    }
}
