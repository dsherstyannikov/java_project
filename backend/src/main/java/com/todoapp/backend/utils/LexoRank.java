package com.todoapp.backend.utils;

import java.math.BigInteger;

public class LexoRank {

    // Длина строки-ранга
    private static final int LENGTH = 8;
    // Шаг между рангами (промежуток) – выбран достаточно большим, чтобы обеспечить возможность вставок
    private static final BigInteger DEFAULT_GAP = new BigInteger("1000000");
    // Максимальное значение не используется напрямую, но может служить ориентиром
    private static final BigInteger MAX_VALUE = BigInteger.valueOf(36).pow(LENGTH);

    // Приводит число к строке фиксированной длины в системе с основанием 36
    private static String formatRank(BigInteger rank) {
        String s = rank.toString(36);
        while (s.length() < LENGTH) {
            s = "0" + s;
        }
        return s;
    }

    // Возвращает первый ранг при отсутствии элементов
    public static String initialRank() {
        return formatRank(DEFAULT_GAP);
    }

    // Рассчитывает ранг, который идёт после указанного
    public static String calculateAfter(String prevRank) {
        BigInteger prev = new BigInteger(prevRank, 36);
        BigInteger newRank = prev.add(DEFAULT_GAP);
        return formatRank(newRank);
    }

    // Рассчитывает ранг, который идёт перед указанным
    public static String calculateBefore(String nextRank) {
        BigInteger next = new BigInteger(nextRank, 36);
        BigInteger newRank = next.subtract(DEFAULT_GAP);
        if (newRank.compareTo(BigInteger.ZERO) < 0) {
            newRank = BigInteger.ZERO;
        }
        return formatRank(newRank);
    }

    // Рассчитывает ранг между двумя значениями, возвращает null, если промежуток слишком мал
    public static String calculateBetween(String prevRank, String nextRank) {
        BigInteger prev = new BigInteger(prevRank, 36);
        BigInteger next = new BigInteger(nextRank, 36);
        BigInteger mid = prev.add(next).divide(BigInteger.valueOf(2));
        // Если промежуток отсутствует – вернуть null для инициирования перераспределения
        if (mid.equals(prev) || mid.equals(next)) {
            return null;
        }
        return formatRank(mid);
    }
}
