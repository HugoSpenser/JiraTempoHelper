import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Report {
    private JTable tblReport;
    private JPanel panel1;

    private static Report instance;

    public static void show(String[][] data) {
        JFrame frame = new JFrame("TimesheetHelper");
        instance = new Report();
        frame.setContentPane(instance.panel1);
        instance.tblReport.setModel(new DefaultTableModel(data, new String[]{"Имя", "Продолжительность"}));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
