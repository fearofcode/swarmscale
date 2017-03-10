package org.wkh.swarmscale.physics.invertedpendulum;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.wkh.swarmscale.optimization.EpochPerformanceResult;
import org.wkh.swarmscale.optimization.ObjectiveFunction;
import org.wkh.swarmscale.optimization.ParticleSwarmOptimizer;

public class DualControllerOptimizer {
    public static void main(String[] args) {
        final int populationSize = 200;
        
        final double[][] bounds = {
            {0.0, 10.0}, /* rotational - proportional */
            {0.0, 0.5}, /* rotational - integral */
            {0.0, 25.0}, /* rotational - derivative */
            {0.0, 10.0}, /* position - proportion */
            {0.0, 0.5}, /* position - integral */
            {0.0, 25.0}, /* position - derivative */
        };

        final int dim = bounds.length;

        final ObjectiveFunction pidSystemSimulator = new DualControllerObjectiveFunction();

        final ParticleSwarmOptimizer optimizer = new ParticleSwarmOptimizer(
            populationSize,
            dim,
            bounds,
            pidSystemSimulator
        );

        optimizer.initializePopulation();

        optimizer.addEpochListener((result, epoch) -> {
            System.out.println("Epoch " + epoch + ": " + new Date());
            System.out.println("Statistics: " + result.fitnessStatistics);
            System.out.println("Best result fitness: " + result.gbestFitness);
            System.out.println("Best result value: " + Arrays.toString(result.gbest));
        });

        final int iterations = 100;

        final List<EpochPerformanceResult> results = optimizer.runForIterations(iterations);
        
        System.out.println("Best result: " + Arrays.toString(results.get(iterations - 1).gbest));
        System.out.println(results.get(iterations - 1).gbestFitness);
    }
}
