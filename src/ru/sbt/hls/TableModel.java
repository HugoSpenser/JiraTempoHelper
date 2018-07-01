package ru.sbt.hls;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

class TableModel extends AbstractTableModel {
    private final static String[] COL_NAMES = {"Имя задачи", "Начало", "Окончание", "Продолжительность", "ТКС"};
    private List<Task> taskList = new ArrayList<>();

    private Task currentTask;

    private static long summarizeDurations(Collection<Task> col) {
        return col.stream().collect(Collectors.summarizingLong(Task::getDuration)).getSum();
    }

    @Override
    public String getColumnName(int column) {
        return COL_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
            case 2:
                return LocalTime.class;
            case 3:
                return Long.class;
            case 4:
                return Boolean.class;
            default:
                return Object.class;
        }
    }

    void addTask(String name, boolean isConf) {
        currentTask = new Task(name, isConf);
        taskList.add(currentTask);
    }

    void finishLastTask() {
        currentTask.finish();
        currentTask = null;
    }

    @Override
    public int getRowCount() {
        return taskList.size();
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Task curTask = taskList.get(rowIndex);
        switch (columnIndex) {
            case 0:
                curTask.setName(aValue.toString());
                break;
            case 1:
                curTask.setStartTime(LocalTime.parse(aValue.toString()));
                break;
            case 2:
                curTask.setEndTime(LocalTime.parse(aValue.toString()));
                break;
            case 3:
                curTask.setDuration(Long.parseLong(aValue.toString()));
                break;
            case 4:
                curTask.setTeleconference(Boolean.parseBoolean(aValue.toString()));
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
                final Task task = new Task(line);
                taskList.add(task);
            }
        } catch (IOException ignored) {
        }
    }

    void reCalcDurations() {
        for (Task task: taskList) {
            task.calcDuration();
        }
    }

    void normalizeDurations(int hours) {
        final Map<Boolean, List<Task>> partitionedTasks = taskList.stream().collect(Collectors.partitioningBy(Task::isTeleconference));

        final List<Task> regularTasks = partitionedTasks.get(false);
        final List<Task> confTasks = partitionedTasks.get(true);

        double estimatedMinutes = (hours * 60) - summarizeDurations(confTasks);
        double multiplier = estimatedMinutes / summarizeDurations(regularTasks);

        regularTasks.forEach(task -> task.setDuration(Math.round(task.getDuration() * multiplier)));
        this.fireTableDataChanged();
    }

    String[][] report(int elapsedDuration, int step) {
        // Подсчет общего времени исполнения по каждой каждой задачи

        long sum = elapsedDuration - taskList.stream().collect(Collectors.summarizingLong(Task::getDuration)).getSum();

        final Map<Boolean, List<Task>> tasks = taskList.stream().collect(Collectors.partitioningBy(Task::isTeleconference));
        final List<Task> confTasks = tasks.get(true);
        final List<Task> regularTasks = tasks.get(false);

        Map<String, Long> reportModel = new HashMap<>();
        regularTasks.stream().collect(Collectors.groupingBy(Task::getName)).
                forEach((key, val) -> reportModel.put(key, val.stream().collect(Collectors.summarizingLong(Task::getDuration)).getSum()));


        // Нормализация общего времени по указанному шагу

        reportModel.putAll(confTasks.stream().collect(Collectors.toMap(Task::getName, Task::getDuration)));

        for (Map.Entry<String, Long> entry: reportModel.entrySet()) {
            long duration = entry.getValue();
            long remainder = duration % step;
            if (remainder > step / 2) {
                long add = (((duration / step) + 1) * step) - duration;
                entry.setValue(duration + add);
                sum -= add;
            } else if (remainder != 0) {
                entry.setValue(duration - remainder);
                sum += remainder;
            }
        }

        if (sum != 0) {
            final long fSum = sum;
            if (sum % step != 0) {
                JOptionPane.showMessageDialog(null, "Ошибка при генерации отчета. Экспортируйте таймшит и передайте на анализ разработчику");
            } else {
                Comparator<Map.Entry<String, Long>> durationComparator = (m1, m2) -> (m1.getValue().equals(m2.getValue())) ? 0 : (m1.getValue() > m2.getValue()) ? 1 : -1;
                reportModel.entrySet().stream()
                        .sorted((fSum < 0) ? durationComparator : durationComparator.reversed())
                        .limit(sum / step).forEach(entry -> entry.setValue(entry.getValue() + step * ((fSum > 0) ? -1 : 1)));
            }

        }

        String[][] retVal = new String[reportModel.size()][2];
        int cnt = 0;
        for (Map.Entry<String, Long> row: reportModel.entrySet()) {
            retVal[cnt][0] = row.getKey();
            retVal[cnt][1] = row.getValue().toString();
            cnt++;
        }
        return retVal;

    }
}
