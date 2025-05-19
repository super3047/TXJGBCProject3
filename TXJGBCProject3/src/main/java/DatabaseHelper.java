import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlite:erat.db";
    private Connection conn;

    public DatabaseHelper() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String createStudents = "CREATE TABLE IF NOT EXISTS students (" +
                "id TEXT PRIMARY KEY, name TEXT NOT NULL, grade TEXT, major TEXT)";
        String createExperiments = "CREATE TABLE IF NOT EXISTS experiments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL)";
        String createMissing = "CREATE TABLE IF NOT EXISTS missing_reports (" +
                "student_id TEXT, experiment_id INTEGER, " +
                "FOREIGN KEY(student_id) REFERENCES students(id), " +
                "FOREIGN KEY(experiment_id) REFERENCES experiments(id), " +
                "PRIMARY KEY (student_id, experiment_id))";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createStudents);
            stmt.execute(createExperiments);
            stmt.execute(createMissing);
        }
    }

    // 检查学生是否存在
    private boolean isStudentExists(String studentId) throws SQLException {
        String sql = "SELECT id FROM students WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // 学生相关操作
    public void addStudent(Student student) throws SQLException {
        if (!isStudentExists(student.getId())) {
            String sql = "INSERT INTO students(id, name, grade, major) VALUES(?,?,?,?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, student.getId());
                pstmt.setString(2, student.getName());
                pstmt.setString(3, student.getGrade());
                pstmt.setString(4, student.getMajor());
                pstmt.executeUpdate();
            }
        }
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT id, name, grade, major FROM students";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(new Student(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("grade"),
                        rs.getString("major")
                ));
            }
        }
        return students;
    }

    // 实验相关操作
    public void addExperiment(Experiment experiment) throws SQLException {
        String sql = "INSERT INTO experiments(name) VALUES(?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, experiment.getName());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                // 可以设置实验ID
            }
        }
    }

    // 其他数据库操作方法...
}