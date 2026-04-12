import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PiiScanner {

    private static final Pattern EMAIL =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    private static final Pattern PHONE =
            Pattern.compile("\\+?\\d[\\d\\s\\-()]{7,}");

    private static final Pattern PASSWORD =
            Pattern.compile("(?i)(password\\s*[:=]\\s*\\S+)");

    public static PiiReport scan(String text) {

        PiiReport report = new PiiReport();

        find(text, EMAIL, "EMAIL", report);
        find(text, PHONE, "PHONE", report);
        find(text, PASSWORD, "PASSWORD", report);

        return report;
    }

    private static void find(String text, Pattern pattern, String type, PiiReport report) {
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            report.add(type + ": " + m.group());
        }
    }
}