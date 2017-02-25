package org.wkh.swarmscale.optimization;

@FunctionalInterface
public interface EpochListener {

    public void onEpochComplete(EpochPerformanceResult result, int epoch);
}
