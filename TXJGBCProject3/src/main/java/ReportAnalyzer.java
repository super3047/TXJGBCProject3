
import java.io.File;
import java.util.*;

public class ReportAnalyzer {
    private StudentManager studentManager;

    public ReportAnalyzer(StudentManager studentManager) {
        this.studentManager = studentManager;
    }

    public Map<Student, List<String>> analyzeByStudent(File reportsDir) throws Exception {
        Map<Student, List<String>> result = new HashMap<>();
        List<Student> allStudents = studentManager.getAllStudents();

        if (!reportsDir.isDirectory()) {
            throw new IllegalArgumentException("必须提供报告目录");
        }

        File[] experimentDirs = reportsDir.listFiles(File::isDirectory);
        if (experimentDirs == null) return result;

        for (File expDir : experimentDirs) {
            String expName = expDir.getName();
            File[] reportFiles = expDir.listFiles();
            if (reportFiles == null) continue;

            Set<String> submittedIds = new HashSet<>();
            for (File report : reportFiles) {
                String fileName = report.getName();
                // 假设文件名格式为"学号_姓名_实验报告.docx"
                String[] parts = fileName.split("_");
                if (parts.length > 0) {
                    submittedIds.add(parts[0]);
                }
            }

            for (Student student : allStudents) {
                if (!submittedIds.contains(student.getId())) {
                    result.computeIfAbsent(student, k -> new ArrayList<>())
                            .add(expName);
                }
            }
        }

        return result;
    }

    public List<Experiment> analyzeByExperiment(File reportsDir) throws Exception {
        List<Experiment> experiments = new ArrayList<>();
        // 实现类似analyzeByStudent的逻辑
        return experiments;
    }
}