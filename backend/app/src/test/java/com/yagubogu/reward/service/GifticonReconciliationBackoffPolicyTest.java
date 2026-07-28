package com.yagubogu.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yagubogu.reward.config.GifticonReconciliationProperties;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GifticonReconciliationBackoffPolicyTest {

    @DisplayName("대사 횟수에 따라 다음 조회 대기 시간을 늘린다")
    @Test
    void increaseDelayByAttempt() {
        GifticonReconciliationBackoffPolicy policy = policy(Duration.ofHours(6));

        assertThat(policy.delayForAttempt(1)).isEqualTo(Duration.ofMinutes(1));
        assertThat(policy.delayForAttempt(2)).isEqualTo(Duration.ofMinutes(5));
        assertThat(policy.delayForAttempt(3)).isEqualTo(Duration.ofMinutes(30));
        assertThat(policy.delayForAttempt(4)).isEqualTo(Duration.ofHours(2));
        assertThat(policy.delayForAttempt(5)).isEqualTo(Duration.ofHours(6));
        assertThat(policy.delayForAttempt(10)).isEqualTo(Duration.ofHours(6));
    }

    @DisplayName("계산된 대기 시간은 설정한 최대 시간을 넘지 않는다")
    @Test
    void capDelayAtConfiguredMaximum() {
        GifticonReconciliationBackoffPolicy policy = policy(Duration.ofMinutes(10));

        assertThat(policy.delayForAttempt(3)).isEqualTo(Duration.ofMinutes(10));
    }

    @DisplayName("대사 횟수는 양수여야 한다")
    @Test
    void rejectNonPositiveAttempt() {
        GifticonReconciliationBackoffPolicy policy = policy(Duration.ofHours(6));

        assertThatThrownBy(() -> policy.delayForAttempt(0))
                .isInstanceOf(InvalidGifticonReconciliationAttemptException.class);
    }

    private GifticonReconciliationBackoffPolicy policy(final Duration maxBackoff) {
        GifticonReconciliationProperties properties = new GifticonReconciliationProperties(
                Duration.ofMinutes(1),
                20,
                maxBackoff
        );
        return new GifticonReconciliationBackoffPolicy(properties);
    }
}
