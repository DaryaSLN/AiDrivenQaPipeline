public class JsonExtractor {

    public static String extractJson(String text) {

        if (text == null) {
            throw new RuntimeException("LLM returned null");
        }

        String cleaned = text
                .replace("```json", "")
                .replace("```", "")
                .trim();

        int start = cleaned.indexOf("{");
        int end = cleaned.lastIndexOf("}");

        if (start == -1 || end == -1) {
            throw new RuntimeException("No JSON object found in LLM output");
        }

        return cleaned.substring(start, end + 1).trim();
    }
}
