package com.yagubogu.support.concurrency;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public final class ConcurrencyTestRunner {

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
        int threadCount = items.size();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (T item : items) {
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    task.accept(item);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
    }
}
