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

    /* Standard constant derived by Maurice Clerc */

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

    private double direction;
    private final double diversityLower;
    private final double diversityUpper;
    
    /**
     *
     * @param populationSize Number of individuals to create
     * @param dim Dimension. Must match bounds and objective function.
     * @param bounds upper and lower bounds in each dimension. Should be `dim` x 2 in size.
     * @param diversityLower Lower diversity bound
     * @param diversityUpper Upper diversity bound
     * @param objective Objective function. Needs to match bounds in the array elements it references.
     */
    public ParticleSwarmOptimizer(
            final int populationSize,
            final int dim,
            final double[][] bounds,
            final double diversityLower,
            final double diversityUpper,
            final ObjectiveFunction objective) {
        this(populationSize, dim, bounds, diversityLower, diversityUpper, objective, new double[][]{});
    }

    /**
     *
     * @param populationSize Number of individuals to create
     * @param dim Dimension. Must match bounds and objective function.
     * @param bounds upper and lower bounds in each dimension. Should be `dim` x 2 in size.
     * @param diversityLower Lower diversity bound
     * @param diversityUpper Upper diversity bound
     * @param objective Objective function. Needs to match bounds in the array elements it references.
     * @param seeds Initial positions to copy into the population
     */
    public ParticleSwarmOptimizer(
            final int populationSize,
            final int dim,
            final double[][] bounds,
            final double diversityLower,
            final double diversityUpper,
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
        
        direction = 1.0;
        this.diversityLower = diversityLower;
        this.diversityUpper = diversityUpper;
    }

    public double setDirection(final int epoch) {
        double currentDiversity = swarmDiversity();
        if (direction > 0 && currentDiversity < diversityLower) {
            System.out.println("Epoch " + epoch + ": Current diversity = " + currentDiversity + ", < " + diversityLower + ". Switching direction to repulsing.");
            direction = -1.0;
        } else if (direction < 0 && currentDiversity > diversityUpper) {
            System.out.println("Epoch " + epoch + ": Current diversity = " + currentDiversity + ", > " + diversityUpper + ". Switching direction to attracting.");
            direction = 1.0;
        }
        
        return currentDiversity;
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

    /**
     * Calculate Euclidean distance (sqrt((y_1 - x_1)^2 + ... (y_n - x_n)^2)) between two arrays.
     * @param x first array
     * @param y second array
     * @return 
     */
    public static double distance(double[] x, double[] y) {
        double sum = 0;
        
        if (x.length != y.length) {
            throw new IllegalArgumentException();
        }
        
        for(int i = 0; i < x.length; i++) {
            sum += (y[i] - x[i])*(y[i] - x[i]);
        }
        
        return Math.sqrt(sum);
    }

    public double[] minimumPoint() {
        double[] minimum = new double[dim];
        for(int i = 0; i < dim; i++) {
            minimum[i] = bounds[i][0];
        }
        
        return minimum;
    }
    
    public double[] maximumPoint() {
        double[] maximum = new double[dim];
        for(int i = 0; i < dim; i++) {
            maximum[i] = bounds[i][1];
        }
        
        return maximum;
    }
    
    public double boundsLength() {
        return distance(minimumPoint(), maximumPoint());
    }
    
    public double[] averagePoint() {
        double[] average = new double[dim];
        
        for (int i = 0; i < populationSize; i++) {
            for (int d = 0; d < dim; d++) {
                average[d] += x[i][d];
            }
        }
        
        for (int d = 0; d < dim; d++) {
            average[d] /= populationSize;
        }
        
        return average;
    }
    
    public double swarmDiversity() {
        double[] average = averagePoint();
        //System.out.println("Average point: " + Arrays.toString(average));
        double individualDiversitySum = 0.0;
        
        for (int i = 0; i < populationSize; i++) {
            individualDiversitySum += distance(x[i], average);
        }
        //System.out.println("Individual diversity sum: " + individualDiversitySum);
        //System.out.println("boundsLength: " + boundsLength());
        //System.out.println("individualDiversitySum/(populationSize * boundsLength()) = " + individualDiversitySum/(populationSize * boundsLength()));
        
        return individualDiversitySum/(populationSize * boundsLength());
    }
    
    public List<EpochPerformanceResult> runForIterations(int iterations) {
        final List<EpochPerformanceResult> results = new ArrayList<>(iterations);

        for (int epoch = 1; epoch <= iterations; epoch++) {
            final double currentDiversity = setDirection(epoch);
            final double inertiaMultiplier = (1.0 - (double)epoch/iterations);
            
            for (int i = 0; i < populationSize; i++) {
                for (int d = 0; d < dim; d++) {
                    final double r1 = rng.nextDouble();
                    final double r2 = rng.nextDouble();

                    v[i][d] = inertiaMultiplier * W * v[i][d] + direction*(C1 * r1 * (pbest[i][d] - x[i][d]) + C2 * r2 * (gbest[d] - x[i][d]));
                    
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

            final EpochPerformanceResult result = new EpochPerformanceResult(fitnessValues, gbest, gbestFitness, currentDiversity, direction);

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

        /* more complex function with global minimum of f(0) = (0, 0, ..., 0). 
         * See <https://upload.wikimedia.org/wikipedia/commons/8/8b/Rastrigin_function.png> for a plot at n = 2. 
         */
        final int dim = bounds.length;
        
        ObjectiveFunction rastrigin = (x, iteration) -> {
            double sum = 0.0;
            for (int i = 0; i < dim; i++) {
                sum += x[i] * x[i] - 10 * Math.cos(Math.PI * 2 * x[i]);
            }

            return 10 * dim + sum;
        };

        ParticleSwarmOptimizer optimizer = new ParticleSwarmOptimizer(
                populationSize,
                dim,
                bounds,
                0.001,
                0.1,
                rastrigin
        );

        optimizer.initializePopulation();

        optimizer.addEpochListener((result, epoch) -> {
            if (epoch % 1000 == 0) {
                System.out.println("Epoch " + epoch + ": " + new Date());
                System.out.println("Best result fitness: " + result.gbestFitness);
                System.out.println("Diversity: " + result.diversity);
                System.out.println("Direction: " + result.direction);
            }
        });

        final int iterations = 50000;

        long start = System.currentTimeMillis();
        final List<EpochPerformanceResult> results = optimizer.runForIterations(iterations);
        long end = System.currentTimeMillis();

        System.out.println("Elapsed: " + (end - start) / 1000.0 + "s");
        System.out.println("Best result: " + Arrays.toString(results.get(iterations - 1).gbest));
        System.out.println(results.get(iterations - 1).gbestFitness);
    }
}
