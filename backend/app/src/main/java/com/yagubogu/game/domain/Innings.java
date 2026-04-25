package com.yagubogu.game.domain;

public class Innings {

    private static final String ONE_THIRD = "1/3";
    private static final String TWO_THIRDS = "2/3";
    private static final double ONE_THIRD_VALUE = 0.1;
    private static final double TWO_THIRDS_VALUE = 0.2;

    private Innings() {
    }

    public static double parse(final String raw) {
        String[] parts = raw.split(" ");
        if (parts.length == 1) {
            return parseSinglePart(parts[0]);
        }
        return Double.parseDouble(parts[0]) + parseFraction(parts[1]);
    }

    private static double parseSinglePart(final String part) {
        if (part.contains("/")) {
            return parseFraction(part);
        }
        return Double.parseDouble(part);
    }

    private static double parseFraction(final String fraction) {
        if (ONE_THIRD.equals(fraction)) {
            return ONE_THIRD_VALUE;
        }
        if (TWO_THIRDS.equals(fraction)) {
            return TWO_THIRDS_VALUE;
        }
        throw new IllegalArgumentException("Unknown innings fraction: " + fraction);
    }
}
