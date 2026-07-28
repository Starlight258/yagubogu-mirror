package com.yagubogu.reward.service;

import com.yagubogu.reward.config.GifticonReconciliationProperties;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 기프티콘 주문 조회 횟수에 따라 다음 대사까지 기다릴 시간을 계산한다.
 */
@RequiredArgsConstructor
@Component
public class GifticonReconciliationBackoffPolicy {

    private static final List<Duration> RETRY_DELAYS = List.of(
            Duration.ofMinutes(1),
            Duration.ofMinutes(5),
            Duration.ofMinutes(30),
            Duration.ofHours(2)
    );

    private final GifticonReconciliationProperties properties;

    /**
     * 첫 시도부터 순서대로 대기 시간을 늘리고 설정된 최대 시간을 넘지 않게 제한한다.
     */
    public Duration delayForAttempt(final int attempt) {
        if (attempt <= 0) {
            throw new InvalidGifticonReconciliationAttemptException(attempt);
        }
        Duration delay = attempt <= RETRY_DELAYS.size()
                ? RETRY_DELAYS.get(attempt - 1)
                : properties.maxBackoff();
        if (delay.compareTo(properties.maxBackoff()) > 0) {
            return properties.maxBackoff();
        }
        return delay;
    }
}
