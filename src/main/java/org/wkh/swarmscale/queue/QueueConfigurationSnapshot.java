package org.wkh.swarmscale.queue;

public class QueueConfigurationSnapshot {
    public final int activeConsumers;
    public final long totalLag;
    public final long totalProcessedJobs;
    
    public QueueConfigurationSnapshot(int activeConsumers, long totalLag, long totalProcessedJobs) {
        this.activeConsumers = activeConsumers;
        this.totalLag = totalLag;
        this.totalProcessedJobs = totalProcessedJobs;
    }
}
