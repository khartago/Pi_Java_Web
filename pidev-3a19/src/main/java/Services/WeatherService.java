package Services;

import model.WeatherInfo;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Service d'appel à l'API Open-Meteo pour la météo actuelle (gratuit, sans clé).
 */
public class WeatherService {

    private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast";

    private final double defaultLat;
    private final double defaultLon;

    public WeatherService() {
        Properties config = loadConfig();
        String latStr = getProp(config, "meteo.default.latitude");
        String lonStr = getProp(config, "meteo.default.longitude");
        this.defaultLat = latStr != null ? Double.parseDouble(latStr.trim()) : 36.8;
        this.defaultLon = lonStr != null ? Double.parseDouble(lonStr.trim()) : 10.1;
    }

    private Properties loadConfig() {
        Properties p = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/config.properties")) {
            if (in != null) p.load(in);
        } catch (IOException ignored) {
        }
        try {
            Path configPath = Path.of(System.getProperty("user.dir")).resolve("config.properties");
            if (Files.exists(configPath)) {
                try (Reader r = Files.newBufferedReader(configPath)) {
                    p.load(r);
                }
            }
        } catch (IOException ignored) {
        }
        return p;
    }

    private static String getProp(Properties p, String key) {
        String v = p.getProperty(key);
        return (v != null && !v.trim().isEmpty()) ? v.trim() : null;
    }

    /**
     * Récupère la météo actuelle pour les coordonnées par défaut.
     * Retourne null en cas d'erreur réseau ou réponse invalide.
     */
    public WeatherInfo getCurrentWeather() {
        return getCurrentWeather(defaultLat, defaultLon);
    }

    public WeatherInfo getCurrentWeather(double lat, double lon) {
        String url = OPEN_METEO_URL + "?latitude=" + lat + "&longitude=" + lon
                + "&current=temperature_2m,weather_code,relative_humidity_2m";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;
            return parseResponse(response.body());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            return null;
        }
    }

    private WeatherInfo parseResponse(String json) {
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            JsonObject root = gson.fromJson(json, JsonObject.class);
            if (root == null || !root.has("current")) return null;
            JsonObject current = root.getAsJsonObject("current");
            double temp = current.has("temperature_2m") ? current.get("temperature_2m").getAsDouble() : 0;
            int code = current.has("weather_code") ? current.get("weather_code").getAsInt() : 0;
            Integer humidity = current.has("relative_humidity_2m") ? current.get("relative_humidity_2m").getAsInt() : null;
            String description = weatherCodeToDescription(code);
            return new WeatherInfo(temp, code, description, humidity);
        } catch (Exception e) {
            return null;
        }
    }

    private static String weatherCodeToDescription(int code) {
        if (code == 0) return "Ciel dégagé";
        if (code == 1) return "Principalement dégagé";
        if (code == 2) return "Partiellement nuageux";
        if (code == 3) return "Couvert";
        if (code >= 45 && code <= 48) return "Brouillard";
        if (code >= 51 && code <= 57) return "Bruine";
        if (code >= 61 && code <= 67) return "Pluie";
        if (code >= 71 && code <= 77) return "Neige";
        if (code >= 80 && code <= 82) return "Averses";
        if (code >= 85 && code <= 86) return "Averses de neige";
        if (code >= 95 && code <= 99) return "Orage";
        return "Variable";
    }
}
