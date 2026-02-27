package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Client minimal OpenAI Chat Completions.
 * Clé via variable d'environnement: OPENAI_API_KEY
 */
public class OpenAiChatService {

    public record Msg(String role, String content) {}

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    // Endpoint "chat completions" (simple à intégrer)
    private static final String ENDPOINT = "https://api.openai.com/v1/chat/completions";

    // Modèle léger et rapide (à adapter si besoin)
    private static final String MODEL = "gpt-4.1-mini";

    public static String chat(List<Msg> messages) throws Exception {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY manquant");
        }

        ObjectNode root = MAPPER.createObjectNode();
        root.put("model", MODEL);
        ArrayNode arr = root.putArray("messages");
        for (Msg m : messages) {
            ObjectNode mm = MAPPER.createObjectNode();
            mm.put("role", m.role());
            mm.put("content", m.content());
            arr.add(mm);
        }
        root.put("temperature", 0.2);

        String json = MAPPER.writeValueAsString(root);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(ENDPOINT))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey.trim())
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new RuntimeException("Erreur API OpenAI (" + resp.statusCode() + "): " + resp.body());
        }

        JsonNode node = MAPPER.readTree(resp.body());
        JsonNode content = node.at("/choices/0/message/content");
        if (content.isMissingNode()) {
            throw new RuntimeException("Réponse OpenAI inattendue: " + resp.body());
        }
        return content.asText();
    }
}




