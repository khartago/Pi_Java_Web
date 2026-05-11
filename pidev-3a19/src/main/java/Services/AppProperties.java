package Services;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Fichier {@code config.properties} fusionné : ressources classpath puis
 * {@code user.dir/config.properties} (même règle que {@link WeatherService} et l’ancien chargement SMTP).
 */
public final class AppProperties {

    private static final Object LOCK = new Object();
    private static volatile Properties merged;

    private AppProperties() {
    }

    private static Properties loadMerged() {
        Properties p = new Properties();
        try (InputStream in = AppProperties.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException ignored) {
            // keep empty
        }
        try {
            Path path = Path.of(System.getProperty("user.dir")).resolve("config.properties");
            if (Files.exists(path)) {
                try (Reader r = Files.newBufferedReader(path)) {
                    p.load(r);
                }
            }
        } catch (IOException ignored) {
            // keep classpath-only
        }
        return p;
    }

    /** Propriétés fusionnées (cache en mémoire pour la durée de vie du JVM). */
    public static Properties merged() {
        Properties local = merged;
        if (local != null) {
            return local;
        }
        synchronized (LOCK) {
            if (merged == null) {
                merged = loadMerged();
            }
            return merged;
        }
    }

    /** Valeur trimée, ou {@code null} si absente / vide. */
    public static String property(String key) {
        String v = merged().getProperty(key);
        if (v == null) {
            return null;
        }
        v = v.trim();
        return v.isEmpty() ? null : v;
    }

    public static String propertyOrDefault(String key, String defaultValue) {
        String v = property(key);
        return v != null ? v : defaultValue;
    }

    /** Pour tests ou rechargement après édition du fichier. */
    public static void clearCache() {
        synchronized (LOCK) {
            merged = null;
        }
    }
}
