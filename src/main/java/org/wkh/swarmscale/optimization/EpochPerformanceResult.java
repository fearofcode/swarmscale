package org.wkh.swarmscale.optimization;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;

public class EpochPerformanceResult {

    public final List<Double> fitnessValues;
    public final double[] gbest;
    public final double gbestFitness;
    public final DoubleSummaryStatistics fitnessStatistics;
    public final double diversity;
    public final double direction;
    
    public EpochPerformanceResult(List<Double> fitnessValues, double[] gbest, double gbestFitness, double diversity, double direction) {
        this.fitnessValues = fitnessValues;
        this.gbest = Arrays.copyOf(gbest, gbest.length);
        this.gbestFitness = gbestFitness;
        this.diversity = diversity;
        this.direction = direction;
        
        fitnessStatistics = fitnessValues.stream().collect(DoubleSummaryStatistics::new,
                DoubleSummaryStatistics::accept,
                DoubleSummaryStatistics::combine);
    }
}
