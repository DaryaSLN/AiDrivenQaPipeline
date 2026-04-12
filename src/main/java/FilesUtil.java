import java.nio.file.Files;
import java.nio.file.Path;

public class FilesUtil {

    public static String read(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (Exception e) {
            throw new RuntimeException("Cannot read file: " + path, e);
        }
    }

    public static void write(String path, String content) {
        try {
            Files.createDirectories(Path.of(path).getParent());
            Files.writeString(Path.of(path), content);
        } catch (Exception e) {
            throw new RuntimeException("Cannot write file: " + path, e);
        }
    }
}
