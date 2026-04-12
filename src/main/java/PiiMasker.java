public class PiiMasker {

    public static String mask(String text) {

        return text
                .replaceAll("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", "[EMAIL]")
                .replaceAll("\\+?\\d[\\d\\s\\-()]{7,}", "[PHONE]")
                .replaceAll("(?i)(password\\s*[:=]\\s*)\\S+", "$1[SECRET]");
    }
}
