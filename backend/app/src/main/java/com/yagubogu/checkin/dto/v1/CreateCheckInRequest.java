package com.yagubogu.checkin.dto.v1;

import jakarta.validation.constraints.Size;

public record CreateCheckInRequest(
        long gameId,
        @Size(max = 500) String memo,
        String imageKey
) {
}