import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Task {
    private static final DateTimeFormatter FRMT = DateTimeFormatter.ofPattern("HH:mm");

    private String name;
    private LocalTime startTime;
    private LocalTime endTime;

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    private long duration;

    Task(String name) {
        this.name = name;
        startTime = LocalTime.now();
    }

    Task(String serialized, String delimiter) {
        String[] parts = serialized.split(delimiter);
        name = parts[0];
        startTime = LocalTime.parse(parts[1], FRMT);
        endTime = LocalTime.parse(parts[2], FRMT);
        duration = Long.parseLong(parts[3]);
    }

    public String get(int idx) {
        switch (idx) {
            case 0:
                return name;
            case 1:
                return FRMT.format(startTime);
            case 2:
                return (endTime == null) ? "" : FRMT.format(endTime);
            case 3:
                return String.valueOf(duration);
            default:
                return "";
        }
    }

    public void finish() {
        endTime = LocalTime.now();
        calcDuration();
    }

    @Override
    public String toString() {
        return String.join(";",
                name,
                FRMT.format(startTime),
                FRMT.format(endTime),
                String.valueOf(duration)
        );
    }

    public void calcDuration() {
        LocalTime effectiveEndTime = (endTime == null) ? LocalTime.now() : endTime;
        duration = startTime.until(effectiveEndTime, ChronoUnit.MINUTES);
    }
}
