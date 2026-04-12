import java.util.ArrayList;
import java.util.List;

public class PiiReport {

    private final List<String> findings = new ArrayList<>();

    public void add(String finding) {
        findings.add(finding);
    }

    public boolean hasFindings() {
        return !findings.isEmpty();
    }

    public List<String> getFindings() {
        return findings;
    }

    public String toText() {
        if (findings.isEmpty()) {
            return "No PII detected";
        }

        StringBuilder sb = new StringBuilder("PII DETECTED:\n");
        for (String f : findings) {
            sb.append("- ").append(f).append("\n");
        }
        return sb.toString();
    }
}
