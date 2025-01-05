package com.example;

import org.apache.commons.cli.*;

public class Config {
    private final String host;
    private final int port;
    private final int dataSize;
    private final int connections;
    private final int slowConnections;
    private final int keysCount;
    private final boolean skipPopulation;
    private final int recvChunkSizeMin;
    private final int recvChunkSizeMax;
    private final double recvSleepTime;
    private final int hashFields;
    private final int hashFieldSize;

    public Config(String[] args) {
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            this.host = cmd.getOptionValue("host");
            this.port = Integer.parseInt(cmd.getOptionValue("port"));
            this.dataSize = Integer.parseInt(cmd.getOptionValue("data-size", "1024"));
            this.connections = Integer.parseInt(cmd.getOptionValue("connections", "10"));
            this.slowConnections = Integer.parseInt(cmd.getOptionValue("slow-connections", "0"));
            this.keysCount = Integer.parseInt(cmd.getOptionValue("keys-count"));
            this.skipPopulation = cmd.hasOption("skip-population");
            this.recvChunkSizeMin = Integer.parseInt(cmd.getOptionValue("recv-chunk-size-min", "1"));
            this.recvChunkSizeMax = Integer.parseInt(cmd.getOptionValue("recv-chunk-size-max", "1"));
            this.recvSleepTime = Double.parseDouble(cmd.getOptionValue("recv-sleep-time", "1.0"));
            this.hashFields = Integer.parseInt(cmd.getOptionValue("hash-fields", "1000000"));
            this.hashFieldSize = Integer.parseInt(cmd.getOptionValue("hash-field-size", "100"));
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse command line arguments", e);
        }
    }

    private Options createOptions() {
        Options options = new Options();
        options.addRequiredOption("h", "host", true, "Redis host");
        options.addRequiredOption("p", "port", true, "Redis port");
        options.addOption("d", "data-size", true, "Size of data in bytes");
        options.addOption("c", "connections", true, "Number of parallel connections");
        options.addOption("s", "slow-connections", true, "Number of slow connections");
        options.addRequiredOption("k", "keys-count", true, "Number of keys to populate");
        options.addOption("sp", "skip-population", false, "Skip the population stage");
        options.addOption(null, "recv-chunk-size-min", true, "Minimum chunk size for socket recv");
        options.addOption(null, "recv-chunk-size-max", true, "Maximum chunk size for socket recv");
        options.addOption(null, "recv-sleep-time", true, "Sleep time between socket recv operations");
        options.addOption(null, "hash-fields", true, "Number of fields in the large hash");
        options.addOption(null, "hash-field-size", true, "Size of each field value in bytes");
        return options;
    }

    // Getters
    public String getHost() { return host; }
    public int getPort() { return port; }
    public int getDataSize() { return dataSize; }
    public int getConnections() { return connections; }
    public int getSlowConnections() { return slowConnections; }
    public int getKeysCount() { return keysCount; }
    public boolean isSkipPopulation() { return skipPopulation; }
    public int getRecvChunkSizeMin() { return recvChunkSizeMin; }
    public int getRecvChunkSizeMax() { return recvChunkSizeMax; }
    public double getRecvSleepTime() { return recvSleepTime; }
    public int getHashFields() { return hashFields; }
    public int getHashFieldSize() { return hashFieldSize; }
}
