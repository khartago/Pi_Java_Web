package Services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * Client OpenAI Chat Completions (marketplace).
 * Priorité : {@code OPENAI_API_KEY} (env), puis {@code openai.api.key} dans {@code config.properties}.
 * URL et modèle : {@code openai.api.base.url}, {@code openai.model} (parité Symfony ASSISTANT_* / .env).
 */
public class OpenAiChatService {

    private static final Gson GSON = new Gson();

    public static final class Msg {
        public final String role;
        public final String content;

        public Msg(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    /**
     * Appelle l’API chat completions (OpenAI-compatible). Bloque jusqu’à la réponse ou une erreur.
     */
    public static String chat(List<Msg> messages) throws Exception {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            String fromFile = AppProperties.property("openai.api.key");
            if (fromFile != null) {
                apiKey = fromFile;
            }
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                "Clé OpenAI absente : définissez OPENAI_API_KEY ou openai.api.key dans config.properties."
            );
        }

        String base = AppProperties.propertyOrDefault("openai.api.base.url", "https://api.openai.com/v1");
        base = base.replaceAll("/+$", "");
        String url = base + "/chat/completions";

        String model = AppProperties.propertyOrDefault("openai.model", "gpt-3.5-turbo");

        JsonObject root = new JsonObject();
        root.addProperty("model", model);
        JsonArray arr = new JsonArray();
        for (Msg m : messages) {
            JsonObject msg = new JsonObject();
            msg.addProperty("role", m.role);
            msg.addProperty("content", m.content);
            arr.add(msg);
        }
        root.add("messages", arr);

        byte[] body = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey.trim());
        conn.setDoOutput(true);
        conn.setFixedLengthStreamingMode(body.length);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body);
        }

        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        if (is == null) {
            throw new RuntimeException("OpenAI API error: " + code);
        }

        String responseBody;
        try (Scanner s = new Scanner(is, StandardCharsets.UTF_8.name()).useDelimiter("\\A")) {
            responseBody = s.hasNext() ? s.next() : "";
        }

        if (code >= 300) {
            throw new RuntimeException("OpenAI API error " + code + ": " + responseBody);
        }

        JsonObject json = GSON.fromJson(responseBody, JsonObject.class);
        JsonArray choices = json.getAsJsonArray("choices");
        if (choices == null || choices.size() == 0) {
            throw new RuntimeException("OpenAI: aucune réponse dans choices");
        }
        String content = choices.get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
        return content != null ? content.trim() : "";
    }
}
