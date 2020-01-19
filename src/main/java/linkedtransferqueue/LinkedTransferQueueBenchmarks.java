package linkedtransferqueue;

import config.Configuration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.*;

@State(Scope.Thread)
public class LinkedTransferQueueBenchmarks {

    private ExecutorService executor;
    private TransferQueue<Long> queue;

    private final Runnable addTask = () -> {

        for(int i = 0; i < Configuration.ONE_MLN; i++) {
            if(queue.hasWaitingConsumer()) {
                queue.tryTransfer(Configuration.VALUE);
            } else {
                while(!queue.offer(Configuration.VALUE)){ ;
                }
            }

        }

    };

    private final Runnable addWaitingTask = () -> {
        try {
            for (int i = 0; i < Configuration.ONE_MLN; i++) {
                if(queue.hasWaitingConsumer()) {
                    queue.tryTransfer(Configuration.VALUE, 1L, TimeUnit.MICROSECONDS);
                } else {
                    while (!queue.offer(Configuration.VALUE, 1L, TimeUnit.MICROSECONDS)) {
                        ;
                    }
                }
            }
        }catch (InterruptedException e) {
            throw new RuntimeException("Test failed due to interrupt.", e);
        }

    };

    @Setup
    public void setup() {
        executor = Executors.newFixedThreadPool(Configuration.THREADS);
        queue = new LinkedTransferQueue<Long>();

    }

    @Benchmark
    public void sendOneMln() {
        executor.execute(addTask);
        for(int i = 0; i < Configuration.ONE_MLN; i++) {
            while (queue.poll() != Configuration.VALUE){ ;
            }
        }
    }


    @TearDown
    public void tearDown() {
        executor.shutdown();
    }

    public static void runBenchmarks(int numOfThreads) throws Exception {
        final String resultFileName = "LinkedTransferQueue_threads_x" + numOfThreads + ".csv";

        Options opts = new OptionsBuilder()
                .include(".*" +LinkedTransferQueueBenchmarks.class.getSimpleName() + ".*")
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
