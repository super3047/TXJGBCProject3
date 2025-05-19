

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
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
        // 实现导出功能
        JOptionPane.showMessageDialog(this, "导出功能待实现", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
}