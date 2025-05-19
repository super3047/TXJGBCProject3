
import java.io.File;
import java.util.List;

public class StudentManager {
    private DatabaseHelper dbHelper;

    public StudentManager() {
        this.dbHelper = new DatabaseHelper();
    }

    public void importStudentsFromExcel(File file) throws Exception {
        FileParser parser = ParserFactory.createParser("excel");
        List<Student> students = parser.parse(file);

        for (Student student : students) {
            dbHelper.addStudent(student);
        }
    }

    public List<Student> getAllStudents() throws Exception {
        return dbHelper.getAllStudents();
    }



    }