package ec.gob.igm.rrhh.consultorio.service;


import ec.gob.igm.rrhh.consultorio.domain.model.PersonaAux;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Date;
import java.util.List;

@Stateless
public class PersonaAuxService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    private void assertEm() {
        if (em == null) {
            throw new IllegalStateException(
                "EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'."
            );
        }
    }

    // BÁSICOS

    public PersonaAux guardar(PersonaAux p) {
        assertEm();

        if (p == null) {
            return null;
        }

        Date ahora = new Date();

        if (p.getIdPersonaAux() == null) {
            // INSERT
            if (p.getEstado() == null || p.getEstado().trim().isEmpty()) {
                p.setEstado("PENDIENTE");
            }
            if (p.getFechaCreacion() == null) {
                p.setFechaCreacion(ahora);
            }
            if (p.getUsrCreacion() == null || p.getUsrCreacion().trim().isEmpty()) {
                p.setUsrCreacion("USR_APP"); // luego reemplazas por usuario real
            }

            em.persist(p);
            // em.flush(); // útil en debug
            return p;

        } else {
            // UPDATE
            p.setFechaActualizacion(ahora);
            if (p.getUsrActualizacion() == null || p.getUsrActualizacion().trim().isEmpty()) {
                p.setUsrActualizacion("USR_APP"); // luego reemplazas por usuario real
            }

            PersonaAux merged = em.merge(p);
            // em.flush(); // útil en debug
            return merged;
        }
    }

    public PersonaAux find(Long id) {
        assertEm();
        if (id == null) return null;
        return em.find(PersonaAux.class, id);
    }

    // CONSULTAS

    public List<PersonaAux> listarTodos() {
        assertEm();
        return em.createNamedQuery("PersonaAux.findAll", PersonaAux.class)
                 .getResultList();
    }

    public PersonaAux findByCedula(String cedula) {
        assertEm();

        if (cedula == null) return null;

        String c = cedula.trim();
        if (c.isEmpty()) return null;

        List<PersonaAux> lst = em.createNamedQuery("PersonaAux.findByCedula", PersonaAux.class)
                                 .setParameter("cedula", c)
                                 .setMaxResults(1)
                                 .getResultList();

        return lst.isEmpty() ? null : lst.get(0);
    }

    public List<PersonaAux> listarPendientes() {
        assertEm();
        return em.createNamedQuery("PersonaAux.findPendientes", PersonaAux.class)
                 .getResultList();
    }

    public List<PersonaAux> listarVinculados() {
        assertEm();
        return em.createNamedQuery("PersonaAux.findVinculados", PersonaAux.class)
                 .getResultList();
    }

    // UTILITARIO

    public PersonaAux crearDesdePantalla(String cedula,
                                         String nombres,
                                         String apellidos,
                                         String sexo,
                                         Date fechaNac) {
        PersonaAux p = new PersonaAux();
        p.setCedula(cedula != null ? cedula.trim() : null);
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setSexo(sexo);
        p.setFechaNac(fechaNac);

        // estado/fechas/usr se setean en guardar(...)
        return guardar(p);
    }
}
