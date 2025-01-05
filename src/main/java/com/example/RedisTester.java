package com.example;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class RedisTester {
    private final Config config;
    private final RedisClient redisClient;
    private final GenericObjectPool<StatefulRedisConnection<String, String>> pool;
    private final Random random = new Random();

    public RedisTester(Config config) {
        this.config = config;
        RedisURI redisUri = RedisURI.builder()
                .withHost(config.getHost())
                .withPort(config.getPort())
                .build();
        this.redisClient = RedisClient.create(redisUri);

        GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(config.getConnections() + config.getSlowConnections());
        poolConfig.setMaxIdle(config.getConnections());
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        this.pool = ConnectionPoolSupport.createGenericObjectPool(
                () -> redisClient.connect(),
                poolConfig
        );
    }

    public void run() {
        if (!config.isSkipPopulation()) {
            populateDb();
        }

        // Only create slow connections executor if there are slow connections configured
        if (config.getSlowConnections() > 0) {
            ExecutorService slowConnectionsExecutor = Executors.newFixedThreadPool(config.getSlowConnections());
            for (int i = 0; i < config.getSlowConnections(); i++) {
                final int clientId = i;
                slowConnectionsExecutor.submit(() -> runSlowReader(clientId));
            }
        }

        // Start regular connections
        runReaders();
    }

    private void populateDb() {
        try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
            RedisCommands<String, String> commands = connection.sync();
            commands.flushdb();
            System.out.println("Database flushed.");

            String value = DataGenerator.generateData(config.getDataSize());

            // Populate regular keys
            for (int i = 0; i < config.getKeysCount(); i++) {
                commands.set("key-" + i, value);
            }
            System.out.printf("Populated DB with %d keys.%n", config.getKeysCount());

            // Populate large hash
            String hashKey = "large-hash";
            ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, config.getConnections()));
            int fieldsPerThread = config.getHashFields() / config.getConnections();

            List<Runnable> tasks = new ArrayList<>();
            for (int i = 0; i < config.getConnections(); i++) {
                final int startField = i * fieldsPerThread;
                final int endField = (i == config.getConnections() - 1) ?
                    config.getHashFields() : startField + fieldsPerThread;

                tasks.add(() -> {
                    try (StatefulRedisConnection<String, String> workerConnection = pool.borrowObject()) {
                        RedisCommands<String, String> workerCommands = workerConnection.sync();
                        for (int j = startField; j < endField; j++) {
                            String field = "field-" + j;
                            String fieldValue = DataGenerator.generateData(config.getHashFieldSize());
                            workerCommands.hset(hashKey, field, fieldValue);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            tasks.forEach(executor::submit);
            executor.shutdown();
            while (!executor.isTerminated()) {
                Thread.sleep(100);
            }

            double sizeMB = (config.getHashFields() * config.getHashFieldSize()) / (1024.0 * 1024.0);
            System.out.printf("Populated DB with large hash: %s, containing %d fields (~%.2f MB)%n",
                    hashKey, config.getHashFields(), sizeMB);
        } catch (Exception e) {
            throw new RuntimeException("Failed to populate database", e);
        }
    }

    private void runSlowReader(int clientId) {
        try {
            Socket socket = new Socket(config.getHost(), config.getPort());
            String command = "HGETALL large-hash\r\n";
            socket.getOutputStream().write(command.getBytes(StandardCharsets.UTF_8));

            int chunkSize = config.getRecvChunkSizeMin() +
                (clientId * ((config.getRecvChunkSizeMax() - config.getRecvChunkSizeMin()) /
                Math.max(1, config.getSlowConnections())));

            byte[] buffer = new byte[chunkSize];
            while (socket.getInputStream().read(buffer) != -1) {
                Thread.sleep((long)(config.getRecvSleepTime() * 1000));
            }
        } catch (Exception e) {
            System.err.printf("Slow Client %d encountered an error: %s%n", clientId, e.getMessage());
        }
    }

    private void runReaders() {
        AtomicInteger ops = new AtomicInteger(0);
        // Ensure at least 1 thread in the pool
        ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, config.getConnections()));

        // Start reader threads
        for (int i = 0; i < config.getConnections(); i++) {
            executor.submit(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        try (StatefulRedisConnection<String, String> connection = pool.borrowObject()) {
                            RedisCommands<String, String> commands = connection.sync();
                            String key = "key-" + random.nextInt(config.getKeysCount());
                            commands.get(key);
                            ops.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Reader thread encountered an error: " + e.getMessage());
                }
            });
        }

        // Monitor and report throughput
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Thread.sleep(1000);
                int currentOps = ops.getAndSet(0);
                System.out.printf("Throughput: %d ops/sec%n", currentOps);
            }
        } catch (InterruptedException e) {
            System.out.println("Shutting down...");
        } finally {
            executor.shutdownNow();
        }
    }

    public void shutdown() {
        pool.close();
        redisClient.shutdown();
    }
}
