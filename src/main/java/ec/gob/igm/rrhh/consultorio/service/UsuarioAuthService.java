package ec.gob.igm.rrhh.consultorio.service;

import ec.gob.igm.rrhh.consultorio.domain.model.DatEmpleado;
import ec.gob.igm.rrhh.consultorio.domain.model.UsuarioAuth;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

@Stateless
public class UsuarioAuthService {

    @PersistenceContext(unitName = "consultorioPU")
    private EntityManager em;

    public UsuarioAuth findOrCreateByEmpleado(DatEmpleado empleado) {
        String username = empleado.getNoCedula();
        UsuarioAuth usuario = findByUsername(username);
        if (usuario != null) {
            return usuario;
        }

        UsuarioAuth nuevo = new UsuarioAuth();
        nuevo.setUsername(username);
        nuevo.setNoCedula(empleado.getNoCedula());
        nuevo.setNoPersona(empleado.getNoPersona());
        nuevo.setNombreVisible(empleado.getNombreC() != null ? empleado.getNombreC() : empleado.getNoCedula());
        nuevo.setEmail(empleado.getEmailInstitucional());
        nuevo.setClaveHash(hash(empleado.getNoCedula()));
        nuevo.setAlgoritmoHash("SHA-256");
        nuevo.setActivo("S");
        nuevo.setBloqueado("N");
        nuevo.setIntentosFallidos(0);
        nuevo.setRequiereCambioClave("S");
        nuevo.setFechaCreacion(new Date());
        nuevo.setUsrCreacion("AUTH_AUTO");
        em.persist(nuevo);
        return nuevo;
    }

    public UsuarioAuth findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return findByUsernameInternal(username.trim());
    }

    public boolean validatePassword(UsuarioAuth usuario, String rawPassword) {
        if (usuario == null || rawPassword == null || usuario.getClaveHash() == null) {
            return false;
        }
        return usuario.getClaveHash().equals(hash(rawPassword));
    }

    public boolean requiereCambioClave(UsuarioAuth usuario) {
        return usuario != null && "S".equalsIgnoreCase(usuario.getRequiereCambioClave());
    }

    public void actualizarClave(UsuarioAuth usuario, String nuevaClave) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no puede ser null");
        }
        usuario.setClaveHash(hash(nuevaClave));
        usuario.setRequiereCambioClave("N");
        usuario.setFechaUltimoCambioClave(new Date());
        usuario.setIntentosFallidos(0);
        em.merge(usuario);
    }

    public void actualizarClaveTemporal(UsuarioAuth usuario, String claveTemporal) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no puede ser null");
        }
        if (claveTemporal == null || claveTemporal.isBlank()) {
            throw new IllegalArgumentException("La clave temporal no puede estar vacía");
        }
        usuario.setClaveHash(hash(claveTemporal));
        usuario.setRequiereCambioClave("S");
        usuario.setFechaUltimoCambioClave(new Date());
        usuario.setIntentosFallidos(0);
        em.merge(usuario);
    }

    public void registrarLoginExitoso(UsuarioAuth usuario) {
        if (usuario == null) return;
        usuario.setFechaUltimoLogin(new Date());
        usuario.setIntentosFallidos(0);
        em.merge(usuario);
    }

    private UsuarioAuth findByUsernameInternal(String username) {
        List<UsuarioAuth> rows = em.createQuery(
                        "SELECT u FROM UsuarioAuth u WHERE u.username = :username", UsuarioAuth.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultList();
        return rows.isEmpty() ? null : rows.get(0);
    }

    private String hash(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo calcular hash", e);
        }
    }
}
