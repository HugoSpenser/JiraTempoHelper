package ru.sbt.hls;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Report {
    private JTable tblReport;
    private JPanel panel1;
    private JScrollPane scrollPane;

    static void show(String[][] data, int elapsedMinutes) {
        JFrame frame = new JFrame("TempoHelper");
        Report instance = new Report();
        frame.setContentPane(instance.panel1);
        instance.tblReport.setModel(new DefaultTableModel(data, new String[]{"Имя", "Продолжительность"}));
        instance.tblReport.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel parent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row == table.getRowCount() - 1) {
                    parent.setFont(parent.getFont().deriveFont(Font.BOLD));
                    if (table.getValueAt(table.getRowCount() - 1, 1).equals(String.valueOf(elapsedMinutes)))
                        parent.setBackground(Color.GREEN);
                    else
                        parent.setBackground(Color.RED);
                }
                return parent;
            }


        });
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
