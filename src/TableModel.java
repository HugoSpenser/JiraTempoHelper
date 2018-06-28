import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

public class TableModel extends AbstractTableModel {
    private static String[] COL_NAMES = {"Имя задачи", "Начало", "Окончание", "Продолжительность"};

    private List<Task> taskList = new ArrayList<>();

    @Override
    public String getColumnName(int column) {
        return COL_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    void addTask(String name) {
        taskList.add(new Task(name));
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
        final LongSummaryStatistics collect = taskList.stream().collect(Collectors.summarizingLong(Task::getDuration));
        long estimatedMinutes = hours * 60;
        long multiplier = estimatedMinutes / collect.getSum();
        taskList.forEach(task -> task.setDuration(task.getDuration() * multiplier));
    }
}
