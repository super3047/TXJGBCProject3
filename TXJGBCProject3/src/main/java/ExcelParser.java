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

            String id = getCellValueAsString(row.getCell(0));
            String name = getCellValueAsString(row.getCell(1));
            String grade = getCellValueAsString(row.getCell(2));
            String major = getCellValueAsString(row.getCell(3));

            students.add(new Student(id, name, grade, major));
        }

        workbook.close();
        return students;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}