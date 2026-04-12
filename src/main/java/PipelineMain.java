import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PipelineMain {

    private static final Logger logger = LogManager.getLogger(PipelineMain.class);

    public static void main(String[] args) {

        logger.info("=== AI QA PIPELINE STARTED ===");

        // STAGE 1. BUILD PROMPT FROM CHECKLIST
        logger.info("STAGE 1: Building prompt from checklist...");
        String prompt = PromptEngine.buildPrompt(
                "prompts/01_scenarios_from_checklist.txt",
                "checklist_create_account.txt"
        );
        FilesUtil.write("generated/final_prompt.txt", prompt);
        logger.debug("Final prompt content:\n{}", prompt);

        // STAGE 2. PII SCAN & MASK
        logger.info("STAGE 2: Scanning for PII and masking if necessary...");
        PiiReport report = PiiScanner.scan(prompt);
        FilesUtil.write("generated/pii_report.txt", report.toText());

        String finalPrompt = prompt;

        if (report.hasFindings()) {
            logger.warn("PII detected. Masking input.");
            finalPrompt = PiiMasker.mask(prompt);
            FilesUtil.write("generated/prompt_masked.txt", finalPrompt);
        }

        // STAGE 3. GENERATE SCENARIOS
        logger.info("STAGE 3: Generating scenarios from LLM...");
        String rawScenarios = MistralClient.call(finalPrompt);
        FilesUtil.write("generated/scenarios_raw.json", rawScenarios);
        logger.debug("Raw scenarios from LLM: {}", rawScenarios);

        String scenarios = extractAssistantContent(rawScenarios);
        FilesUtil.write("generated/ai_output.txt", scenarios);
        logger.info("Scenarios generated successfully.");

        // ================================
        // STAGE 4. GENERATE JSON TESTCASES
        logger.info("STAGE 4: Generating JSON test cases from scenarios...");
        String jsonPrompt = FilesUtil.read("prompts/02_testcases_json.txt")
                .replace("{{SCENARIOS}}", scenarios);

        FilesUtil.write("generated/testcases_prompt.txt", jsonPrompt);

        String rawJson = MistralClient.call(jsonPrompt);
        FilesUtil.write("generated/testcases_raw.json", rawJson);
        logger.debug("Raw JSON from LLM: {}", rawJson);

        String llmJsonText = extractAssistantContent(rawJson);
        FilesUtil.write("generated/testcases_llm.txt", llmJsonText);

        String pureJson = JsonExtractor.extractJson(llmJsonText);
        FilesUtil.write("generated/testcases.json", pureJson);

        logger.info("Testcases generated: generated/testcases.json");

        // STAGE 5. GENERATE AUTOTESTS
        logger.info("STAGE 5: Generating autotests...");
        TestGenerator.generate();
        logger.info("Autotests generated.");

        // STAGE 6. AI CODE REVIEW
        logger.info("STAGE 6: Performing AI code review...");
        String generatedTest = FilesUtil.read(
                "src/test/java/org/demo/generated/GeneratedCreateAccountTest.java"
        );

        String reviewPrompt = FilesUtil.read("prompts/03_code_review.txt")
                .replace("{{CODE}}", generatedTest);

        FilesUtil.write("generated/code_review_prompt.txt", reviewPrompt);

        String rawReview = MistralClient.call(reviewPrompt);
        FilesUtil.write("generated/code_review_raw.json", rawReview);

        String review = extractAssistantContent(rawReview);
        FilesUtil.write("generated/code_review.txt", review);

        logger.info("AI code review saved: generated/code_review.txt");

        // STAGE. AI BUG REPORT (DESIGN-TIME)
        logger.info("STAGE 7: Generating AI bug report...");
        String checklist = FilesUtil.read("checklist_create_account.txt");
        String testcases = FilesUtil.read("generated/testcases.json");
        String codeReview = FilesUtil.read("generated/code_review.txt");

        String bugPrompt = FilesUtil.read("prompts/04_bug_report.txt")
                .replace("{{CHECKLIST}}", checklist)
                .replace("{{TESTCASES}}", testcases)
                .replace("{{REVIEW}}", codeReview);

        FilesUtil.write("generated/bug_report_prompt.txt", bugPrompt);

        String rawBug = MistralClient.call(bugPrompt);
        FilesUtil.write("generated/bug_report_raw.json", rawBug);

        String bugText = extractAssistantContent(rawBug);
        FilesUtil.write("generated/bug_report_llm.txt", bugText);

        String pureBugJson = JsonExtractor.extractJson(bugText);
        FilesUtil.write("generated/bug_report.json", pureBugJson);

        logger.info("Bug report saved: generated/bug_report.json");

        // STAGE 8. AI LOG ANALYSIS
        logger.info("STAGE 8: Analyzing logs and generating QA summary...");
        String logs = FilesUtil.read("generated/pipeline.log");
        String summaryPrompt = FilesUtil.read("prompts/05_qa_summary.txt")
                .replace("{{LOGS}}", logs);

        FilesUtil.write("generated/qa_summary_prompt.txt", summaryPrompt);

        String rawSummary = MistralClient.call(summaryPrompt);
        FilesUtil.write("generated/qa_summary_raw.json", rawSummary);

        String summary = extractAssistantContent(rawSummary);
        FilesUtil.write("generated/qa_summary.txt", summary);

        logger.info("QA summary saved: generated/qa_summary.txt");

        logger.info("=== AI QA PIPELINE FINISHED ===");
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