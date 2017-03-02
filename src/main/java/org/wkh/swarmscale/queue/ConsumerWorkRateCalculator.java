package org.wkh.swarmscale.queue;

@FunctionalInterface
public interface ConsumerWorkRateCalculator {
    public int getWorkRate(int partitionIndex);
}
