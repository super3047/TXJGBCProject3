import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {
    private StudentManager studentManager;
    private ReportAnalyzer reportAnalyzer;

    private JTextArea outputArea;

    public MainFrame() {
        super("实验报告统计分析工具 ERAT");
        this.studentManager = new StudentManager();
        this.reportAnalyzer = new ReportAnalyzer(studentManager);

        initUI();
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 顶部工具栏
        JToolBar toolBar = new JToolBar();
        JButton importBtn = new JButton("导入学生名单");
        JButton analyzeBtn = new JButton("分析报告");
        JButton exportBtn = new JButton("导出结果");

        importBtn.addActionListener(this::importStudents);
        analyzeBtn.addActionListener(this::analyzeReports);
        exportBtn.addActionListener(this::exportResults);

        toolBar.add(importBtn);
        toolBar.add(analyzeBtn);
        toolBar.add(exportBtn);

        // 中间输出区域
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        mainPanel.add(toolBar, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void importStudents(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                studentManager.importStudentsFromExcel(file);
                outputArea.append("成功导入学生名单: " + file.getName() + "\n");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "导入失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void analyzeReports(ActionEvent e) {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (dirChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dir = dirChooser.getSelectedFile();
            try {
                Map<Student, List<String>> result = reportAnalyzer.analyzeByStudent(dir);

                outputArea.append("\n=== 缺交报告统计 ===\n");
                for (Map.Entry<Student, List<String>> entry : result.entrySet()) {
                    outputArea.append(entry.getKey().getName() + " (" + entry.getKey().getId() + "): ");
                    outputArea.append(String.join(", ", entry.getValue()) + "\n");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "分析失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void exportResults(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择导出文件的路径");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }

            @Override
            public String getDescription() {
                return "CSV 文件 (*.csv)";
            }
        });

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (FileWriter writer = new FileWriter(fileToSave)) {
                // 写入 CSV 文件的标题行
                writer.append("姓名,学号,缺交实验\n");

                // 获取分析结果
                File reportsDir = new File("path/to/reports/dir"); // 这里需要替换为实际的报告目录路径
                Map<Student, List<String>> result = reportAnalyzer.analyzeByStudent(reportsDir);

                // 遍历分析结果，写入 CSV 文件
                for (Map.Entry<Student, List<String>> entry : result.entrySet()) {
                    Student student = entry.getKey();
                    List<String> missingExperiments = entry.getValue();

                    for (String experiment : missingExperiments) {
                        writer.append(student.getName());
                        writer.append(",");
                        writer.append(student.getId());
                        writer.append(",");
                        writer.append(experiment);
                        writer.append("\n");
                    }
                }

                JOptionPane.showMessageDialog(this, "导出成功: " + fileToSave.getName(),
                        "提示", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "导出失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}