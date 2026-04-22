package com.smartcampus;

import java.net.URI;
import java.util.concurrent.CountDownLatch;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public final class Main {

    private static final URI BASE_URI = URI.create("http://0.0.0.0:8080/api/v1/");

    private Main() {
    }

    public static void main(String[] args) throws InterruptedException {
        ResourceConfig config = ResourceConfig.forApplication(new SmartCampusApplication());
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, config);
        CountDownLatch shutdownLatch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdownNow();
            shutdownLatch.countDown();
        }));

        System.out.println("Smart Campus API running at http://localhost:8080/api/v1");
        System.out.println("Keep this window open. Press Ctrl+C to stop the server.");
        shutdownLatch.await();
    }
}
