package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaMedica;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

@Stateless
public class ConsultaMedicaService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    public ConsultaMedica guardar(ConsultaMedica consulta, String usuario) {
        if (consulta == null) {
            throw new IllegalArgumentException("La consulta médica es obligatoria.");
        }

        Date ahora = new Date();
        String usr = (usuario == null || usuario.isBlank()) ? "SISTEMA" : usuario;

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
        if (noPersona == null) {
            return List.of();
        }
        return em.createQuery("""
                SELECT DISTINCT c FROM ConsultaMedica c
                LEFT JOIN FETCH c.diagnosticos d
                LEFT JOIN FETCH d.cie10
                WHERE c.empleado.noPersona = :noPersona
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
