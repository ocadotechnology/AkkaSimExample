package com.ocado.time;

public class AdjustableTimeProvider implements TimeProvider {
    private double currTime;

    public AdjustableTimeProvider(double initialTime) {
        this.currTime = initialTime;
    }

    @Override
    public double getTime() {
        return this.currTime;
    }

    public void setTime(double time) {
        this.currTime = time;
    }

    public void advanceTime(double periodMs) {
        this.currTime += periodMs;
    }
}
