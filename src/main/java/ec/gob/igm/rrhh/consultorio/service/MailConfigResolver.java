package ec.gob.igm.rrhh.consultorio.service;

final class MailConfigResolver {

    private MailConfigResolver() {
    }

    static String resolve(String systemProperty, String envVar, String defaultValue) {
        String fromSystem = normalize(System.getProperty(systemProperty));
        if (fromSystem != null) {
            return fromSystem;
        }
        String fromEnv = normalize(System.getenv(envVar));
        if (fromEnv != null) {
            return fromEnv;
        }
        return defaultValue;
    }

    static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
