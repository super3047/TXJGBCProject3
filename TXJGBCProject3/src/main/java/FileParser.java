import java.io.File;
import java.util.List;

public interface FileParser {
    List<Student> parse(File file) throws Exception;
}