package org.wkh.swarmscale.queue;

public class SimulatedPartition {
    private long queuedJobs = 0;

    public void queueJob() {
        queuedJobs++;
    }

    public long getLag() {
        return queuedJobs;
    }
    
    public long processJobs(long count) {
        final long processedJobs = Math.min(queuedJobs, count);
        queuedJobs -= processedJobs;
        
        return processedJobs;
    }
}
