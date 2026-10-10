package me.nik.superenchantments.utils;

public class MillisTest {

    private long start;

    public MillisTest() {
        this.start = System.currentTimeMillis();
    }

    public void reset() {
        this.start = System.currentTimeMillis();
    }

    public long getMillis() {
        return System.currentTimeMillis() - this.start;
    }
}