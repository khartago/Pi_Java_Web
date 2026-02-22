package Services;

import model.DiagnosticSuggestion;
import model.Probleme;
import Utils.ImageUploadHelper;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service d'appel à l'API Hugging Face Router (Qwen) pour suggérer un diagnostic
 * (cause, solution, médicament) à partir de la description du problème.
 * Utilise https://router.huggingface.co (format chat completions compatible OpenAI).
 */
public class DiagnosticAIService {

    private static final String HF_ROUTER_CHAT_URL = "https://router.huggingface.co/v1/chat/completions";
    private static final String PROMPT = "Tu es un expert en problèmes agricoles. À partir de la/les photo(s) et de la description fournies, "
            + "fournis au format JSON strict uniquement, sans texte avant ou après, avec exactement ces clés : "
            + "\"cause\", \"solutionProposee\", \"medicament\". "
            + "cause = cause probable du problème, solutionProposee = solution recommandée, "
            + "medicament = nom du médicament/traitement recommandé et dosage si possible. "
            + "Réponds uniquement avec le JSON, pas de markdown.";

    private final Gson gson = new Gson();
    private final String hfApiKey;
    private final String hfModel;

    public DiagnosticAIService() {
        Properties config = loadConfig();
        this.hfApiKey = getProp(config, "hf.api.key");
        this.hfModel = getProp(config, "hf.model");
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
     * Génère une suggestion de diagnostic à partir du problème (description + photos).
     * Envoie texte et images au format OpenAI (content = tableau image_url + text).
     *
     * @throws RuntimeException si clé API manquante, erreur réseau ou réponse mal formée
     */
    public DiagnosticSuggestion generateFromProbleme(Probleme p) {
        if (hfApiKey == null || hfApiKey.isEmpty()) {
            throw new RuntimeException("Clé Hugging Face manquante. Renseignez hf.api.key dans config.properties.");
        }
        String model = (hfModel != null && !hfModel.isEmpty()) ? hfModel : "Qwen/Qwen3.5-397B-A17B";
        String promptText = "Description du problème : " + (p.getDescription() != null ? p.getDescription() : "") + "\n\n" + PROMPT;

        List<Map<String, Object>> contentList = new ArrayList<>();
        Path uploadsBase = ImageUploadHelper.getBaseUploadDir().getParent();
        if (p.getPhotos() != null && !p.getPhotos().isEmpty()) {
            for (String rel : p.getPhotos().split(";")) {
                String trimmed = rel.trim();
                if (trimmed.isEmpty()) continue;
                Path fullPath = uploadsBase.resolve(trimmed);
                if (!Files.exists(fullPath)) continue;
                try {
                    byte[] bytes = Files.readAllBytes(fullPath);
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    String mime = "image/jpeg";
                    String lower = trimmed.toLowerCase();
                    if (lower.endsWith(".png")) mime = "image/png";
                    else if (lower.endsWith(".gif")) mime = "image/gif";
                    else if (lower.endsWith(".webp")) mime = "image/webp";
                    String dataUrl = "data:" + mime + ";base64," + base64;
                    contentList.add(Map.of(
                            "type", "image_url",
                            "image_url", Map.of("url", dataUrl)
                    ));
                } catch (IOException e) {
                    throw new RuntimeException("Impossible de charger l'image : " + trimmed, e);
                }
            }
        }
        contentList.add(Map.of("type", "text", "text", promptText));

        Map<String, Object> message = Map.of("role", "user", "content", contentList);
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(message));
        body.put("max_tokens", 4096);
        body.put("temperature", 0.6);
        body.put("top_p", 0.95);
        body.put("top_k", 20);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HF_ROUTER_CHAT_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + hfApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException("Erreur réseau : " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Appel interrompu");
        }

        if (response.statusCode() != 200) {
            String bodyStr = response.body();
            if (response.statusCode() == 401) {
                throw new RuntimeException("Clé Hugging Face invalide. Vérifiez hf.api.key sur https://huggingface.co/settings/tokens");
            }
            throw new RuntimeException("Hugging Face erreur " + response.statusCode() + " : " + (bodyStr != null && bodyStr.length() > 250 ? bodyStr.substring(0, 250) + "..." : bodyStr));
        }

        String rawBody = response.body();
        String responseText = extractHfRouterResponseText(rawBody);
        if (responseText == null || responseText.isEmpty()) {
            String preview = rawBody != null && rawBody.length() > 400 ? rawBody.substring(0, 400) + "..." : rawBody;
            throw new RuntimeException("Réponse Hugging Face vide ou mal formée. Réponse reçue : " + (preview != null ? preview : "(vide)"));
        }
        return parseSuggestionFromText(responseText);
    }

    /**
     * Extrait le texte de la réponse HF Router.
     * Gère : choices[0].message.content (string ou tableau de parties type/text).
     */
    private String extractHfRouterResponseText(String jsonBody) {
        if (jsonBody == null || jsonBody.isBlank()) return null;
        try {
            JsonObject root = gson.fromJson(jsonBody, JsonObject.class);
            if (root == null || !root.has("choices")) return null;
            var choices = root.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) return null;
            JsonObject choice = choices.get(0).getAsJsonObject();
            // Format OpenAI : choice.message.content
            if (choice.has("message")) {
                JsonObject message = choice.getAsJsonObject("message");
                JsonElement content = message.get("content");
                if (content == null) return null;
                if (content.isJsonPrimitive() && content.getAsJsonPrimitive().isString()) {
                    return content.getAsString();
                }
                if (content.isJsonArray()) {
                    var parts = content.getAsJsonArray();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < parts.size(); i++) {
                        JsonObject part = parts.get(i).getAsJsonObject();
                        if (part.has("type") && "text".equals(part.get("type").getAsString()) && part.has("text")) {
                            sb.append(part.get("text").getAsString());
                        }
                    }
                    return sb.length() > 0 ? sb.toString() : null;
                }
            }
            // Certaines APIs : choice.text
            if (choice.has("text")) {
                return choice.get("text").getAsString();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private DiagnosticSuggestion parseSuggestionFromText(String text) {
        String json = text.trim();
        Matcher m = Pattern.compile("(?s)```(?:json)?\\s*(.*?)\\s*```").matcher(json);
        if (m.find()) {
            json = m.group(1).trim();
        }
        try {
            JsonObject obj = gson.fromJson(json, JsonObject.class);
            if (obj == null) throw new RuntimeException("JSON invalide");
            String cause = obj.has("cause") ? obj.get("cause").getAsString() : "";
            String solutionProposee = obj.has("solutionProposee") ? obj.get("solutionProposee").getAsString() : "";
            String medicament = obj.has("medicament") ? obj.get("medicament").getAsString() : "";
            return new DiagnosticSuggestion(cause, solutionProposee, medicament);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de parser la réponse IA : " + e.getMessage());
        }
    }
}
