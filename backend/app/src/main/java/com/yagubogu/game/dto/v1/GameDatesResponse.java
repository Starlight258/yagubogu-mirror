package com.yagubogu.game.dto.v1;

import java.time.LocalDate;
import java.util.List;

public record GameDatesResponse(
        List<LocalDate> dates
) {
}
