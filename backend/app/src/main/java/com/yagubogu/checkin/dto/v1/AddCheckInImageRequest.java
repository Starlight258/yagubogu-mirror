package com.yagubogu.checkin.dto.v1;

import jakarta.validation.constraints.NotBlank;

public record AddCheckInImageRequest(
        @NotBlank String imageKey
) {
}
