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
    private final PriorityQueue<Integer> commissionTimestamps;
    private int activePartitions;
    private long activeConsumers;
    private long processedJobs;
    private long enqueuedJobs;
    private double parallelizablePortion;
    
    public QueueSimulation(int initialCapacity, 
            int commissionTimeLower,
            int commissionTimeUpper,
            double parallelizablePortion) {
        partitions = new ArrayList<>(initialCapacity);
        
        for(int i = 1; i <= initialCapacity; i++) {
            partitions.add(new SimulatedPartition());
        }
        
        activePartitions = initialCapacity;
        activeConsumers = initialCapacity;
        
        rng = new Random();
        
        processedJobs = 0;
        enqueuedJobs = 0;
        commissionTimestamps = new PriorityQueue<>();
        
        this.commissionTimeLower = commissionTimeLower;
        this.commissionTimeUpper = commissionTimeUpper;
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
        return 1.0/((1 - activePartitions) + parallelizablePortion/(activeConsumers + 1));
    }
    public void stepJobs() {
        for(int i = 0; i < partitions.size(); i++) {
            int workRate = workRateCalculator.getWorkRate(i);
            long partitionWork = partitions.get(i).processJobs(workRate);
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
    }
    
    public long[] getPartitionLags() {
        return partitions.stream().mapToLong(SimulatedPartition::getLag).toArray();
    }
    
    public LongSummaryStatistics getLagStatistics() {
        return partitions.stream().collect(Collectors.summarizingLong(SimulatedPartition::getLag));
    }
    
    private void consumerComesOnline() {
        partitions.add(new SimulatedPartition());
        activePartitions++;
        activeConsumers++;
        System.out.println("New consumer online. Active partitions = " + activePartitions);
    }
    
    public void commissionConsumer(int timestamp) {
        int commissionTime = rng.nextInt(commissionTimeUpper - commissionTimeLower) 
                + commissionTimeLower;
        
        commissionTimestamps.add(timestamp + commissionTime);
    }
    /**
     * It's probably safe enough to assume that consumers stop enqueuing new
     * jobs pretty much right away.
     * 
     * We could enqueue the event but I don't think it's important enough to 
     * model.
     */
    public void decomissionConsumer() {
        activePartitions--;
        System.out.println("Consumer taken offline. Active partitions = " + activePartitions);
    }
    
    public static void main(String[] args) {
        final int timesteps = 3;
        
        final int initialCapacity = 10;
        
        final int commissionTimeLower = 1;
        final int commissionTimeUpper = 1;
        
        final int baseWorkRateLower = 10;
        final int baseWorkRateUpper = 12;
        
        final double parallelizablePortion = 0.75;
        
        final int perStepQueueCount = initialCapacity * baseWorkRateLower;
        
        ConsumerWorkRateCalculator calculator = new AmdahlWorkRate(
            baseWorkRateLower,
            baseWorkRateUpper,
            parallelizablePortion
        );
        
        QueueSimulation queue = new QueueSimulation(
            initialCapacity,
            commissionTimeLower,
            commissionTimeUpper,
            calculator
        );
        
        for(int i = 1; i <= timesteps; i++) {
            queue.enqueueJobs(perStepQueueCount);
            
            if (i == 1) {
                queue.commissionConsumer(i);
            }
            
            System.out.println("time " + i + ":");
            
            LongSummaryStatistics stats = queue.getLagStatistics();
            
            long[] lags = queue.getPartitionLags();
            System.out.println("Lags: " + Arrays.toString(lags));
            System.out.println("Lag statistics: " + stats);
            
            queue.stepSystem(i);
            
            System.out.println("Total enqueued jobs: " + queue.getEnqueuedJobs());
            System.out.println("Processed jobs: " + queue.getProcessedJobs());
            System.out.println();
        }
    }
}
