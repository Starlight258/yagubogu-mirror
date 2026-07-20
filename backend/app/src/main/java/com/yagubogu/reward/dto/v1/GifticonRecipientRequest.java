package com.yagubogu.reward.dto.v1;

import jakarta.validation.constraints.NotBlank;

public record GifticonRecipientRequest(
        @NotBlank String phoneNumber
) {
}
