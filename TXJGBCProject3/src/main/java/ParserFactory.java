
public class ParserFactory {
    public static FileParser createParser(String fileType) throws IllegalArgumentException {
        switch (fileType.toLowerCase()) {
            case "excel":
                return new ExcelParser();
            case "csv":
                // 可以添加CSVParser实现
                // return new CSVParser();
            default:
                throw new IllegalArgumentException("不支持的文件类型: " + fileType);
        }
    }
}