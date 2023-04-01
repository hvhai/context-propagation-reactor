package com.codehunter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Hello world!
 *
 */
public class ThreadLocalApp
{
    static final ThreadLocal<Long> CORRELATION_ID = new ThreadLocal<>();
    public static void main( String[] args )
    {
        ThreadLocalApp app = new ThreadLocalApp();
        // 1. Thread local
        app.handleRequest();
        // 2. Async
//        app.handleRequestAsync();
//        app.handleRequestAsync().join();
        // 3. Wrap async
//        app.handleRequestByWrapper();
//        app.handleRequestByWrapper().join();
        // 4. Wrap runnable
        Executor executor = new WrappedExecutor(ForkJoinPool.commonPool());
        handleRequest(executor);


    }
    static long correlationId() {
        return Math.abs(ThreadLocalRandom.current().nextLong());
    }
    static void initRequest() {
        CORRELATION_ID.set(correlationId());
    }

    static void log(String message) {
        String threadName = Thread.currentThread().getName();
        System.out.printf("[%10s][%20s] %s%n",
                threadName, CORRELATION_ID.get(), message);
    }

    void handleRequest() {
        initRequest();
        addProduct("product-1");
        notifyShop("product-1");
    }

    static void addProduct(String productName) {
        log("Adding product: " + productName);
        // ...
    }

    static void notifyShop(String productName) {
        log("Notifying shop about: " + productName);
        // ...
    }

    CompletableFuture<Void> handleRequestAsync() {
        return CompletableFuture
                .runAsync(() -> addProduct("product-1"))
                .thenRunAsync(()-> notifyShop("product-1"));
    }

    CompletableFuture<Void> handleRequestByWrapper() {
        return CompletableFuture
                .runAsync(new WrappedRunnable(
                        () -> addProduct("test-product")))
                .thenRunAsync(new WrappedRunnable(
                        () -> notifyShop("test-product")));
    }


    static CompletableFuture<Void> handleRequest(Executor executor) {
        return CompletableFuture
                .runAsync(() -> addProduct("test-product"), executor)
                .thenRunAsync(() -> notifyShop("test-product"), executor);
    }
     static class WrappedRunnable implements Runnable{
        private final Long correlationId;
        private final Runnable wrapped;

        public WrappedRunnable(Runnable wrapped) {
            this.correlationId = CORRELATION_ID.get();
            this.wrapped = wrapped;
        }

        @Override
        public void run() {
            Long old = CORRELATION_ID.get();
            CORRELATION_ID.set(this.correlationId);
            try {
                wrapped.run();
            } finally {
                CORRELATION_ID.set(old);
            }

        }
    }

     static class WrappedExecutor implements Executor {

        private final Executor actual;

        WrappedExecutor(Executor actual) {
            this.actual = actual;
        }

        @Override
        public void execute(Runnable command) {
            actual.execute(new WrappedRunnable(command));
        }
    }
}
