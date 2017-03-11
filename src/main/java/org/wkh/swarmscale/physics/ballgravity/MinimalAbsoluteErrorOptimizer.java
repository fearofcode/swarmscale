package org.wkh.swarmscale.physics.ballgravity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.wkh.swarmscale.optimization.EpochPerformanceResult;
import org.wkh.swarmscale.optimization.ObjectiveFunction;
import org.wkh.swarmscale.optimization.ParticleSwarmOptimizer;

public class MinimalAbsoluteErrorOptimizer {

    public static void main(String[] args) {
        final int populationSize = 250;
        /* TODO maybe associate bounds with the objective function? */

        final double[][] bounds = {
            {0.0, 250.0}, /* proportional */
            {0.0, 25.0}, /* integral */
            {0.0, 250.0}, /* derivative */};

        final int dim = bounds.length;

        final ObjectiveFunction pidSystemSimulator = new MinimalAbsoluteErrorObjectiveFunction();

        boolean seedWithZieglerNichols = true;

        final ParticleSwarmOptimizer optimizer;

        if (seedWithZieglerNichols) {
            /*
            PID tight control: 0.6 * Kc, 0.5 * Tc, 0.125 * Tc
            PID some overshoot: 0.33 * Kc, 0.5 * Tc, 0.33 * Tc
            PID no overshoot: 0.2 * Kc, 0.3 * Tc, 0.5 * Tc
             */
            double criticalGain = 25.0;
            double oscillationTime = 0.1;

            double[][] zieglerNicholsSeeds = {
                {0.6 * criticalGain, 0.5 * oscillationTime, 0.125 * oscillationTime},
                {0.33 * criticalGain, 0.5 * oscillationTime, 0.33 * oscillationTime},
                {0.2 * criticalGain, 0.3 * oscillationTime, 0.5 * oscillationTime},};

            optimizer = new ParticleSwarmOptimizer(
                    populationSize,
                    dim,
                    bounds,
                    1.0,
                    2.0,
                    pidSystemSimulator,
                    zieglerNicholsSeeds
            );
        } else {
            optimizer = new ParticleSwarmOptimizer(
                    populationSize,
                    dim,
                    bounds,
                    1.0,
                    2.0,
                    pidSystemSimulator
            );
        }

        optimizer.initializePopulation();

        optimizer.addEpochListener((result, epoch) -> {
            System.out.println("Epoch " + epoch + ": " + new Date());
            System.out.println("Best result fitness: " + result.gbestFitness);
            System.out.println("Best result value: " + Arrays.toString(result.gbest));
        });

        final int iterations = 250;

        long start = System.currentTimeMillis();
        final List<EpochPerformanceResult> results = optimizer.runForIterations(iterations);
        long end = System.currentTimeMillis();

        System.out.println("Elapsed: " + (end - start) / 1000.0 + "s");
        System.out.println("Best result: " + Arrays.toString(results.get(iterations - 1).gbest));
        System.out.println(results.get(iterations - 1).gbestFitness);
    }
}
