package ec.gob.igm.rrhh.consultorio.web.security;

import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class CredentialStore {

    private final Map<String, String> passwordsByCedula = new ConcurrentHashMap<>();

    @Lock(LockType.READ)
    public boolean isFirstLogin(String cedula) {
        return !passwordsByCedula.containsKey(cedula);
    }

    @Lock(LockType.READ)
    public boolean validate(String cedula, String rawPassword) {
        String persisted = passwordsByCedula.get(cedula);
        if (persisted == null) {
            return cedula != null && cedula.equals(rawPassword);
        }
        return persisted.equals(rawPassword);
    }

    @Lock(LockType.WRITE)
    public void updatePassword(String cedula, String newRawPassword) {
        passwordsByCedula.put(cedula, newRawPassword);
    }
}
