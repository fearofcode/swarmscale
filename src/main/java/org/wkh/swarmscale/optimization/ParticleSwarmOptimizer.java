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
 * See https://en.wikipedia.org/wiki/Particle_swarm_optimization for 
 * explanation.
 * 
 * There are many variants of the algorithm in existence. This is the basic
 * vanilla one.
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
    
    /**
     * 
     * @param populationSize Number of individuals to create
     * @param dim Dimension. Must match bounds and objective function.
     * @param bounds upper and lower bounds in each dimension. Should be 
     * `dim` x 2 in size.
     * @param objective Objective function. Needs to match bounds in the array
     * elements it references.
     */
    public ParticleSwarmOptimizer(
            final int populationSize, 
            final int dim,
            final double[][] bounds,
            final ObjectiveFunction objective) {
        if (bounds.length != dim) {
            throw new IllegalArgumentException("Got bounds of length " + bounds.length + " != dim " + dim);
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
    }

    public void addEpochListener(EpochListener listener) {
        epochListeners.add(listener);
    }
    
    public void initializePopulation() {
        for(int i = 0; i < populationSize; i++) {
            for(int j = 0; j < dim; j++) {
                final double lowerBound = bounds[j][0];
                final double upperBound = bounds[j][1];
                final double velocityRange = Math.abs(upperBound - lowerBound);
                
                x[i][j] = randomDoubleInRange(lowerBound, upperBound);
                pbest[i][j] = x[i][j];
                v[i][j] = randomDoubleInRange(
                    -velocityRange, 
                    velocityRange
                );
            }
            
            /* arbitrarily initialize gbest to the first individual */
            if (i == 0) {
                gbest = x[0];
                gbestFitness = objective.evaluate(gbest, 0);
            }
            
            pbestFitness[i] = objective.evaluate(x[i], 0);
            
            if (pbestFitness[i] < gbestFitness) {
                double[] individual = x[i];
                gbest = Arrays.copyOf(individual, dim);
            }
        }
    }
    
    public List<EpochPerformanceResult> runForIterations(int iterations) {
        final List<EpochPerformanceResult> results = new ArrayList<>(iterations);
        
        for(int epoch = 1; epoch <= iterations; epoch++) {
            for(int i = 0; i < populationSize; i++) {
                for(int d = 0; d < dim; d++) {
                    final double r1 = rng.nextDouble();
                    final double r2 = rng.nextDouble();
                    
                    v[i][d] = W*v[i][d] + C1*r1*(pbest[i][d] - x[i][d]) + C2*r2*(gbest[d] - x[i][d]);
                    x[i][d] = x[i][d] + v[i][d];
                }    
            }
            
            final int epochDummy = epoch;
            
            /* because each evaluation runs in parallel, we can easily just do a parallel map */
            
            List<Double> fitnessValues = Arrays.stream(x).parallel().map(position -> 
                    objective.evaluate(position, epochDummy)
            ).collect(
                    Collectors.toList()
            );
            
            for(int i = 0; i < fitnessValues.size(); i++) {
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
    
    private double randomDoubleInRange(double lowerBound, double upperBound) {
        return rng.nextDouble()*(upperBound-lowerBound) + lowerBound;
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
            {-5.12, 5.12},
        };
        
        /* simple 2D function with global minimum at (0, 0) */
        ObjectiveFunction sphere2d = (x, iteration) -> x[0] * x[0] + x[1] * x[1];
        
        /* more complex function with global minimum of f(0) = (0, 0, ..., 0). 
         * See <https://upload.wikimedia.org/wikipedia/commons/8/8b/Rastrigin_function.png> for a plot at n = 2. 
         */
        
        final int dim = 12;
        ObjectiveFunction rastrigin = (x, iteration) -> {
            /* ineffective but simple way to simulate a computationally intensive task */
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {}
            double sum = 0.0;
            for(int i = 0; i < dim; i++) {
                sum += x[i]*x[i] - 10*Math.cos(Math.PI*2*x[i]);
            }
            
            return 10*dim + sum;
        };
        
        ParticleSwarmOptimizer optimizer = new ParticleSwarmOptimizer(
            populationSize, 
            dim,
            bounds,
            rastrigin
        );
        
        optimizer.initializePopulation();
        
        optimizer.addEpochListener((result, epoch) -> {
            if (epoch % 10 == 0) {
                System.out.println("Epoch " + epoch + ": " + new Date());
                System.out.println("Best result: " + result.gbestFitness);
            }
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
