import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Paths;

public class TimesheetHelper {
    private JPanel mainPanel;
    private JLabel lbl;
    private JButton start;
    private JTextField textField1;
    private JButton finish;
    private JTable taskList;
    private JPanel northPanel;
    private JPanel southPanel;
    private JButton btnImport;
    private JButton btnExport;
    private JButton btnRecalc;
    private JButton btnNormalize;
    private JSpinner spinner1;
    private JRadioButton radioStrategy1;
    private JRadioButton radioStrategy2;
    private ButtonGroup strategyGroup = new ButtonGroup();

    final JFileChooser fc = new JFileChooser();

    private TableModel tblModel = new TableModel();

    public TimesheetHelper() {
        strategyGroup.add(radioStrategy1);
        strategyGroup.add(radioStrategy2);
        strategyGroup.setSelected(radioStrategy1.getModel(), true);

        taskList.setModel(tblModel);
        taskList.setTableHeader(new JTableHeader());

        start.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (start.isEnabled()) {
                    super.mouseClicked(e);
                    tblModel.addTask(textField1.getText());
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
                fc.showOpenDialog(null);
                tblModel.deserialize(Paths.get(fc.getSelectedFile().getAbsolutePath()));
                tblModel.fireTableDataChanged();
            }
        });
        btnRecalc.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                tblModel.recalcDurations();
                tblModel.fireTableDataChanged();
            }
        });
        btnNormalize.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (radioStrategy2.isSelected()) {
                    JOptionPane.showMessageDialog(null, "NYI. Ждите следующей версии )", "Предупреждение", JOptionPane.WARNING_MESSAGE);
                } else {
                    tblModel.normalizeDurations(Integer.valueOf(spinner1.getValue().toString()));
                    tblModel.fireTableDataChanged();
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("TimesheetHelper");
        frame.setContentPane(new TimesheetHelper().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void switchBtnsVisibility() {
        start.setEnabled(!start.isEnabled());
        finish.setEnabled(!finish.isEnabled());
        textField1.setEnabled(!textField1.isEnabled());
    }

}
