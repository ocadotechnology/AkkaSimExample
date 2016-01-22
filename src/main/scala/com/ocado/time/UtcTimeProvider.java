package com.ocado.time;

public class UtcTimeProvider implements TimeProvider {
    @Override
    public double getTime() {
        return System.currentTimeMillis();
    }
}
