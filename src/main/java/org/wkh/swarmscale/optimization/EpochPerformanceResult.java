package org.wkh.swarmscale.optimization;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;

class EpochPerformanceResult {
    public final List<Double> fitnessValues;
    public final double[] gbest;
    public final double gbestFitness;
    public final DoubleSummaryStatistics fitnessStatistics;
    
    public EpochPerformanceResult(List<Double> fitnessValues, double[] gbest, double gbestFitness) {
        this.fitnessValues = fitnessValues;
        this.gbest = Arrays.copyOf(gbest, gbest.length);
        this.gbestFitness = gbestFitness;
        
        fitnessStatistics = fitnessValues.stream().collect(DoubleSummaryStatistics::new,
                                                      DoubleSummaryStatistics::accept,
                                                      DoubleSummaryStatistics::combine);
    }
}
