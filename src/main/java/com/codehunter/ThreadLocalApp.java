package com.codehunter;

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
        app.handleRequest();
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

    void addProduct(String productName) {
        log("Adding product: " + productName);
        // ...
    }

    void notifyShop(String productName) {
        log("Notifying shop about: " + productName);
        // ...
    }
}
