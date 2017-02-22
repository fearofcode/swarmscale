package org.wkh.swarmscale.physicalexample;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.wkh.swarmscale.optimization.EpochPerformanceResult;
import org.wkh.swarmscale.optimization.ObjectiveFunction;
import org.wkh.swarmscale.optimization.ParticleSwarmOptimizer;

public class PIDControlledBallGravitySystemOptimizer {
    public static void main(String[] args) {
        final int populationSize = 100;
        final int dim = 5;
        
        /* TODO why not associate bounds with the objective function? change the interface, ya dingus */
        
        final double[][] bounds = {
            {0.0, 250.0}, /* proportional */
            {0.0, 10}, /* integral */
            {0.0, 2.5}, /* derivative */
            {0.01, 25.0}, /* max output magnitude */
            {5.0, 100.0}, /* control interval */
        };
        
        ObjectiveFunction pidSystemSimulator = new PIDControlledBallGravitySystemObjectiveFunction();
        
        ParticleSwarmOptimizer optimizer = new ParticleSwarmOptimizer(
            populationSize, 
            dim,
            bounds,
            pidSystemSimulator
        );
        
        optimizer.initializePopulation();
        
        optimizer.addEpochListener((result, epoch) -> {
            System.out.println("Epoch " + epoch + ": " + new Date());
            System.out.println("Best result fitness: " + result.gbestFitness);
            System.out.println("Best result value: " + Arrays.toString(result.gbest));
        });
        
        final int iterations = 1000;
        
        long start = System.currentTimeMillis();
        final List<EpochPerformanceResult> results = optimizer.runForIterations(iterations);
        long end = System.currentTimeMillis();
        
        System.out.println("Elapsed: " + (end - start)/1000.0 + "s");
        System.out.println("Best result: " + Arrays.toString(results.get(iterations-1).gbest));
        System.out.println(results.get(iterations-1).gbestFitness);
    }
}
