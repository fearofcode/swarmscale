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
            {0.0, 25.0},     /* rotational - proportional - schedule 1 */
            {0.0, 1.0},      /* rotational - integral     - schedule 1 */
            {0.0, 25.0},     /* rotational - derivative   - schedule 1 */
            {0.0, 25.0},     /* position   - proportion   - schedule 1 */
            {0.0, 0.0},      /* position   - integral     - schedule 1 */
            {0.0, 25.0},     /* position   - derivative   - schedule 1 */
            
            {0.0, 25.0},     /* rotational - proportional - schedule 2 */
            {0.0, 1.0},      /* rotational - integral     - schedule 2 */
            {0.0, 25.0},     /* rotational - derivative   - schedule 2 */
            {0.0, 25.0},     /* position   - proportion   - schedule 2 */
            {0.0, 0.0},      /* position   - integral     - schedule 2 */
            {0.0, 25.0},     /* position   - derivative   - schedule 2 */
            
            {0.0, 25.0},     /* rotational - proportional - schedule 3 */
            {0.0, 1.0},      /* rotational - integral     - schedule 3 */
            {0.0, 25.0},     /* rotational - derivative   - schedule 3 */
            {0.0, 25.0},     /* position   - proportion   - schedule 3 */
            {0.0, 0.0},      /* position   - integral     - schedule 3 */
            {0.0, 25.0},     /* position   - derivative   - schedule 3 */
        };

        final int dim = bounds.length;

        final ObjectiveFunction pidSystemSimulator = new DualControllerObjectiveFunction();

        final double diversityLower = 0.05;
        final double diversityUpper = 0.20;
        
        final ParticleSwarmOptimizer optimizer = new ParticleSwarmOptimizer(
            populationSize,
            dim,
            bounds,
            diversityLower,
            diversityUpper,
            pidSystemSimulator
        );

        optimizer.initializePopulation();

        optimizer.addEpochListener((result, epoch) -> {
            System.out.println("Epoch " + epoch + ": " + new Date());
            System.out.println("Statistics: " + result.fitnessStatistics);
            System.out.println("Diversity: " + result.diversity);
            System.out.println("Direction: " + result.direction);
            System.out.println("Best result fitness: " + result.gbestFitness);
            System.out.println("Best result value: " + Arrays.toString(result.gbest));
        });

        final int iterations = 300;
        
        final List<EpochPerformanceResult> results = optimizer.runForIterations(iterations);
        
        System.out.println("Best result: " + Arrays.toString(results.get(iterations - 1).gbest));
        System.out.println(results.get(iterations - 1).gbestFitness);
    }
}
