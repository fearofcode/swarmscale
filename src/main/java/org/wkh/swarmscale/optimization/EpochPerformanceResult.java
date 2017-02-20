package org.wkh.swarmscale.optimization;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;

class EpochPerformanceResult {
    public final double[] fitnessValues;
    public final double[] gbest;
    public final double gbestFitness;
    public final DoubleSummaryStatistics fitnessStatistics;
    
    public EpochPerformanceResult(double[] fitnessValues, double[] gbest, double gbestFitness) {
        this.fitnessValues = Arrays.copyOf(fitnessValues, fitnessValues.length);
        this.gbest = Arrays.copyOf(gbest, gbest.length);
        this.gbestFitness = gbestFitness;
        
        fitnessStatistics = Arrays.stream(fitnessValues).collect(DoubleSummaryStatistics::new,
                                                      DoubleSummaryStatistics::accept,
                                                      DoubleSummaryStatistics::combine);
    }
}
