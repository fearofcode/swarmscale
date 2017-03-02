package org.wkh.swarmscale.queue;

import static org.junit.Assert.*;

public class QueueSimulationTest {
    @org.junit.Test
    public void testBasics() {
        final int initialCapacity = 1;
        final int minimumCapacity = 1;
        final int maximumCapacity = 15;
        
        final int commissionTimeLower = 1;
        final int commissionTimeUpper = 1;
        
        final int baseWorkRateLower = 10;
        final int baseWorkRateUpper = 12;
        
        final double parallelizablePortion = 0.9;
        
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
        
        queue.enqueueJob();
        assertEquals(queue.getLagStatistics().getSum(), 1);

        long[] lags = queue.getPartitionLags();
        
        assertTrue(lags[0] == 1);
        
        queue.enqueueJob();
        assertEquals(queue.getLagStatistics().getSum(), 2);
        
        lags = queue.getPartitionLags();
        
        assertTrue(lags[0] == 2);
        
        assertEquals(queue.getProcessedJobs(), 0);
        
        queue.stepJobs();        
        queue.stepJobs();
        
        assertEquals(queue.getProcessedJobs(), 2);
        assertEquals(queue.getLagStatistics().getSum(), 0);
        
        queue.enqueueJobs(1000);
        
        assertEquals(queue.getLagStatistics().getSum(), 1000);
        
        for(int i = 1; i <= 1000; i++) {
            queue.stepJobs();
        }
        
        assertEquals(queue.getLagStatistics().getSum(), 0);
        
        assertEquals(queue.getProcessedJobs(), 1002);
    }
}
