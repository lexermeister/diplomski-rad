package config;

import org.openjdk.jmh.annotations.Mode;

import java.util.concurrent.TimeUnit;

public class Configuration {

    public static final int THREADS = Math.min(Runtime.getRuntime().availableProcessors(), 4);;
    public static final int FORKS = 1;
    public static final int WARMUP_ITERATIONS = 5;
    public static final int MEASUREMENT_ITERATIONS = 10;
    public static final Long VALUE = 123456789L;
    public static final int ONE_MLN = 1000000;
    public static final Mode MODE = Mode.Throughput;
    public static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
}
