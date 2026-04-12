import model.TestCase;
import model.TestSuite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestGenerator {

    private static final Logger logger = LogManager.getLogger(TestGenerator.class);

    public static void generate() {

        logger.info("Parsing test cases from JSON...");
        TestSuite suite = TestcasesParser.parse("generated/testcases.json");
        logger.info("Found {} test cases to generate.", suite.testcases.size());

        StringBuilder code = new StringBuilder();

        code.append("""
                package org.demo.generated;
                import org.demo.ui.BaseUiTest;
                import org.demo.ui.CreateAccountPage;
                import org.testng.Assert;
                import org.testng.annotations.Test;
                public class GeneratedCreateAccountTest extends org.demo.ui.BaseUiTest {
                """);

        for (TestCase tc : suite.testcases) {
            logger.debug("Generating test for: {}", tc.title);
            code.append(generateTest(tc));
        }

        code.append("}");

        String generatedCode = code.toString();
        logger.debug("Final generated code:\n{}", generatedCode);

        FilesUtil.write(
                "src/test/java/org/demo/generated/GeneratedCreateAccountTest.java",
                generatedCode
        );

        logger.info("Autotests generated successfully.");
    }

    private static String generateTest(TestCase tc) {

        String methodName = tc.id.toLowerCase() + "_" +
                tc.title.replaceAll("[^a-zA-Z0-9_]", ""); // Allow numbers in method name

        // Extract data from TestCase.data map
        String firstName = tc.data != null ? tc.data.getOrDefault("firstName", "") : "";
        String lastName = tc.data != null ? tc.data.getOrDefault("lastName", "") : "";
        String email = tc.data != null ? tc.data.getOrDefault("email", "") : "";
        String password = tc.data != null ? tc.data.getOrDefault("password", "") : "";
        String confirmPassword = tc.data != null ? tc.data.getOrDefault("confirmPassword", "") : "";

        // Wrap string values in double quotes for Java code
        // and handle potential nulls from getOrDefault to ensure proper string literals
        firstName = "\"" + firstName.replace("\"", "\\\"") + "\"";
        lastName = "\"" + lastName.replace("\"", "\\\"") + "\"";
        email = "\"" + email.replace("\"", "\\\"") + "\"";
        password = "\"" + password.replace("\"", "\\\"") + "\"";
        confirmPassword = "\"" + confirmPassword.replace("\"", "\\\"") + "\"";


        String actionCall = String.format("page.createAccount(%s, %s, %s, %s, %s);",
                firstName, lastName, email, password, confirmPassword);

        String assertion;
        if (tc.type.equalsIgnoreCase("positive")) {
            assertion = String.format("Assert.assertTrue(page.isAccountCreatedSuccessfully(), \"Account should be created successfully for scenario '%s'.\");", tc.title);
        } else {
            assertion = String.format("Assert.assertTrue(page.isErrorVisible(), \"Error should be visible for scenario '%s'.\");", tc.title);
        }


        return """
                
                    @Test
                    public void %s() {
                        CreateAccountPage page = new CreateAccountPage(driver);
                        page.open();
                        %s
                        %s
                    }
                """.formatted(methodName, actionCall, assertion);
    }
}