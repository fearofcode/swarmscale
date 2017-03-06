package org.wkh.swarmscale.queue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.logging.Logger;

/**
 * Simple simulation of a partitioned queue system.
 * 
 * Not thread safe at all. Not concurrent at all. It doesn't need to be either
 * of those things because it's going to be called by separate threads that are
 * completely independent.
 * 
 * Loosely based on my current understanding of how Apache Kafka works.
 * 
 * Partitions can't be deleted, so we track the active partitions to enqueue 
 * stuff to.
 * 
 * We assume there's a delay to have new partitions/consumers come online. That
 * gets stored in a priority queue.
 * 
 * Like the work rate calculator, we model consumer commissioning times as a
 * uniform distribution. This, too, may be better served by non-uniform 
 * distributions. We may need general distribution + sampling stuff but I'm
 * later but for now I'm just going to go with uniform to begin with.
 */
public class QueueSimulation {
    public final static Logger LOGGER = Logger.getLogger(QueueSimulation.class.getName());
    
    /* eh I should use DI and interfaces to make this same class work on a 
    live cluster but IDK if this will even work
    */
    private final List<SimulatedPartition> partitions;
    private final SimulatedPartition batchPartition;
    private final Random rng;
    private final int commissionTimeLower;
    private final int commissionTimeUpper;
    private final int minimumCapacity;
    private final int maximumCapacity;
    private final PriorityQueue<Integer> commissionTimestamps;
    private final double parallelizablePortion;
    private final int baseWorkRateLower;
    private final int baseWorkRateUpper;
    private int activePartitions;
    private int activeConsumers;
    private long processedJobs;
    private long enqueuedJobs;
    
    public QueueSimulation(int initialCapacity,
            int minimumCapacity,
            int maximumCapacity,
            int commissionTimeLower,
            int commissionTimeUpper,
            int baseWorkRateLower,
            int baseWorkRateUpper,
            double parallelizablePortion) {
        batchPartition = new SimulatedPartition();
        partitions = new ArrayList<>(initialCapacity);
        
        for(int i = 1; i <= initialCapacity; i++) {
            partitions.add(new SimulatedPartition());
        }
        
        activePartitions = initialCapacity;
        activeConsumers = initialCapacity;
        
        if (commissionTimeUpper < commissionTimeLower || baseWorkRateLower > baseWorkRateUpper) {
            throw new IllegalArgumentException("Lower must be < upper");
        }
        
        this.minimumCapacity = minimumCapacity;
        this.maximumCapacity = maximumCapacity;
        this.commissionTimeLower = commissionTimeLower;
        this.commissionTimeUpper = commissionTimeUpper;
        this.baseWorkRateLower = baseWorkRateLower;
        this.baseWorkRateUpper = baseWorkRateUpper;
        this.parallelizablePortion = parallelizablePortion;

        rng = new Random();
        
        processedJobs = 0;
        enqueuedJobs = 0;
        commissionTimestamps = new PriorityQueue<>();
    }
    
    public PriorityQueue<Integer> getCommissionTimestamps() {
        return commissionTimestamps;
    }
    
    public int getMinimumCapacity() {
        return minimumCapacity;
    }

    public int getMaximumCapacity() {
        return maximumCapacity;
    }
    
    public int getActiveConsumers() {
        return activeConsumers;
    }
    
    public long getEnqueuedJobs() {
        return enqueuedJobs;
    }
    
    public long getProcessedJobs() {
        return processedJobs;
    }
    
    public int getQueuedConsumers() {
        return commissionTimestamps.size();
    }
    
    public boolean consumersQueued() {
        return getQueuedConsumers() != 0;
    }
    
    public long getBatchLag() {
        return batchPartition.getLag();
    }
    
    public long getTotalLag() {
        return getBatchLag() + getLagStatistics().getSum();
    }
    
    /**
     * Enqueue in a load-balanced way using power-of-two-choices algorithm.
     */
    public void enqueueJob() {
        if (activePartitions == 0) {
            System.out.println("welp");
        }
        
        int index1 = rng.nextInt(activePartitions);
        int index2 = rng.nextInt(activePartitions);
        
        SimulatedPartition partition1 = partitions.get(index1);
        SimulatedPartition partition2 = partitions.get(index2);
        
        if (partition1.getLag() < partition2.getLag()) {
            partition1.queueJob();
        } else {
            partition2.queueJob();
        }
        
        enqueuedJobs++;
    }
    
