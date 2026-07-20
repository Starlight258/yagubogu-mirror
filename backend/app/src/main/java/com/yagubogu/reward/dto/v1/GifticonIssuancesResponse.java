package com.yagubogu.reward.dto.v1;

import com.yagubogu.reward.domain.GifticonIssuance;
import java.util.List;

public record GifticonIssuancesResponse(
        List<GifticonIssuanceResponse> gifticons
) {

    public static GifticonIssuancesResponse from(final List<GifticonIssuance> issuances) {
        return new GifticonIssuancesResponse(
                issuances.stream()
                        .map(GifticonIssuanceResponse::from)
                        .toList()
        );
    }
}
