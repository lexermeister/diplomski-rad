package linkedblockingqueue;


import config.Configuration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class LinkedBlockingQueueBenchmarks {


    @Param({"8192", "32768", "65536"})
    private int queueSize;

    private ExecutorService executor;
    private LinkedBlockingQueue<Long> queue;


    private final Runnable addTask = () -> {
        for(int i = 0; i < Configuration.ONE_MLN; i++) {
            while(!queue.offer(Configuration.VALUE)) {
                ;
            }
        }
    };


    private final Runnable addWaitingTask = () -> {
        try {
            for(int i = 0; i < Configuration.ONE_MLN; i++) {
                while(!queue.offer(Configuration.VALUE, 1L, TimeUnit.MICROSECONDS)){
                    ;
                }
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException("Test failed due to interrupt.", e);
        }
    };


    @Setup
    public void setup() {
        executor = Executors.newFixedThreadPool(Configuration.THREADS);
        queue = new LinkedBlockingQueue<Long>();
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

    @Benchmark
    public void sendOneMlnWaiting() throws InterruptedException {
        executor.execute(addWaitingTask);
        try {
            for (int i = 0; i < Configuration.ONE_MLN; i++) {
                while (queue.poll(1L, TimeUnit.MICROSECONDS) != Configuration.VALUE) {
                    ;
                }
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException("Test failed due to interrupt.", e);
        }
    }


    public static void runBenchmarks(int numOfThreads) throws Exception {
        final String resultFileName = "LinkedBlockingQueue_threads_x" + numOfThreads + ".csv";


        Options opts = new OptionsBuilder()
                .include(".*" + LinkedBlockingQueueBenchmarks.class.getSimpleName() + ".*")
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
