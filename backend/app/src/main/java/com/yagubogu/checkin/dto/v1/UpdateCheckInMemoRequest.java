package com.yagubogu.checkin.dto.v1;

import jakarta.validation.constraints.Size;

public record UpdateCheckInMemoRequest(
        @Size(max = 500) String memo
) {
}
