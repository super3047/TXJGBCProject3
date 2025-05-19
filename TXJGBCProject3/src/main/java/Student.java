
public class Student {
    private String id;
    private String name;
    private String grade;
    private String major;

    public Student(String id, String name, String grade, String major) {
        this.id = id;
        this.name = name;
        this.grade = grade;
        this.major = major;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getGrade() { return grade; }
    public String getMajor() { return major; }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
