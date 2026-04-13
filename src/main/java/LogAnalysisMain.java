import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogAnalysisMain {

    private static final Logger logger = LogManager.getLogger(LogAnalysisMain.class);

    public static void main(String[] args) {
        logger.info("=== AI LOG ANALYSIS STARTED ===");

        // STAGE 8. AI LOG ANALYSIS
        logger.info("STAGE 8: Analyzing logs and generating QA summary...");
        String logs = FilesUtil.read("generated/pipeline.log");
        if (logs == null || logs.trim().isEmpty()) {
            logs = "No logs were provided.";
            logger.warn("Log file is empty or could not be read. Using placeholder message.");
        }

        String summaryPrompt = FilesUtil.read("prompts/05_qa_summary.txt")
                .replace("{{LOGS}}", logs);

        FilesUtil.write("generated/qa_summary_prompt.txt", summaryPrompt);

        String rawSummary = MistralClient.call(summaryPrompt);
        FilesUtil.write("generated/qa_summary_raw.json", rawSummary);

        String summary = extractAssistantContent(rawSummary);
        FilesUtil.write("generated/qa_summary.txt", summary);

        logger.info("QA summary saved: generated/qa_summary.txt");
        logger.info("=== AI LOG ANALYSIS FINISHED ===");
    }

    private static String extractAssistantContent(String rawJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode choice = mapper.readTree(rawJson).path("choices").get(0);

            if (choice == null || choice.isMissingNode()) {
                String errorMessage = "LLM response does not contain expected 'choices' data.";
                logger.error(errorMessage + " Raw response: {}", rawJson);
                throw new RuntimeException(errorMessage + " Raw response: " + rawJson);
            }

            return choice.path("message")
                    .path("content")
                    .asText();
        } catch (Exception e) {
            String errorMessage = "Failed to extract assistant content from LLM response.";
            logger.error(errorMessage + " Raw response: {}", rawJson, e);
            throw new RuntimeException(errorMessage + " Raw response: " + rawJson, e);
        }
    }
}
