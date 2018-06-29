import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

public class TableModel extends AbstractTableModel {
    private static String[] COL_NAMES = {"Имя задачи", "Начало", "Окончание", "Продолжительность"};

    private List<Task> taskList = new ArrayList<>();
    private List<Task> regularTaskList = new ArrayList<>();

    @Override
    public String getColumnName(int column) {
        return COL_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    void addTask(String name, boolean isConf) {
        Task task = new Task(name, isConf);
        taskList.add(task);
        if (!isConf)
            regularTaskList.add(task);
    }

    void finishLastTask() {
        taskList.get(taskList.size() - 1).finish();
    }

    @Override
    public int getRowCount() {
        return taskList.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                taskList.get(rowIndex).setName(aValue.toString());
                break;
            default:
                break;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return taskList.get(rowIndex).get(columnIndex);
    }

    void serialize(Path filePath) {
        try {
            Files.write(filePath, taskList.stream().map(Task::toString).collect(Collectors.toList()), WRITE, CREATE, TRUNCATE_EXISTING);
        } catch (IOException ignored) {
        }
    }

    void deserialize(Path filePath) {
        taskList = new ArrayList<>();
        try {
            for (String line: Files.readAllLines(filePath)) {
                taskList.add(new Task(line, ";"));
            }
        } catch (IOException ignored) {
        }
    }

    void recalcDurations() {
        for (Task task: taskList) {
            task.calcDuration();
        }
    }

    void normalizeDurations(int hours) {
        final LongSummaryStatistics collect = regularTaskList.stream().collect(Collectors.summarizingLong(Task::getDuration));
        long estimatedMinutes = hours * 60;
        long multiplier = estimatedMinutes / collect.getSum();
        regularTaskList.forEach(task -> task.setDuration(task.getDuration() * multiplier));
        this.fireTableDataChanged();
    }

    public String[][] report() {
        Map<String, Long> reportModel = new HashMap<>();
        for (Task t : taskList) {
            String tName = t.getName();
            long tDuration = t.getDuration();

            if (reportModel.containsKey(tName))
                tDuration = reportModel.get(tName) + tDuration;
            reportModel.put(tName, tDuration);
        }

        String[][] retVal = new String[reportModel.size()][2];
        int cnt = 0;
        for (Map.Entry<String, Long> row : reportModel.entrySet()) {
            retVal[cnt][0] = row.getKey();
            retVal[cnt][1] = row.getValue().toString();
            cnt++;
        }
        return retVal;

    }
}
