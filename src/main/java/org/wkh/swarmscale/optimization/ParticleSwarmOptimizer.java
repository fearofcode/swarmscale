package org.wkh.swarmscale.optimization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Minimizes an objective function.
 *
 * See https://en.wikipedia.org/wiki/Particle_swarm_optimization for explanation.
 *
 * There are many variants of the algorithm in existence. This is the basic vanilla one.
 */
public class ParticleSwarmOptimizer {

    /* Standard constants derived by Maurice Clerc */

    /**
     * Inertia
     */
    private static final double W = 0.729;

    /**
     * Personal best coefficient
     */
    private static final double C1 = 1.494;

    /**
     * Global best coefficient
     */
    private static final double C2 = 1.494;

    /**
     * Position
     */
    private final double[][] x;

    /**
     * Velocity
     */
    private final double[][] v;

    /**
     * Personal bests
     */
    private final double[][] pbest;

    /**
     * Cached value of pbest fitness
     */
    private final double[] pbestFitness;

    /**
     * Dimension bounds
     */
    private final double[][] bounds;

    /**
     * Global best
     */
    private double[] gbest;

    /**
     * Cached value of gbest fitness
     */
    private double gbestFitness;

    private final ObjectiveFunction objective;
    private final int populationSize;
    private final int dim;

    private final Random rng;

    private final List<EpochListener> epochListeners;

    private final double[][] seeds;

    /**
     *
     * @param populationSize Number of individuals to create
     * @param dim Dimension. Must match bounds and objective function.
     * @param bounds upper and lower bounds in each dimension. Should be `dim` x 2 in size.
     * @param objective Objective function. Needs to match bounds in the array elements it references.
     */
    public ParticleSwarmOptimizer(
            final int populationSize,
            final int dim,
            final double[][] bounds,
            final ObjectiveFunction objective) {
        this(populationSize, dim, bounds, objective, new double[][]{});
    }

    /**
     *
     * @param populationSize Number of individuals to create
     * @param dim Dimension. Must match bounds and objective function.
     * @param bounds upper and lower bounds in each dimension. Should be `dim` x 2 in size.
     * @param objective Objective function. Needs to match bounds in the array elements it references.
     * @param seeds Initial positions to copy into the population
     */
    public ParticleSwarmOptimizer(
            final int populationSize,
            final int dim,
            final double[][] bounds,
            final ObjectiveFunction objective,
            final double[][] seeds) {
        if (bounds.length != dim) {
            throw new IllegalArgumentException("Got bounds of length " + bounds.length + " != dim " + dim);
        }

        if (seeds.length > populationSize) {
            throw new IllegalArgumentException(
                    "Got seeds of length " + seeds.length + ", exceeding population of " + populationSize
            );
        }

        rng = new Random();

        this.populationSize = populationSize;

        this.objective = objective;

        this.dim = dim;

        this.bounds = bounds;

        x = new double[populationSize][dim];
        v = new double[populationSize][dim];
        pbest = new double[populationSize][dim];
        pbestFitness = new double[populationSize];
        gbest = new double[dim];

        epochListeners = new ArrayList<>();

        /* make a deep copy of seeds */
        this.seeds = new double[seeds.length][dim];

        for (int i = 0; i < seeds.length; i++) {
            if (seeds[i].length != dim) {
                throw new IllegalArgumentException("seeds[" + i + "] length is incorrect");
            }
            this.seeds[i] = Arrays.copyOf(seeds[i], dim);
        }
    }

    public void addEpochListener(EpochListener listener) {
        epochListeners.add(listener);
    }

    public void initializePopulation() {
        for (int i = 0; i < populationSize; i++) {
            for (int j = 0; j < dim; j++) {
                final double lowerBound = bounds[j][0];
                final double upperBound = bounds[j][1];
                final double velocityRange = Math.abs(upperBound - lowerBound);

                if (i < seeds.length) {
                    x[i][j] = seeds[i][j];
                } else {
                    x[i][j] = randomDoubleInRange(lowerBound, upperBound);

                }
                v[i][j] = randomDoubleInRange(
                        -velocityRange,
                        velocityRange
                );
            }

            pbest[i] = Arrays.copyOf(x[i], dim);
        }

        /* evaluate the population to initialize fitness values */
        List<Double> fitnessValues = evaluatePopulation(0);

        /* arbitrarily initialize gbest to the first individual */
        gbest = Arrays.copyOf(x[0], dim);
        gbestFitness = fitnessValues.get(0);

        /* now initialize pbest fitnesses */
        for (int i = 0; i < populationSize; i++) {
            final double fitnessValue = fitnessValues.get(i);
            pbestFitness[i] = fitnessValue;
        }
    }

