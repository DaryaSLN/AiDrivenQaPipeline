import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MistralClient {

    private static final Logger logger = LogManager.getLogger(MistralClient.class);
    private static final String API_URL = "https://api.mistral.ai/v1/chat/completions";
    private static final String API_KEY = System.getenv("MISTRAL_API_KEY");

    public static String call(String prompt) {

        if (API_KEY == null || API_KEY.isBlank()) {
            logger.error("MISTRAL_API_KEY not set");
            throw new RuntimeException("MISTRAL_API_KEY not set");
        }

        try {
            String safePrompt = prompt
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\r", "\\r")
                    .replace("\n", "\\n")
                    .replace("\t", "\\t");

            String body = """
                    {
                      "model": "mistral-small-latest",
                      "messages": [
                        { "role": "system", "content": "You are a QA automation engineer. Return structured output." },
                        { "role": "user", "content": "%s" }
                      ],
                      "temperature": 0.2
                    }
                    """.formatted(safePrompt);

            logger.debug("Sending request to Mistral API. Body: {}", body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            logger.debug("Received response from Mistral API. Status: {}, Body: {}", response.statusCode(), response.body());

            return response.body();

        } catch (Exception e) {
            logger.error("Error calling Mistral API", e);
            throw new RuntimeException(e);
        }
    }
}