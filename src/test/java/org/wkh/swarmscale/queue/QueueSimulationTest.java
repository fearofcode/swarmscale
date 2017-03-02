package org.wkh.swarmscale.queue;

import static org.junit.Assert.*;

public class QueueSimulationTest {
    @org.junit.Test
    public void testBasics() {
        int initialCapacity = 1;
        int commissionTimeLower = 0;
        int commissionTimeUpper = 0;
        
        ConsumerWorkRateCalculator uniformWorkRateCalculator = (i) -> 1;
        
        QueueSimulation queue = new QueueSimulation(
                initialCapacity,
                commissionTimeLower,
                commissionTimeUpper,
                uniformWorkRateCalculator
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
        assertEquals(queue.getProcessedJobs(), 1);
        
        queue.stepJobs();
        
        assertEquals(queue.getProcessedJobs(), 2);
        assertEquals(queue.getLagStatistics().getSum(), 0);
        
        queue.enqueueJobs(1000);
        
        assertEquals(queue.getLagStatistics().getSum(), 1000);
        
        for(int i = 1; i <= 1000; i++) {
            queue.stepJobs();
            assertEquals(queue.getLagStatistics().getSum(), 1000 - i);
        }
        
        assertEquals(queue.getLagStatistics().getSum(), 0);
        
        assertEquals(queue.getProcessedJobs(), 1002);
    }
}
