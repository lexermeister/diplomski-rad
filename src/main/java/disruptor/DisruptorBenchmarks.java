package disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import config.Configuration;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@State(Scope.Thread)
public class DisruptorBenchmarks {

    private EventHandler<LongEvent> handler, handler2;
    private Disruptor<LongEvent> disruptor;
    private RingBuffer<LongEvent> ringBuffer;
    private AtomicInteger eventCount;

//    @Param({"2048", "8192", "32768"})
    @Param({"8192", "32768", "65536", "131072"})
    String ringBufferSize;

    @Param({"SINGLE",})
    String producerType;

    @Param({"com.lmax.disruptor.LiteBlockingWaitStrategy", "com.lmax.disruptor.BusySpinWaitStrategy",
            "com.lmax.disruptor.BlockingWaitStrategy", "com.lmax.disruptor.SleepingWaitStrategy", "com.lmax.disruptor.YieldingWaitStrategy"})
    private String waitStrategy;

    private ExecutorService executor;

    private static long result = 0;
    private static int i = 0;

    @Setup
    public void setup() throws Exception {


        disruptor = new Disruptor<LongEvent>(LongEvent.EVENT_FACTORY, Integer.valueOf(ringBufferSize),
                DaemonThreadFactory.INSTANCE, ProducerType.valueOf(producerType),
                createWaitStrategyInstance(waitStrategy));
        eventCount = new AtomicInteger();

        handler = (longEvent, seq, endOfBatch) -> {
            if(Configuration.VALUE == longEvent.getValue()) {
                eventCount.incrementAndGet();
            } else {
                throw new IllegalStateException("Expected: " + Configuration.VALUE + ". Actual: " + longEvent.getValue());
            }
        };

        handler2 = (longEvent, seq, endOfBatch) -> {
            if(Configuration.VALUE == longEvent.getValue()) {
                eventCount.incrementAndGet();
            } else {
                throw new IllegalStateException("Expected: " + Configuration.VALUE + ". Actual: " + longEvent.getValue());
            }
        };


        disruptor.handleEventsWith(handler);

        ringBuffer = disruptor.start();
    }

    @TearDown
    public void tearDown() {
        disruptor.shutdown();
    }


    private WaitStrategy createWaitStrategyInstance(String className) throws Exception {
        Class<WaitStrategy> waitStrategyClass = (Class<WaitStrategy>) Class.forName(className);
        return waitStrategyClass.newInstance();
    }

    @Benchmark
    public void processOneMlnEvents() {
//        long startTime = System.currentTimeMillis();
        for(int i = 0; i < Configuration.ONE_MLN; i++) {
            long seq = ringBuffer.next();
            LongEvent longEvent = ringBuffer.get(seq);
            longEvent.setValue(Configuration.VALUE);
            ringBuffer.publish(seq);

 //           ringBuffer.publishEvent(LongEvent.EVENT_TRANSLATOR, Configuration.VALUE);
        }

        while(eventCount.get() < Configuration.ONE_MLN) {
            Thread.yield();
        }

//        result += ((Configuration.ONE_MLN * 1000L) / (System.currentTimeMillis() - startTime));
//        i++;
//        System.out.println("Test: " + result + " ops/s");
    }

    private static final class LongEvent {

        private long value = -1L;

        public static final EventFactory<LongEvent> EVENT_FACTORY = () -> new LongEvent();

        public static final EventTranslatorOneArg<LongEvent, Long> EVENT_TRANSLATOR = (longEvent, seq, value) -> { longEvent.setValue(value); };

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }


    }


    public static void runBenchmarks(int numOfThreads) throws Exception {
        final String resultFileName = "Disruptor_threads_x" + numOfThreads  + ".csv";
//        final String resultFileName = "LMAXDisruptor_results.csv";


        Options opts = new OptionsBuilder()
                .include(".*" + DisruptorBenchmarks.class.getSimpleName() + ".*")
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







