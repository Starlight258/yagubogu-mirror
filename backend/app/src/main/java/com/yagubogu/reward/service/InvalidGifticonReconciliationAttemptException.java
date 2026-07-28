package com.yagubogu.reward.service;

/**
 * 대사 시도 횟수가 백오프 계산에 사용할 수 없는 값일 때 발생한다.
 */
public class InvalidGifticonReconciliationAttemptException extends RuntimeException {

    public InvalidGifticonReconciliationAttemptException(final int attempt) {
        super("Gifticon reconciliation attempt must be positive: attempt=" + attempt);
    }
}
