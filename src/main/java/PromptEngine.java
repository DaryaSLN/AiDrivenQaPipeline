public class PromptEngine {

    public static String buildPrompt(String promptTemplatePath, String checklistPath) {

        String template = FilesUtil.read(promptTemplatePath);
        String checklist = FilesUtil.read(checklistPath);

        return template.replace("{{CHECKLIST}}", checklist);
    }
}