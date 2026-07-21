package com.yagubogu.reward.client;

public record GiftOrderRequest(
        String externalOrderId,
        String recipientPhoneNumber
) {
}
