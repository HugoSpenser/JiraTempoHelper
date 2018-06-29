import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Task {
    private static final DateTimeFormatter FRMT = DateTimeFormatter.ofPattern("HH:mm");

    public String getName() {
        return name;
    }

    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isConf;
    private long duration;

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }


    Task(String name, boolean isConf) {
        this.name = name;
        this.isConf = isConf;
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

    public boolean isRegular() {
        return !isConf;
    }
}
