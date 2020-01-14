package concurrentstack;

import config.Configuration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.*;

@State(Scope.Thread)
public class ConcurrentLinkedDequeBenchmarks {

    private ExecutorService executor;
    private ConcurrentLinkedDeque<Long> deque;

    private final Runnable addTask = () -> {
        for(int i = 0; i < Configuration.ONE_MLN; i++) {
            while(!deque.offer(Configuration.VALUE)) {
                ;
            }
        }
    };

    @Setup
    public void setup() {
        executor = Executors.newFixedThreadPool(1);
        deque = new ConcurrentLinkedDeque<>();
    }

    @TearDown
    public void tearDown() {
        executor.shutdown();
    }

    @Benchmark
    public void sendOneMln() {
        executor.execute(addTask);

        for(int i = 0; i < Configuration.ONE_MLN; i++) {
            while(deque.poll() != Configuration.VALUE){
                ;
            }
        }
    }

    public static void runBenchmark(int numOfThreads) throws Exception {
        final String resultFileName = "ConcurrentLinkedDeque_threads_x" + numOfThreads + ".csv";
//        final String resultFileName = "ConcurrentLinkedDeque_results.csv";

        Options opts = new OptionsBuilder()
                .include(".*" + ConcurrentLinkedDequeBenchmarks.class.getSimpleName() + ".*")
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
