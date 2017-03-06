package org.wkh.swarmscale.queue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.wkh.swarmscale.optimization.EpochPerformanceResult;
import org.wkh.swarmscale.optimization.ObjectiveFunction;
import org.wkh.swarmscale.optimization.ParticleSwarmOptimizer;

public class CostMinimizingOptimizer {
    public static void main(String[] args) {
        final int populationSize = 250;
        
        final double[][] bounds = {
            {0.0, 500.0},  /* proportional */
            {0.0, 500.0},  /* integral */
            {0.0, 500.0},  /* derivative */
            {0.0, 1000.0}, /* setpoint */
        };

        final int dim = bounds.length;
           
        final int initialWorkload = 50000;
        final int timesteps = 500;
        
        final int initialCapacity = 1;
        final int minimumCapacity = 1;
        final int maximumCapacity = 20;
        
        final int commissionTimeLower = 1;
        final int commissionTimeUpper = 2;
        
        final int baseWorkRateLower = 25;
        final int baseWorkRateUpper = 30;
        
        final double parallelizablePortion = 0.9;
        
        final ObjectiveFunction pidSystemSimulator = new CostMinimizingObjectiveFunction(
                timesteps, 
                initialWorkload, 
                initialCapacity, 
                minimumCapacity, 
                maximumCapacity, 
                commissionTimeLower, 
                commissionTimeUpper, 
                baseWorkRateLower, 
                baseWorkRateUpper, 
                parallelizablePortion,
                Level.SEVERE
        );

        final ParticleSwarmOptimizer optimizer = new ParticleSwarmOptimizer(
            populationSize,
            dim,
            bounds,
            pidSystemSimulator
        );

        optimizer.initializePopulation();

        optimizer.addEpochListener((result, epoch) -> {
            if (epoch % 10 == 0) {
                System.out.println("Epoch " + epoch + ": " + new Date());
                System.out.println("Best result fitness: " + result.gbestFitness);
                System.out.println("Fitness statistics: " + result.fitnessStatistics);
                System.out.println("Best result value: " + Arrays.toString(result.gbest));
            }
        });

        final int iterations = 500;

        final List<EpochPerformanceResult> results = optimizer.runForIterations(iterations);
        
        System.out.println("Best result: " + Arrays.toString(results.get(iterations - 1).gbest));
        System.out.println(results.get(iterations - 1).gbestFitness);
    }
}
