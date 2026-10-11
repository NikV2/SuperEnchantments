package me.nik.superenchantments.utils;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomUtils {

    private RandomUtils(){}

    public static boolean chance(double percentage) {
        if (!Double.isFinite(percentage) || percentage <= 0.0D) return false;

        if (percentage >= 100.0D) return true;

        return ThreadLocalRandom.current().nextDouble(100.0D) < percentage;
    }
}