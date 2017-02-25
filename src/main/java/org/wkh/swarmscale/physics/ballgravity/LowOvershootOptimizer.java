package org.wkh.swarmscale.physics.ballgravity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.wkh.swarmscale.optimization.EpochPerformanceResult;
import org.wkh.swarmscale.optimization.ObjectiveFunction;
import org.wkh.swarmscale.optimization.ParticleSwarmOptimizer;

public class LowOvershootOptimizer {

    public static void main(String[] args) {
        final int populationSize = 250;
        
        final double[][] bounds = {
            {0.0, 250.0}, /* proportional */
            {0.0, 25.0}, /* integral */
            {0.0, 250.0}, /* derivative */
        };

        final int dim = bounds.length;

        final ObjectiveFunction pidSystemSimulator = new LowOvershootObjectiveFunction();

        final ParticleSwarmOptimizer optimizer = new ParticleSwarmOptimizer(
            populationSize,
            dim,
            bounds,
            pidSystemSimulator
        );

        optimizer.initializePopulation();

        optimizer.addEpochListener((result, epoch) -> {
            System.out.println("Epoch " + epoch + ": " + new Date());
            System.out.println("Summary statistics: " + result.fitnessStatistics);
            System.out.println("Best result fitness: " + result.gbestFitness);
            System.out.println("Best result value: " + Arrays.toString(result.gbest));
        });

        final int iterations = 200;

        long start = System.currentTimeMillis();
        final List<EpochPerformanceResult> results = optimizer.runForIterations(iterations);
        long end = System.currentTimeMillis();

        System.out.println("Elapsed: " + (end - start) / 1000.0 + "s");
        System.out.println("Best result: " + Arrays.toString(results.get(iterations - 1).gbest));
        System.out.println(results.get(iterations - 1).gbestFitness);
    }
}
