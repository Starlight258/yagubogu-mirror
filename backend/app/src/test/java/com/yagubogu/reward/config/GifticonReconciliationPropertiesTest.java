package com.yagubogu.reward.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GifticonReconciliationPropertiesTest {

    @DisplayName("최초 대사 대기 시간이 누락되면 설정 검증에 실패한다")
    @Test
    void rejectMissingInitialDelay() {
        GifticonReconciliationProperties properties =
                properties(null, 20, Duration.ofHours(6));

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Set<ConstraintViolation<GifticonReconciliationProperties>> violations =
                    factory.getValidator().validate(properties);

            assertThat(violations)
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactly("initialDelay");
        }
    }

    @DisplayName("최초 대사 대기 시간은 양수여야 한다")
    @Test
    void rejectNonPositiveInitialDelay() {
        GifticonReconciliationProperties properties =
                properties(Duration.ZERO, 20, Duration.ofHours(6));

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Set<ConstraintViolation<GifticonReconciliationProperties>> violations =
                    factory.getValidator().validate(properties);

            assertThat(violations)
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactly("initialDelayPositive");
        }
    }

    @DisplayName("대사 배치 크기는 양수여야 한다")
    @Test
    void rejectNonPositiveBatchSize() {
        GifticonReconciliationProperties properties =
                properties(Duration.ofMinutes(1), 0, Duration.ofHours(6));

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Set<ConstraintViolation<GifticonReconciliationProperties>> violations =
                    factory.getValidator().validate(properties);

            assertThat(violations)
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactly("batchSize");
        }
    }

    @DisplayName("최대 대사 대기 시간은 양수여야 한다")
    @Test
    void rejectNonPositiveMaxBackoff() {
        GifticonReconciliationProperties properties =
                properties(Duration.ofMinutes(1), 20, Duration.ZERO);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Set<ConstraintViolation<GifticonReconciliationProperties>> violations =
                    factory.getValidator().validate(properties);

            assertThat(violations)
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactly("maxBackoffPositive");
        }
    }

    private GifticonReconciliationProperties properties(
            final Duration initialDelay,
            final int batchSize,
            final Duration maxBackoff
    ) {
        return new GifticonReconciliationProperties(initialDelay, batchSize, maxBackoff);
    }
}
