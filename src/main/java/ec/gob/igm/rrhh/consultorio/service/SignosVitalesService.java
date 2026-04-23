package ec.gob.igm.rrhh.consultorio.service;


import ec.gob.igm.rrhh.consultorio.domain.model.SignosVitales;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;

@Stateless
/**
 * Class SignosVitalesService: encapsula reglas de negocio y acceso a datos del dominio.
 */
public class SignosVitalesService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;
    @EJB
    private ClientIdentifierService clientIdentifierService;

    private void assertEm() {
        if (em == null) {
            throw new IllegalStateException(
                "EntityManager no inyectado. Revisa persistence.xml y unitName='consultorioPU'."
            );
        }
    }

    public SignosVitales guardar(SignosVitales signos) {
        return guardar(signos, null);
    }

    public SignosVitales guardar(SignosVitales signos, String usuario) {
        assertEm();

        if (signos == null) {
            return null;
        }
        clientIdentifierService.apply(usuario);

        if (signos.getIdSignos() == null) {
            em.persist(signos);
            // em.flush(); // útil en debug para errores de BD
            return signos;
        }

        SignosVitales merged = em.merge(signos);
        // em.flush(); // útil en debug
        return merged;
    }

    public SignosVitales buscarPorId(Long id) {
        assertEm();

        if (id == null) {
            return null;
        }
        return em.find(SignosVitales.class, id);
    }
}
