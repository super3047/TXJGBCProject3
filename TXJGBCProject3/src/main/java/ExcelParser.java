import org.apache.poi.ss.usermodel.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExcelParser implements FileParser {
    @Override
    public List<Student> parse(File file) throws Exception {
        List<Student> students = new ArrayList<>();
        Workbook workbook = WorkbookFactory.create(file);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // 跳过标题行

            String id = row.getCell(0).getStringCellValue();
            String name = row.getCell(1).getStringCellValue();
            String grade = row.getCell(2).getStringCellValue();
            String major = row.getCell(3).getStringCellValue();

            students.add(new Student(id, name, grade, major));
        }

        workbook.close();
        return students;
    }
}
