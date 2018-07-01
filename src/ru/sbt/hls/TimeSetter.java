package ru.sbt.hls;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.util.function.Consumer;

public class TimeSetter {
    private JSpinner spnHours;
    private JSpinner spnMinutes;
    private JPanel rootPanel;
    private JButton btnOk;

    private final Consumer<LocalTime> timeSetter;

    private JDialog frame;

    TimeSetter(Consumer<LocalTime> cons, int hours, int minutes) {
        spnHours.setModel(new SpinnerNumberModel(hours, 0, 23, 1));
        spnMinutes.setModel(new SpinnerNumberModel(minutes, 0, 59, 1));

        timeSetter = cons;

        btnOk.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                timeSetter.accept(LocalTime.parse(String.join(":",
                        spnHours.getValue().toString(),
                        spnMinutes.getValue().toString()
                        ),
                        Task.D_INP_FORMAT
                ));
                frame.dispose();
            }
        });
    }

    void show(Point atPoint) {
        System.out.println(atPoint);
        frame = new JDialog((Frame) null, "Set Time");
        frame.setModal(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(rootPanel);
        frame.setResizable(false);
        frame.setLocation(atPoint);
        frame.pack();
        frame.setVisible(true);
    }
}
