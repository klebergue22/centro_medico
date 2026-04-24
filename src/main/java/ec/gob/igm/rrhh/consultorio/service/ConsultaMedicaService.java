package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaMedica;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

@Stateless
public class ConsultaMedicaService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;
    @EJB
    private ClientIdentifierService clientIdentifierService;

    public ConsultaMedica guardar(ConsultaMedica consulta, String usuario) {
        if (consulta == null) {
            throw new IllegalArgumentException("La consulta médica es obligatoria.");
        }

        Date ahora = new Date();
        String usr = (usuario == null || usuario.isBlank()) ? "SISTEMA" : usuario;
        clientIdentifierService.apply(usr);

        if (consulta.getIdConsulta() != null) {
            ConsultaMedica actual = em.find(ConsultaMedica.class, consulta.getIdConsulta());
            if (actual == null) {
                // Cuando llega una entidad "detached" con id inválido, Hibernate puede lanzar
                // OptimisticLockException en merge; tratamos el caso como un alta nueva.
                consulta.setIdConsulta(null);
            } else {
                consulta.setFechaCreacion(actual.getFechaCreacion());
                consulta.setUsrCreacion(actual.getUsrCreacion());
            }
        }

        if (consulta.getIdConsulta() == null) {
            consulta.setFechaCreacion(ahora);
            consulta.setUsrCreacion(usr);
            if (consulta.getEstado() == null || consulta.getEstado().isBlank()) {
                consulta.setEstado("ACTIVO");
            }
            em.persist(consulta);
            return consulta;
        }

        consulta.setFechaActualizacion(ahora);
        consulta.setUsrActualizacion(usr);
        return em.merge(consulta);
    }

    public List<ConsultaMedica> buscarPorEmpleado(Integer noPersona) {
        return buscarPorEmpleado(noPersona, false, false);
    }

    public List<ConsultaMedica> buscarPorEmpleado(Integer noPersona, boolean soloOdontologicas) {
        return buscarPorEmpleado(noPersona, soloOdontologicas, false);
    }

    public List<ConsultaMedica> buscarPorEmpleadoSoloMedicas(Integer noPersona) {
        return buscarPorEmpleado(noPersona, false, true);
    }

    public List<ConsultaMedica> buscarPorEmpleado(Integer noPersona, boolean soloOdontologicas, boolean soloMedicas) {
        if (noPersona == null) {
            return List.of();
        }

        if (soloOdontologicas && soloMedicas) {
            throw new IllegalArgumentException("No se puede filtrar simultáneamente consultas odontológicas y médicas.");
        }

        String filtroRolOdontologo = """
                UPPER(c.usrCreacion) IN (
                    SELECT UPPER(u.username)
                    FROM UsuarioAuth u, SegUsuarioRol ur, SegRol r
                    WHERE ur.idUsuario = u.idUsuario
                      AND r.idRol = ur.idRol
                      AND ur.activo = 'S'
                      AND r.activo = 'S'
                      AND UPPER(r.codigo) = 'ODONTOLOGO'
                )
                """;

        String filtroOdontologico = soloOdontologicas
                ? """
                  AND (
                        c.signos IS NULL
                        OR """ + filtroRolOdontologo + """
                  )
                  """
                : "";
        String filtroMedico = soloMedicas
                ? """
                  AND (
                        c.signos IS NOT NULL
                        AND COALESCE(UPPER(c.usrCreacion), '') NOT IN (
                            SELECT UPPER(u.username)
                            FROM UsuarioAuth u, SegUsuarioRol ur, SegRol r
                            WHERE ur.idUsuario = u.idUsuario
                              AND r.idRol = ur.idRol
                              AND ur.activo = 'S'
                              AND r.activo = 'S'
                              AND UPPER(r.codigo) = 'ODONTOLOGO'
                        )
                  )
                  """
                : "";

        List<ConsultaMedica> consultas = em.createQuery("""
                SELECT DISTINCT c FROM ConsultaMedica c
                LEFT JOIN FETCH c.diagnosticos
                LEFT JOIN FETCH c.diagnosticos.cie10
                WHERE c.empleado.noPersona = :noPersona
                """ + filtroOdontologico + filtroMedico + """
                ORDER BY c.fechaConsulta DESC
                """, ConsultaMedica.class)
                .setParameter("noPersona", noPersona)
                .setMaxResults(10)
                .getResultList();

        consultas.forEach(c -> {
            if (c.getDiagnosticos() != null) {
                c.getDiagnosticos().size();
            }
            if (c.getRecetas() != null) {
                c.getRecetas().forEach(r -> {
                    if (r.getItems() != null) {
                        r.getItems().size();
                    }
                });
            }
        });
        return consultas;
    }
}
