package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.FichaOcupacional;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Stateless

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

    /**
     * Oracle trata "" (cadena vacía) como NULL. Varias columnas EXF_* están
     * definidas NOT NULL en la tabla, por lo que si la UI no marca nada o llega
     * vacío, el merge/persist falla con ORA-01407.
     *
     * Por convención, guardamos "N" cuando no hay selección.
     */
    private void normalizarNoNulos(FichaOcupacional f) {
        if (f == null) {
            return;
        }

        normalizarPacienteExclusivo(f);

        f.setExfPielCicatrices(snNoNull(f.getExfPielCicatrices()));
        f.setExfOjosParpados(snNoNull(f.getExfOjosParpados()));
        f.setExfOjosConjuntivas(snNoNull(f.getExfOjosConjuntivas()));
        f.setExfOjosPupilas(snNoNull(f.getExfOjosPupilas()));
        f.setExfOjosCornea(snNoNull(f.getExfOjosCornea()));
        f.setExfOjosMotilidad(snNoNull(f.getExfOjosMotilidad()));
        f.setExfOidoConducto(snNoNull(f.getExfOidoConducto()));
        f.setExfOidoPabellon(snNoNull(f.getExfOidoPabellon()));
        f.setExfOidoTimpanos(snNoNull(f.getExfOidoTimpanos()));
        f.setExfOroLabios(snNoNull(f.getExfOroLabios()));
        f.setExfOroLengua(snNoNull(f.getExfOroLengua()));
        f.setExfOroFaringe(snNoNull(f.getExfOroFaringe()));
        f.setExfOroAmigdalas(snNoNull(f.getExfOroAmigdalas()));
        f.setExfOroDentadura(snNoNull(f.getExfOroDentadura()));
        f.setExfNarizTabique(snNoNull(f.getExfNarizTabique()));
        f.setExfNarizCornetes(snNoNull(f.getExfNarizCornetes()));
        f.setExfNarizMucosas(snNoNull(f.getExfNarizMucosas()));
        f.setExfNarizSenosParanasa(snNoNull(f.getExfNarizSenosParanasa()));
        f.setExfCuelloTiroidesMasas(snNoNull(f.getExfCuelloTiroidesMasas()));
        f.setExfCuelloMovilidad(snNoNull(f.getExfCuelloMovilidad()));
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
        f.setExfExtVascular(snNoNull(f.getExfExtVascular()));
        f.setExfExtMiembrosSup(snNoNull(f.getExfExtMiembrosSup()));
        f.setExfExtMiembrosInf(snNoNull(f.getExfExtMiembrosInf()));
        f.setExfNeuroFuerza(snNoNull(f.getExfNeuroFuerza()));
        f.setExfNeuroSensibilidad(snNoNull(f.getExfNeuroSensibilidad()));
        f.setExfNeuroMarcha(snNoNull(f.getExfNeuroMarcha()));
        f.setExfNeuroReflejos(snNoNull(f.getExfNeuroReflejos()));

        // Regla: si PLANIFICACION != "SI", no debe guardar "PLANIFICACION_CUAL"
        if (!"SI".equalsIgnoreCase(safeTrim(f.getPlanificacion()))) {
            f.setPlanificacionCual(null);
        }
    }

    /**
     * La restricción CK_FICHA_PERSONA_OR_AUX exige exclusividad entre
     * NO_PERSONA (empleado) e ID_PERSONA_AUX (persona auxiliar).
     *
     * Como la ficha puede llegar con estado mixto desde distintos pasos/UI,
     * se normaliza antes de persistir/merge para evitar violaciones al hacer flush
     * desde otros servicios de la misma transacción.
     */
    private void normalizarPacienteExclusivo(FichaOcupacional f) {
        boolean tieneEmpleado = f.getEmpleado() != null;
        boolean tieneAuxiliar = f.getPersonaAux() != null;

        // Estado ya válido frente al CHECK: exactamente uno informado.
        if (tieneEmpleado ^ tieneAuxiliar) {
            return;
        }

        // Si llegan ambos nulos en UPDATE, intentamos conservar el paciente ya persistido.
        if (!tieneEmpleado && !tieneAuxiliar && f.getIdFicha() != null) {
            FichaOcupacional actual = em.find(FichaOcupacional.class, f.getIdFicha());
            if (actual != null) {
                if (actual.getEmpleado() != null) {
                    f.setEmpleado(actual.getEmpleado());
                    return;
                }
                if (actual.getPersonaAux() != null) {
                    f.setPersonaAux(actual.getPersonaAux());
                    return;
                }
            }
        }

        // Si después de normalizar sigue sin paciente, fallamos en capa de servicio
        // con mensaje claro en lugar de ORA-02290 en flush.
        if (f.getEmpleado() == null && f.getPersonaAux() == null) {
            throw new IllegalArgumentException(
                    "La ficha ocupacional debe tener NO_PERSONA o ID_PERSONA_AUX (exclusivo)."
            );
        }

        String historia = safeTrim(f.getNoHistoriaClinica());
        String cedulaEmpleado = (f.getEmpleado() != null) ? safeTrim(f.getEmpleado().getNoCedula()) : null;
        String cedulaAux = (f.getPersonaAux() != null) ? safeTrim(f.getPersonaAux().getCedula()) : null;

        if (historia != null) {
            if (historia.equals(cedulaAux) && !historia.equals(cedulaEmpleado)) {
                f.setEmpleado(null);
                return;
            }
            if (historia.equals(cedulaEmpleado) && !historia.equals(cedulaAux)) {
                f.setPersonaAux(null);
                return;
            }
        }

        // Fallback seguro para evitar que llegue ambos no-nulos a BD.
        f.setEmpleado(null);
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
            em.refresh(f); // fuerza a traer lo último de BD
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
}
