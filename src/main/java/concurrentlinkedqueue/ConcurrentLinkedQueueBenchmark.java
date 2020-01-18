package concurrentlinkedqueue;

import config.Configuration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class ConcurrentLinkedQueueBenchmark {

    private ExecutorService executor;
    private ConcurrentLinkedQueue<Long> queue;

    private int queueSize;

    private final Runnable addTask = () -> {
        for(int i = 0; i < Configuration.ONE_MLN; i++) {
            while(!queue.offer(Configuration.VALUE)) {
                ;
            }
        }
    };

    @Setup
    public void setup() {
        executor = Executors.newFixedThreadPool(1);
        queue = new ConcurrentLinkedQueue<Long>();
    }

    @TearDown
    public void tearDown() {
        executor.shutdown();
    }


    @Benchmark
    public void sendOneMln() {
        executor.execute(addTask);

        for(int i = 0; i < Configuration.ONE_MLN; i++) {
            while(queue.poll() != Configuration.VALUE) {
                ;
            }
        }
    }

    public static void runBenchmarks(int numOfThreads) throws Exception {
        final String resultFileName = "ConcurrentLinkedQueue_threads_x" + numOfThreads + ".csv";

        Options opts = new OptionsBuilder()
                .include(".*" + ConcurrentLinkedQueueBenchmark.class.getSimpleName() + ".*")
                .forks(Configuration.FORKS)
                .threads(numOfThreads)
                .jvmArgs("-server", "-Xms2048m", "-Xmx2048m")
                .mode(Configuration.MODE)
                .timeUnit(Configuration.TIME_UNIT)
                .warmupIterations(Configuration.WARMUP_ITERATIONS)
                .measurementIterations(Configuration.MEASUREMENT_ITERATIONS)
                .resultFormat(ResultFormatType.CSV)
                .result(resultFileName)
                .build();

        new Runner(opts).run();
    }
}