    public void enqueueBatchWorkload(long count) {
        batchPartition.setToPoint(count);
    }
    
    public void distributeBatchWorkToPartitions(long count) {
        long effectiveAmount = Math.min(count, batchPartition.getLag());
        
        for(int i = 1; i <= effectiveAmount; i++) {
            enqueueJob();
            batchPartition.setToPoint(batchPartition.getLag() - 1);
        }
    }
    
    /**
     * Queue jobs in the worker partitions. For batch workloads, queue them on the batch partition instead.
     * @param count Number of jobs to create
     */
    public void enqueueJobs(long count) {
        for(long i = 1; i <= count; i++) {
            enqueueJob();
        }
        LOGGER.log(Level.INFO, "Enqueued {0} jobs", count);
    }
    
    public double getSpeedupFactor() {
        return 1.0/((1 - parallelizablePortion) + parallelizablePortion/(activeConsumers + 1));
    }
    
    public void stepJobs() {
        /* try to model Amdahl's Law effects and random variation in a simple, concise way */
        int effectiveWorkRate = rng.nextInt(baseWorkRateUpper - baseWorkRateLower + 1) + baseWorkRateLower;
        final int meanWorkRate = (int) Math.round(effectiveWorkRate * getSpeedupFactor() / activeConsumers);
        
        for(int i = 0; i < partitions.size(); i++) {
            long partitionWork = partitions.get(i).processJobs(meanWorkRate);
            processedJobs += partitionWork;
            LOGGER.log(Level.FINER, "Consumer {0} processed {1} jobs. Now processedJobs = {2}", 
                    new Object[]{i, partitionWork, processedJobs});
            LOGGER.log(Level.FINER, "Lags: {0}", Arrays.toString(getPartitionLags()));
        }
    }
    
    public void stepSystem(int timestamp) {
        LOGGER.log(Level.INFO, "Timestamp {0}:", timestamp);
        LOGGER.log(Level.INFO, "At beginning of timestamp, lags: {0}", Arrays.toString(getPartitionLags()));
        LOGGER.log(Level.INFO, "Batch lag: {0}", getBatchLag());
        
        Integer nextEventTimestamp = commissionTimestamps.peek();
        
        /* bring all consumers ready to go online */
        do {
            if (nextEventTimestamp != null && nextEventTimestamp <= timestamp) {
                LOGGER.log(Level.INFO, "nextEventTimestamp = {0}, going to spin up a consumer", nextEventTimestamp);
                commissionTimestamps.poll();
                consumerComesOnline();
                nextEventTimestamp = commissionTimestamps.peek();
            }
        } while (nextEventTimestamp != null && nextEventTimestamp <= timestamp);
        
        stepJobs();
        
        if (timestamp > commissionTimeUpper +1 && !consumersQueued()) {
            decommissionIdleExcessConsumers();
        }
        
        
        LongSummaryStatistics stats = getLagStatistics();
            
        long[] lags = getPartitionLags();

        LOGGER.log(Level.INFO, "At end of timestamp, lags: {0}", Arrays.toString(lags));
        LOGGER.log(Level.INFO, "Lag statistics: {0}", stats);
        LOGGER.log(Level.INFO, "Batch lag: {0}", getBatchLag());
        LOGGER.log(Level.INFO, "Total enqueued jobs: {0}", enqueuedJobs);
        LOGGER.log(Level.INFO, "Processed jobs: {0}", processedJobs);
        LOGGER.log(Level.INFO, "Active partitions: {0}", activePartitions);
        LOGGER.log(Level.INFO, "Active consumers: {0}", activeConsumers);
        LOGGER.log(Level.INFO, "--------------------------------------------------------------------------------");
    }

    private void decommissionIdleExcessConsumers() {
        for(int i = activePartitions; i < partitions.size() && partitions.size() > minimumCapacity; i++) {
            final SimulatedPartition partition = partitions.get(i);
            if (partition.getLag() == 0) {
                LOGGER.log(Level.INFO, "Consumer at index {0} has 0 queued jobs, spinning it down", i);
                decomissionConsumer(i);
            }
            
        }
    }
    
