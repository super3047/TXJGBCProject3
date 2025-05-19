

import java.util.ArrayList;
import java.util.List;

public class Experiment {
    private String name;
    private List<Student> missingStudents;

    public Experiment(String name) {
        this.name = name;
        this.missingStudents = new ArrayList<>();
    }

    public void addMissingStudent(Student student) {
        missingStudents.add(student);
    }

    // Getters
    public String getName() { return name; }
    public List<Student> getMissingStudents() { return missingStudents; }
    public int getMissingCount() { return missingStudents.size(); }
}