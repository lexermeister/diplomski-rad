import arrayblockingqueue.ArrayBlockingQueueBenchmarks;
import concurrentlinkedqueue.ConcurrentLinkedQueueBenchmark;
import concurrentstack.ConcurrentLinkedDequeBenchmarks;
import disruptor.DisruptorBenchmarks;
import linkedblockingqueue.LinkedBlockingQueueBenchmarks;
import linkedtransferqueue.LinkedTransferQueueBenchmarks;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedTransferQueue;


public class RunBenchmarks {

    public static void main(String[] args) throws Exception {
        int[] threadConfigurations = {1, 4};

        for(int numOfThreads : threadConfigurations) {
            DisruptorBenchmarks.runBenchmarks(numOfThreads);
            LinkedBlockingQueueBenchmarks.runBenchmarks(numOfThreads);
            ArrayBlockingQueueBenchmarks.runBenchmarks(numOfThreads);
            ConcurrentLinkedQueueBenchmark.runBenchmarks(numOfThreads);
            ConcurrentLinkedDequeBenchmarks.runBenchmark(numOfThreads);
            LinkedTransferQueueBenchmarks.runBenchmarks(numOfThreads);
        }
    }

}
