package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.dto.HistorialFichaCertificadoDTO;
import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless

/**
 * Class FichaOcupacionalService: encapsula reglas de negocio y acceso a datos del dominio.
 */
public class FichaOcupacionalService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    public FichaOcupacional guardar(FichaOcupacional f) {
        normalizarNoNulos(f);
        if (f.getIdFicha() == null) {
            em.persist(f);
            return f;
        } else {
            return em.merge(f);
        }
    }

    public FichaOcupacional actualizar(FichaOcupacional f) {
        normalizarNoNulos(f);
        return em.merge(f);
    }

    private void normalizarNoNulos(FichaOcupacional f) {
        if (f == null) {
            return;
        }

        normalizarPacienteExclusivo(f);
        normalizarPielYOjos(f);
        normalizarOidoYOrofaringe(f);
        normalizarNarizYCuello(f);
        normalizarTorsoYPelvis(f);
        normalizarExtremidadesYNeuro(f);
        normalizarPlanificacion(f);
    }

    private void normalizarPacienteExclusivo(FichaOcupacional f) {
        if (hasExclusivePatient(f)) {
            return;
        }
        if (restorePersistedPatientIfNeeded(f)) {
            return;
        }
        validatePatientPresence(f);
        resolvePatientByHistoriaClinica(f);
    }

    private String snNoNull(String v) {
        if (v == null) {
            return "N";
        }
        String t = v.trim();
        return t.isEmpty() ? "N" : t;
    }

    private String safeTrim(String v) {
        return v == null ? null : v.trim();
    }

    public FichaOcupacional findById(Long idFicha) {
        if (idFicha == null) {
            return null;
        }
        return em.find(FichaOcupacional.class, idFicha);
    }

    public FichaOcupacional reloadById(Long idFicha) {
        if (idFicha == null) {
            return null;
        }

        FichaOcupacional f = em.find(FichaOcupacional.class, idFicha);
        if (f != null) {
            em.refresh(f);
        }
        return f;
    }

    public FichaOcupacional reloadForPrint(Long idFicha) {
        if (idFicha == null) {
            return null;
        }

        final String jpql = """
            SELECT f
            FROM FichaOcupacional f
            LEFT JOIN FETCH f.empleado
            LEFT JOIN FETCH f.personaAux
            LEFT JOIN FETCH f.signos
            LEFT JOIN FETCH f.cie10Principal
            WHERE f.idFicha = :id
        """;

        TypedQuery<FichaOcupacional> q = em.createQuery(jpql, FichaOcupacional.class);
        q.setParameter("id", idFicha);
        return q.getResultStream().findFirst().orElse(null);
    }


    public List<HistorialFichaCertificadoDTO> listarHistorialPorCedula(String cedula) {
        if (cedula == null || cedula.trim().isEmpty()) {
            return List.of();
        }

        String cedulaNorm = cedula.trim();
        String jpql = """
            SELECT new ec.gob.igm.rrhh.consultorio.domain.dto.HistorialFichaCertificadoDTO(
                f.idFicha,
                COALESCE(e.noCedula, p.cedula, f.noHistoriaClinica, f.noArchivo),
                CASE
                    WHEN e.noPersona IS NOT NULL THEN CONCAT(CONCAT(COALESCE(e.priApellido, ''), ' '), CONCAT(COALESCE(e.segApellido, ''), CONCAT(' ', COALESCE(e.nombres, ''))))
                    WHEN p.idPersonaAux IS NOT NULL THEN CONCAT(COALESCE(p.apellidos, ''), CONCAT(' ', COALESCE(p.nombres, '')))
                    ELSE 'SIN NOMBRE REGISTRADO'
                END,
                f.fechaEvaluacion,
                f.fechaEmision,
                f.estado,
                COALESCE(f.usrActualizacion, f.usrCreacion)
            )
            FROM FichaOcupacional f
            LEFT JOIN f.empleado e
            LEFT JOIN f.personaAux p
            WHERE e.noCedula = :cedula
               OR p.cedula = :cedula
               OR f.noHistoriaClinica = :cedula
               OR f.noArchivo = :cedula
            ORDER BY COALESCE(f.fechaActualizacion, f.fechaCreacion, f.fechaEvaluacion) DESC, f.idFicha DESC
        """;

        return em.createQuery(jpql, HistorialFichaCertificadoDTO.class)
                .setParameter("cedula", cedulaNorm)
                .getResultList();
    }

    private void normalizarPielYOjos(FichaOcupacional f) {
        f.setExfPielCicatrices(snNoNull(f.getExfPielCicatrices()));
        f.setExfOjosParpados(snNoNull(f.getExfOjosParpados()));
        f.setExfOjosConjuntivas(snNoNull(f.getExfOjosConjuntivas()));
        f.setExfOjosPupilas(snNoNull(f.getExfOjosPupilas()));
        f.setExfOjosCornea(snNoNull(f.getExfOjosCornea()));
        f.setExfOjosMotilidad(snNoNull(f.getExfOjosMotilidad()));
    }

    private void normalizarOidoYOrofaringe(FichaOcupacional f) {
        f.setExfOidoConducto(snNoNull(f.getExfOidoConducto()));
        f.setExfOidoPabellon(snNoNull(f.getExfOidoPabellon()));
        f.setExfOidoTimpanos(snNoNull(f.getExfOidoTimpanos()));
        f.setExfOroLabios(snNoNull(f.getExfOroLabios()));
        f.setExfOroLengua(snNoNull(f.getExfOroLengua()));
        f.setExfOroFaringe(snNoNull(f.getExfOroFaringe()));
        f.setExfOroAmigdalas(snNoNull(f.getExfOroAmigdalas()));
        f.setExfOroDentadura(snNoNull(f.getExfOroDentadura()));
    }

    private void normalizarNarizYCuello(FichaOcupacional f) {
        f.setExfNarizTabique(snNoNull(f.getExfNarizTabique()));
        f.setExfNarizCornetes(snNoNull(f.getExfNarizCornetes()));
        f.setExfNarizMucosas(snNoNull(f.getExfNarizMucosas()));
        f.setExfNarizSenosParanasa(snNoNull(f.getExfNarizSenosParanasa()));
        f.setExfCuelloTiroidesMasas(snNoNull(f.getExfCuelloTiroidesMasas()));
        f.setExfCuelloMovilidad(snNoNull(f.getExfCuelloMovilidad()));
    }

    private void normalizarTorsoYPelvis(FichaOcupacional f) {
        f.setExfToraxMamas(snNoNull(f.getExfToraxMamas()));
        f.setExfToraxPulmones(snNoNull(f.getExfToraxPulmones()));
        f.setExfToraxCorazon(snNoNull(f.getExfToraxCorazon()));
        f.setExfToraxParrillaCostal(snNoNull(f.getExfToraxParrillaCostal()));
        f.setExfAbdVisceras(snNoNull(f.getExfAbdVisceras()));
        f.setExfAbdParedAbdominal(snNoNull(f.getExfAbdParedAbdominal()));
        f.setExfColFlexibilidad(snNoNull(f.getExfColFlexibilidad()));
        f.setExfColDesviacion(snNoNull(f.getExfColDesviacion()));
        f.setExfColDolor(snNoNull(f.getExfColDolor()));
        f.setExfPelvisPelvis(snNoNull(f.getExfPelvisPelvis()));
        f.setExfPelvisGenitales(snNoNull(f.getExfPelvisGenitales()));
    }

    private void normalizarExtremidadesYNeuro(FichaOcupacional f) {
        f.setExfExtVascular(snNoNull(f.getExfExtVascular()));
        f.setExfExtMiembrosSup(snNoNull(f.getExfExtMiembrosSup()));
        f.setExfExtMiembrosInf(snNoNull(f.getExfExtMiembrosInf()));
        f.setExfNeuroFuerza(snNoNull(f.getExfNeuroFuerza()));
        f.setExfNeuroSensibilidad(snNoNull(f.getExfNeuroSensibilidad()));
        f.setExfNeuroMarcha(snNoNull(f.getExfNeuroMarcha()));
        f.setExfNeuroReflejos(snNoNull(f.getExfNeuroReflejos()));
    }

    private void normalizarPlanificacion(FichaOcupacional f) {
        if (!"SI".equalsIgnoreCase(safeTrim(f.getPlanificacion()))) {
            f.setPlanificacionCual(null);
        }
    }

    private boolean hasExclusivePatient(FichaOcupacional f) {
        return (f.getEmpleado() != null) ^ (f.getPersonaAux() != null);
    }

    private boolean restorePersistedPatientIfNeeded(FichaOcupacional f) {
        if (f.getEmpleado() != null || f.getPersonaAux() != null || f.getIdFicha() == null) {
            return false;
        }

        FichaOcupacional actual = em.find(FichaOcupacional.class, f.getIdFicha());
        if (actual == null) {
            return false;
        }
        if (actual.getEmpleado() != null) {
            f.setEmpleado(actual.getEmpleado());
            return true;
        }
        if (actual.getPersonaAux() != null) {
            f.setPersonaAux(actual.getPersonaAux());
            return true;
        }
        return false;
    }

    private void validatePatientPresence(FichaOcupacional f) {
        if (f.getEmpleado() == null && f.getPersonaAux() == null) {
            throw new IllegalArgumentException(
                    "La ficha ocupacional debe tener NO_PERSONA o ID_PERSONA_AUX (exclusivo)."
            );
        }
    }

    private void resolvePatientByHistoriaClinica(FichaOcupacional f) {
        String historia = safeTrim(f.getNoHistoriaClinica());
        String cedulaEmpleado = (f.getEmpleado() != null) ? safeTrim(f.getEmpleado().getNoCedula()) : null;
        String cedulaAux = (f.getPersonaAux() != null) ? safeTrim(f.getPersonaAux().getCedula()) : null;

        if (historia != null && historia.equals(cedulaAux) && !historia.equals(cedulaEmpleado)) {
            f.setEmpleado(null);
            return;
        }
        if (historia != null && historia.equals(cedulaEmpleado) && !historia.equals(cedulaAux)) {
            f.setPersonaAux(null);
            return;
        }
        f.setEmpleado(null);
    }
}
