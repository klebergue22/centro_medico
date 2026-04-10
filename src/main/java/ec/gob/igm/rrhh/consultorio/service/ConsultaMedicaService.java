package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.ConsultaMedica;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;

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
}
