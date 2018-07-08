package ru.sbt.hls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

class Task {
    private static final Logger LOG = LoggerFactory.getLogger(Task.class);
    private static final String ERR_COULD_NOT_DESERIALIZE = "Не удалось десериализовать задачу из строки: '%s'";
    static final DateTimeFormatter D_INP_FORMAT = DateTimeFormatter.ofPattern("H:m");
    private static final DateTimeFormatter D_OUT_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isTeleconference;
    private long duration;

    Task(String name, boolean isTeleconference) {
        this.name = name;
        this.isTeleconference = isTeleconference;
        startTime = LocalTime.now();
    }

    static Task deserialize(String serialized) {
        String[] parts = serialized.split(";");
        if (parts.length < 4) {
            LOG.warn(String.format(ERR_COULD_NOT_DESERIALIZE, serialized));
            return null;
        } else {
            Task curTask = new Task();
            curTask.name = parts[0];
            curTask.startTime = LocalTime.parse(parts[1], D_INP_FORMAT);
            curTask.endTime = LocalTime.parse(parts[2], D_INP_FORMAT);
            curTask.duration = Long.parseLong(parts[3]);
            curTask.isTeleconference = Boolean.parseBoolean(parts[4]);
            return curTask;
        }
    }

    private Task() {
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    long getDuration() {
        return duration;
    }

    void setDuration(long duration) {
        this.duration = duration;
    }

    void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    Object get(int idx) {
        switch (idx) {
            case 0:
                return name;
            case 1:
                return D_OUT_FORMAT.format(startTime);
            case 2:
                return (endTime == null) ? "" : D_OUT_FORMAT.format(endTime);
            case 3:
                return duration;
            case 4:
                return isTeleconference;
            default:
                return "";
        }
    }

    void finish() {
        endTime = LocalTime.now();
        calcDuration();
    }

    @Override
    public String toString() {
        return String.join(";",
                name,
                D_INP_FORMAT.format(startTime),
                D_INP_FORMAT.format(endTime),
                String.valueOf(duration),
                String.valueOf(isTeleconference)
        );
    }

    void calcDuration() {
        LocalTime effectiveEndTime = (endTime == null) ? LocalTime.now() : endTime;
        duration = startTime.until(effectiveEndTime, ChronoUnit.MINUTES);
    }

    boolean isTeleconference() {
        return isTeleconference;
    }

    void setTeleconference(boolean teleconference) {
        isTeleconference = teleconference;
    }
}
