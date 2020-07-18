package com.korimart.f12;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class SandBox {
    @Test
    public void hehe() {
        CompletableFuture<Object> future = CompletableFuture.runAsync(() -> {
            System.out.println("nothing1");
        }).thenCompose(ignored -> {
                System.out.println("nothing2");
                return CompletableFuture.completedFuture(null);
        }).thenCompose(ignored -> {
                if (true)
                    throw new RuntimeException();
                System.out.println("nothing3");
                return CompletableFuture.completedFuture(null);
            }).whenComplete((a, b) -> {
                System.out.println("nothing4");
        });

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