    public long[] getPartitionLags() {
        return partitions.stream().mapToLong(SimulatedPartition::getLag).toArray();
    }
    
    public LongSummaryStatistics getLagStatistics() {
        return partitions.stream().collect(Collectors.summarizingLong(SimulatedPartition::getLag));
    }
    
    private void consumerComesOnline() {
        if (activeConsumers >= maximumCapacity) {
            LOGGER.log(Level.INFO, "activeConsumers >= maximumCapacity, so bailing out");
            return;
        }
        
        activePartitions++;
        activeConsumers++;
        
        LOGGER.log(Level.INFO, "New consumer online. Active partitions = {0}, active consumers = {1}", 
                new Object[]{activePartitions, activeConsumers});

        if (activePartitions > partitions.size()) {
            partitions.add(new SimulatedPartition());
            LOGGER.log(Level.INFO, "Added a new partition to the array. partitions.size() = {0}", partitions.size());
        }
    }
    
    public void commissionConsumer(int timestamp) {
        if (activeConsumers >= maximumCapacity) {
            LOGGER.log(Level.INFO, "activeConsumers >= maximumCapacity, so bailing out");
            return;
        }
        
        int commissionTime = rng.nextInt(commissionTimeUpper - commissionTimeLower + 1) 
                + commissionTimeLower;
        final int popTime = timestamp + commissionTime;
        LOGGER.log(Level.INFO, "Commissioning a consumer that will come online at timestamp {0}", popTime);
        commissionTimestamps.add(popTime);
    }
    
    public void deactivatePartition() {
        if (activeConsumers <= minimumCapacity || activePartitions <= minimumCapacity) {
            LOGGER.log(Level.INFO, "activeConsumers <= minimumCapacity || activePartitions <= minimumCapacity. Not going to deactivate anything.");
            return;
        }
        
        activePartitions--;
        
        if (activePartitions < 0)  {
            System.out.println("welp");
        }
        
        LOGGER.log(Level.INFO, "Dcommissioned a partition. Now activePartitions = {0}", activePartitions);
    }
    
    /**
     * We can't take down a consumer while it has data queued, but we can let it process what it already has queued and
     * then decommission it when its lag is zero.
     * 
     * @param index Partition index to remove
     */
    private void decomissionConsumer(int index) {
        if (activeConsumers <= minimumCapacity) {
            LOGGER.log(Level.INFO, "activeConsumers <= minimumCapacity. Not going to deactivate anything.");
            return;
        }
        
        partitions.remove(index);
        activeConsumers--;
        LOGGER.log(Level.INFO, "Dcommissioned a consumer. Now activeConsumers = {0}", activeConsumers);
    }
    
    public static void main(String[] args) {
        LOGGER.setLevel(Level.INFO);
        final long start = System.nanoTime();
        
        /* simulate system applying control once a minute */
        
        final int timesteps = 25;
        
        final int initialCapacity = 10;
        final int minimumCapacity = 1;
        final int maximumCapacity = 20;
        
        final int commissionTimeLower = 1;
        final int commissionTimeUpper = 2;
        
        final int baseWorkRateLower = 10*60;
        final int baseWorkRateUpper = 12*60;
        
        final double parallelizablePortion = 0.9;
        
        final long perStepQueueCount = 75*60;
        
        QueueSimulation queue = new QueueSimulation(
            initialCapacity,
            minimumCapacity,
            maximumCapacity,
            commissionTimeLower,
            commissionTimeUpper,
            baseWorkRateLower,
            baseWorkRateUpper,
            parallelizablePortion
        );
        
        /* queue jobs every time timestamp, and spin some consumers up/down at a couple arbitrarily chosen points */
        for(int i = 1; i <= timesteps; i++) {
            queue.enqueueJobs(perStepQueueCount);
            
            if (i == 1) {
                queue.commissionConsumer(i);
            }
            
            if (i == 15) {
                queue.deactivatePartition();
            }
            
            queue.stepSystem(i);
        }
        
        final long elapsed = System.nanoTime() - start;
        final double elapsedMs = elapsed/(1.0e6);
        
        LOGGER.log(Level.INFO, "Elapsed: {0}ms", elapsedMs);
    }
}
