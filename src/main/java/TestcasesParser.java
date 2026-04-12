import com.fasterxml.jackson.databind.ObjectMapper;
import model.TestSuite;

import java.nio.file.Files;
import java.nio.file.Path;

public class TestcasesParser {

    public static TestSuite parse(String path) {
        try {
            String json = Files.readString(Path.of(path));
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, TestSuite.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse testcases.json", e);
        }
    }
}
