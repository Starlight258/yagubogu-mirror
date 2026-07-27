package com.yagubogu.reward.dto.v1;

import com.yagubogu.reward.domain.GifticonIssuance;
import com.yagubogu.reward.domain.GifticonIssuanceStatus;
import java.time.LocalDate;

public record GifticonIssuanceResponse(
        Long gifticonIssuanceId,
        LocalDate weekStart,
        GifticonIssuanceStatus status,
        String recipientPhoneNumber
) {

    public static GifticonIssuanceResponse from(final GifticonIssuance issuance) {
        String maskedPhoneNumber = issuance.getRecipientPhoneNumber() == null
                ? null
                : issuance.getRecipientPhoneNumber().masked();
        return new GifticonIssuanceResponse(
                issuance.getId(),
                issuance.getWeeklyTopScore().getWeekStart(),
                issuance.getStatus(),
                maskedPhoneNumber
        );
    }
}
