package ru.sbt.hls;

import java.time.LocalTime;

public class TestClass {
    public static void main(String[] args) {
        LocalTime.parse("09:53", Task.D_INP_FORMAT);
        LocalTime.parse("9:53", Task.D_INP_FORMAT);
        LocalTime.parse("09:", Task.D_INP_FORMAT);
    }
}
