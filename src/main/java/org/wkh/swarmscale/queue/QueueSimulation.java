package org.wkh.swarmscale.queue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.stream.Collectors;

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
    /* eh I should use DI and interfaces to make this same class work on a 
    live cluster but IDK if this will even work
    */
    private final List<SimulatedPartition> partitions;
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
    private long activeConsumers;
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
    
    public long getActiveConsumers() {
        return activeConsumers;
    }
    
    public long getEnqueuedJobs() {
        return enqueuedJobs;
    }
    
    public long getProcessedJobs() {
        return processedJobs;
    }
    
    /**
     * Enqueue in a load-balanced way using power-of-two-choices algorithm.
     */
    public void enqueueJob() {
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
    
    public void enqueueJobs(int count) {
        for(int i = 1; i <= count; i++) {
            enqueueJob();
        }
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
        }
    }
    
    public void stepSystem(int timestamp) {
        final Integer nextEventTimestamp = commissionTimestamps.peek();
        
        if (nextEventTimestamp != null && nextEventTimestamp <= timestamp) {
            commissionTimestamps.poll();
            consumerComesOnline();
        }
        
        stepJobs();
        
        decommissionIdleExcessConsumers();
    }

    private void decommissionIdleExcessConsumers() {
        for(int i = activePartitions; i < partitions.size() && partitions.size() > minimumCapacity; i++) {
            if (partitions.get(i).getLag() == 0) {
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
        if (partitions.size() >= maximumCapacity) {
            return;
        }
        
        activePartitions++;
        activeConsumers++;
        
        if (activePartitions > partitions.size()) {
            partitions.add(new SimulatedPartition());
        }
    }
    
    public void commissionConsumer(int timestamp) {
        int commissionTime = rng.nextInt(commissionTimeUpper - commissionTimeLower + 1) 
                + commissionTimeLower;
        
        commissionTimestamps.add(timestamp + commissionTime);
    }
    
    public void deactivatePartition() {
        activePartitions--;
    }
    
    /**
     * We can't take down a consumer while it has data queued, but we can let it process what it already has queued and
     * then decommission it when its lag is zero.
     * 
     * @param index Partition index to remove
     */
    private void decomissionConsumer(int index) {
        partitions.remove(index);
        activeConsumers--;
    }
    
    public static void main(String[] args) {
        final int timesteps = 25;
        
        final int initialCapacity = 10;
        final int minimumCapacity = 1;
        final int maximumCapacity = 15;
        
        final int commissionTimeLower = 5;
        final int commissionTimeUpper = 7;
        
        final int baseWorkRateLower = 10;
        final int baseWorkRateUpper = 12;
        
        final double parallelizablePortion = 0.9;
        
        final int perStepQueueCount = 75;
        
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
        
        for(int i = 1; i <= timesteps; i++) {
            queue.enqueueJobs(perStepQueueCount);
            
            if (i == 1) {
                queue.commissionConsumer(i);
            }
            
            if (i == 15) {
                queue.deactivatePartition();
            }
            
            queue.stepSystem(i);
            
            System.out.println("Timestep " + i + ":");
            
            LongSummaryStatistics stats = queue.getLagStatistics();
            
            long[] lags = queue.getPartitionLags();
            System.out.println("Lags: " + Arrays.toString(lags));
            System.out.println("Lag statistics: " + stats);
            
            
            System.out.println("Total enqueued jobs: " + queue.getEnqueuedJobs());
            System.out.println("Processed jobs: " + queue.getProcessedJobs());
            System.out.println();
        }
    }
}
