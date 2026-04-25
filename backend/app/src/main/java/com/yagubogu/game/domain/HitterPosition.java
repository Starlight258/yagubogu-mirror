package com.yagubogu.game.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum HitterPosition {
    FIRST_BASE("一", "1루수"),
    SECOND_BASE("二", "2루수"),
    THIRD_BASE("三", "3루수"),
    SHORTSTOP("유", "유격수"),
    LEFT_FIELD("좌", "좌익수"),
    RIGHT_FIELD("우", "우익수"),
    CENTER_FIELD("중", "중견수"),
    CATCHER("포", "포수"),
    DESIGNATED_HITTER("지", "지명타자"),
    PINCH_RUNNER("주", "대주자"),
    PINCH_HITTER("타", "대타");

    private final String code;
    private final String displayName;

    HitterPosition(final String code, final String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    private static final Map<String, String> CODE_TO_DISPLAY = Arrays.stream(values())
            .collect(Collectors.toMap(p -> p.code, p -> p.displayName));

    public static String normalize(final String raw) {
        if (CODE_TO_DISPLAY.containsKey(raw)) {
            return CODE_TO_DISPLAY.get(raw);
        }
        if (raw.length() == 2) {
            return normalize(raw.substring(0, 1));
        }
        return raw;
    }
}
