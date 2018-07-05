package ru.sbt.hls;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;

import static java.nio.file.StandardOpenOption.*;

public class TempoHelper {
    private final JFileChooser fc = new JFileChooser();
    private JPanel mainPanel;
    private JPanel northPanel;
    private JPanel southPanel;
    private JLabel lbl;
    private JButton start;
    private JComboBox<String> boxTaskName;
    private JButton finish;
    private JTable tblTasks;
    private JButton btnImport;
    private JButton btnExport;
    private JButton btnRecalc;
    private JButton btnNormalize;
    private JSpinner spnElapsedHours;
    private JCheckBox chkCommunicationTask;
    private JButton btnReport;
    private JSpinner spnStepMinutes;
    private JScrollPane scrollPane;
    private final TableModel tblModel = new TableModel();

    private TempoHelper() throws IOException {
        System.setErr(new PrintStream(Files.newOutputStream(Paths.get("eer.log"), WRITE, CREATE, APPEND)));
        btnImport.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        btnExport.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));

        tblTasks.setModel(tblModel);

        spnElapsedHours.setModel(new SpinnerNumberModel(8, 0, 10, 1));
        spnStepMinutes.setModel(new SpinnerNumberModel(6, 0, 600, 1));

        start.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (start.isEnabled()) {
                    super.mouseClicked(e);
                    if (boxTaskName.getSelectedItem() == null) {
                        JOptionPane.showMessageDialog(null, "Введите имя задачи!", "Не указано имя задачи", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    String curTaskName = String.valueOf(boxTaskName.getSelectedItem());
                    tblModel.addTask(curTaskName, chkCommunicationTask.isSelected());

                    boolean isCurNameInList = false;
                    for (int i = 0; i < boxTaskName.getItemCount(); i++) {
                        if (boxTaskName.getItemAt(i).equals(curTaskName)) {
                            isCurNameInList = true;
                            break;
                        }
                    }
                    if (!isCurNameInList)
                        boxTaskName.addItem(curTaskName);

                    switchBtnsVisibility();
                    tblModel.fireTableRowsInserted(tblModel.getRowCount() - 1, tblModel.getRowCount());
                }
            }
        });
        finish.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (finish.isEnabled()) {
                    tblModel.finishLastTask();
                    switchBtnsVisibility();
                    tblModel.fireTableRowsUpdated(tblModel.getRowCount() - 1, tblModel.getRowCount() - 1);
                }
            }
        });
        btnExport.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                fc.showSaveDialog(null);
                tblModel.serialize(Paths.get(fc.getSelectedFile().getAbsolutePath()));
            }
        });
        btnImport.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                fc.setCurrentDirectory(new File(System.getenv("userprofile") + "\\desktop"));
                fc.getActionMap().get("viewTypeDetails").actionPerformed(null);
                fc.showOpenDialog(null);
                tblModel.deserialize(Paths.get(fc.getSelectedFile().getAbsolutePath()));
                tblModel.fireTableDataChanged();
            }
        });
        btnRecalc.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                tblModel.reCalcDurations();
                tblModel.fireTableDataChanged();
            }
        });
        btnNormalize.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (Integer.parseInt(spnElapsedHours.getValue().toString()) == 0) {
                    JOptionPane.showMessageDialog(null, "Для нормализации необходимо ввести продолжительность рабочего дня, отличную от нуля", "Ошибка", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                super.mouseClicked(e);
                tblModel.normalizeDurations(Integer.valueOf(spnElapsedHours.getValue().toString()));
                tblModel.fireTableDataChanged();
            }
        });
        btnReport.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int elapsedMinutes = Integer.valueOf(spnElapsedHours.getValue().toString()) * 60;
                int stepMinutes = Integer.valueOf(spnStepMinutes.getValue().toString());

                if (elapsedMinutes % stepMinutes != 0) {
                    JOptionPane.showMessageDialog(null, "Шаг должен быть кратен целевому общему времени!", "Ошибка", JOptionPane.WARNING_MESSAGE);
                } else {
                    super.mouseClicked(e);
                    Report.show(tblModel.report(elapsedMinutes, stepMinutes));
                }
            }
        });
        tblTasks.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    switch (tblTasks.columnAtPoint(e.getPoint())) {
                        case 1:
                        case 2:
                            int row = tblTasks.rowAtPoint(e.getPoint());
                            int column = tblTasks.columnAtPoint(e.getPoint());
                            LocalTime curTime = LocalTime.parse(tblModel.getValueAt(row, column).toString());
                            new TimeSetter((time) -> tblModel.setValueAt(time, row, column), curTime.getHour(), curTime.getMinute()).show(e.getLocationOnScreen());
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        tblTasks.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_DELETE) {
                    removeRows();
                }
                super.keyTyped(e);
            }
        });
    }

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("TempoHelper");
        frame.setContentPane(new TempoHelper().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void switchBtnsVisibility() {
        start.setEnabled(!start.isEnabled());
        finish.setEnabled(!finish.isEnabled());
        boxTaskName.setEnabled(!boxTaskName.isEnabled());
    }

    private void removeRows() {
        int[] rows = tblTasks.getSelectedRows();
        tblModel.removeRows(rows);
    }

}


