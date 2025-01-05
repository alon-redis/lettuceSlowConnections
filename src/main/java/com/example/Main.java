package com.example;

public class Main {
    public static void main(String[] args) {
        try {
            // Print startup message
            System.out.println("Starting Redis Parallel Tester...");

            // Create and print configuration
            Config config = new Config(args);
            System.out.println("Configuration:");
            System.out.println("  Host: " + config.getHost());
            System.out.println("  Port: " + config.getPort());
            System.out.println("  Keys Count: " + config.getKeysCount());
            System.out.println("  Connections: " + config.getConnections());
            System.out.println("  Slow Connections: " + config.getSlowConnections());

            // Create tester instance
            System.out.println("Initializing Redis connection...");
            RedisTester tester = new RedisTester(config);

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down...");
                try {
                    tester.shutdown();
                } catch (Exception e) {
                    System.err.println("Error during shutdown: " + e);
                    e.printStackTrace();
                }
            }));

            // Run the tester
            System.out.println("Starting test execution...");
            tester.run();

        } catch (Exception e) {
            System.err.println("Error running Redis tester: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
