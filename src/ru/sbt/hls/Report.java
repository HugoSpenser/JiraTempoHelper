package ru.sbt.hls;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Report {
    private JTable tblReport;
    private JPanel panel1;
    private JScrollPane scrollPane;

    static void show(String[][] data) {
        JFrame frame = new JFrame("TempoHelper");
        Report instance = new Report();
        frame.setContentPane(instance.panel1);
        instance.tblReport.setModel(new DefaultTableModel(data, new String[]{"Имя", "Продолжительность"}));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
