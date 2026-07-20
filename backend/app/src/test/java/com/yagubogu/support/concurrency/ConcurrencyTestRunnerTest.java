package com.yagubogu.support.concurrency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConcurrencyTestRunnerTest {

    @DisplayName("워커 스레드에서 발생한 예외를 테스트 스레드로 전파한다")
    @Test
    void runConcurrentlyPerItem_propagatesWorkerException() {
        // given
        IllegalStateException exception = new IllegalStateException("worker failed");

        // when & then
        assertThatThrownBy(() -> ConcurrencyTestRunner.runConcurrentlyPerItem(
                List.of(1),
                ignored -> {
                    throw exception;
                },
                Duration.ofSeconds(1)
        )).isSameAs(exception);
    }

    @DisplayName("작업이 제한 시간 안에 끝나지 않으면 워커를 중단하고 실패한다")
    @Test
    void runConcurrentlyPerItem_cancelsWorkerOnTimeout() throws InterruptedException {
        // given
        CountDownLatch neverReleased = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);

        // when & then
        assertThatThrownBy(() -> ConcurrencyTestRunner.runConcurrentlyPerItem(
                List.of(1),
                ignored -> {
                    try {
                        neverReleased.await();
                    } catch (InterruptedException e) {
                        interrupted.countDown();
                        Thread.currentThread().interrupt();
                    }
                },
                Duration.ofMillis(100)
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("동시성 테스트 작업이 제한 시간 내에 끝나지 않았습니다.");
        assertThat(interrupted.await(1, TimeUnit.SECONDS)).isTrue();
    }
}
