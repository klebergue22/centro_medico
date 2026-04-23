package ec.gob.igm.rrhh.consultorio.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class ClientIdentifierService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    public void apply(String username) {
        if (em == null) {
            return;
        }
        String resolved = (username == null || username.trim().isEmpty()) ? "USR_APP" : username.trim();
        em.createNativeQuery("BEGIN DBMS_SESSION.SET_IDENTIFIER(:usr); END;")
                .setParameter("usr", resolved)
                .executeUpdate();
    }
}
