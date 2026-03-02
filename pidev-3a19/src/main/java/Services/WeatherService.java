package Services;

import model.WeatherInfo;
import com.google.gson.JsonObject;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Service météo : Open-Meteo (dashboard) + OpenWeatherMap (game).
 */
public class WeatherService {

    private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast";
    private static final String OPENWEATHER_API_KEY = "0779acbb9aee496ae9653db6b1f6a910";

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

    /** Open-Meteo : météo pour le dashboard (sans clé). */
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

    /** Retourne un snapshot JSON de la meteo actuelle pour stockage dans probleme.meteo_snapshot. */
    public String getWeatherSnapshotJson() {
        WeatherInfo info = getCurrentWeather();
        if (info == null) return null;
        try {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            return gson.toJson(java.util.Map.of(
                    "temp", info.getTemperature(),
                    "description", info.getDescription() != null ? info.getDescription() : "",
                    "humidity", info.getHumidity() != null ? info.getHumidity() : 0,
                    "timestamp", java.time.LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            return null;
        }
    }

    /** OpenWeatherMap : température par ville (pour le jeu). */
    public static double getTemperature(String city) {
        try {
            String urlString = "https://api.openweathermap.org/data/2.5/weather?q="
                    + city + "&units=metric&appid=" + OPENWEATHER_API_KEY;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonObject = new JSONObject(response.toString());
            return jsonObject.getJSONObject("main").getDouble("temp");
        } catch (Exception e) {
            return -1;
        }
    }
}
