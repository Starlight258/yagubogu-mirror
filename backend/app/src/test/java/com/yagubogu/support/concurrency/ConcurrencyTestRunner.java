package com.yagubogu.support.concurrency;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public final class ConcurrencyTestRunner {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    private ConcurrencyTestRunner() {
    }

    public static void runConcurrently(final int threadCount, final Runnable task) throws InterruptedException {
        List<Integer> placeholders = IntStream.range(0, threadCount).boxed().toList();
        runConcurrentlyPerItem(placeholders, ignored -> task.run());
    }

    /**
     * 모든 스레드를 대기시켰다가 동시에 풀어서 진짜 동시 요청을 재현한다.
     */
    public static <T> void runConcurrentlyPerItem(final List<T> items, final Consumer<T> task)
            throws InterruptedException {
        runConcurrentlyPerItem(items, task, DEFAULT_TIMEOUT);
    }

    static <T> void runConcurrentlyPerItem(
            final List<T> items,
            final Consumer<T> task,
            final Duration timeout
    ) throws InterruptedException {
        int threadCount = items.size();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Future<?>> futures = new ArrayList<>(threadCount);

        try {
            for (T item : items) {
                futures.add(executorService.submit(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await();
                        task.accept(item);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                }));
            }

            readyLatch.await();
            startLatch.countDown();

            if (!doneLatch.await(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("동시성 테스트 작업이 제한 시간 내에 끝나지 않았습니다.");
            }
            propagateWorkerExceptions(futures);
        } finally {
            executorService.shutdownNow();
        }
    }

    private static void propagateWorkerExceptions(final List<Future<?>> futures) throws InterruptedException {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                rethrowWorkerException(e.getCause());
            }
        }
    }

    private static void rethrowWorkerException(final Throwable cause) {
        if (cause instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (cause instanceof Error error) {
            throw error;
        }
        throw new IllegalStateException("동시성 테스트 워커 실행에 실패했습니다.", cause);
    }
}