    private double clipToBounds(double value, int dimension) {
        final double lowerBound = bounds[dimension][0];

        if (value < lowerBound) {
            return lowerBound;
        }

        final double upperBound = bounds[dimension][1];

        if (value > upperBound) {
            return upperBound;
        }

        return value;
    }

    public List<EpochPerformanceResult> runForIterations(int iterations) {
        final List<EpochPerformanceResult> results = new ArrayList<>(iterations);

        for (int epoch = 1; epoch <= iterations; epoch++) {
            for (int i = 0; i < populationSize; i++) {
                for (int d = 0; d < dim; d++) {
                    final double r1 = rng.nextDouble();
                    final double r2 = rng.nextDouble();

                    v[i][d] = W * v[i][d] + C1 * r1 * (pbest[i][d] - x[i][d]) + C2 * r2 * (gbest[d] - x[i][d]);
                    x[i][d] = clipToBounds(x[i][d] + v[i][d], d);
                }
            }

            final int epochDummy = epoch;

            /* because each evaluation runs in parallel, we can easily just do a parallel map */
            List<Double> fitnessValues = evaluatePopulation(epochDummy);

            for (int i = 0; i < fitnessValues.size(); i++) {
                final double currentFitness = fitnessValues.get(i);
                if (currentFitness < pbestFitness[i]) {
                    pbestFitness[i] = currentFitness;
                    pbest[i] = Arrays.copyOf(x[i], dim);
                }

                if (currentFitness < gbestFitness) {
                    gbestFitness = currentFitness;
                    gbest = Arrays.copyOf(x[i], dim);
                }
            }

            final EpochPerformanceResult result = new EpochPerformanceResult(fitnessValues, gbest, gbestFitness);

            epochListeners.forEach(listener -> listener.onEpochComplete(result, epochDummy));

            results.add(result);
        }

        return results;
    }

    private List<Double> evaluatePopulation(final int epoch) {
        return Arrays.stream(x).parallel().map(position
                -> objective.evaluate(position, epoch)
        ).collect(
                Collectors.toList()
        );
    }

    private double randomDoubleInRange(double lowerBound, double upperBound) {
        return rng.nextDouble() * (upperBound - lowerBound) + lowerBound;
    }

    public static void main(String[] args) {
        final int populationSize = 500;

        final double[][] bounds = {
            {-5.12, 5.12},
            {-5.12, 5.12},
            {-5.12, 5.12},
            {-5.12, 5.12},
            {-5.12, 5.12},
            {-5.12, 5.12},
            {-5.12, 5.12},
            {-5.12, 5.12},
            {-5.12, 5.12},
            {-5.12, 5.12},
            {-5.12, 5.12},
            {-5.12, 5.12},};

        /* simple 2D function with global minimum at (0, 0) */
        ObjectiveFunction sphere2d = (x, iteration) -> x[0] * x[0] + x[1] * x[1];

        /* more complex function with global minimum of f(0) = (0, 0, ..., 0). 
         * See <https://upload.wikimedia.org/wikipedia/commons/8/8b/Rastrigin_function.png> for a plot at n = 2. 
         */
        final int dim = 12;
        ObjectiveFunction rastrigin = (x, iteration) -> {
            double sum = 0.0;
            for (int i = 0; i < dim; i++) {
                sum += x[i] * x[i] - 10 * Math.cos(Math.PI * 2 * x[i]);
            }

            return 10 * dim + sum;
        };

        final double[][] seeds = new double[][]{
            {0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01}
        };

        ParticleSwarmOptimizer optimizer = new ParticleSwarmOptimizer(
                populationSize,
                dim,
                bounds,
                rastrigin,
                seeds
        );

        optimizer.initializePopulation();

        optimizer.addEpochListener((result, epoch) -> {
            if (epoch % 10 == 0) {
                System.out.println("Epoch " + epoch + ": " + new Date());
                System.out.println("Best result fitness: " + result.gbestFitness);
            }
        });

        final int iterations = 500;

        long start = System.currentTimeMillis();
        final List<EpochPerformanceResult> results = optimizer.runForIterations(iterations);
        long end = System.currentTimeMillis();

        System.out.println("Elapsed: " + (end - start) / 1000.0 + "s");
        System.out.println("Best result: " + Arrays.toString(results.get(iterations - 1).gbest));
        System.out.println(results.get(iterations - 1).gbestFitness);
    }
}
