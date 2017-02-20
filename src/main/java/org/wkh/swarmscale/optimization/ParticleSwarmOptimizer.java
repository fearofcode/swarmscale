package org.wkh.swarmscale.optimization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
    private static final double w = 0.729;
    
    /**
     * Personal best coefficient
     */
    private static final double c1 = 1.494;
    
    /**
     * Global best coefficient
     */
    private static final double c2 = 1.494;
    
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
    
    /**
     * 
     * @param populationSize Number of individuals to create
     * @param bounds upper and lower bounds in each dimension. Should be 
     * `dim` x 2 in size.
     * @param objective Objective function. Needs to match bounds in the array
     * elements it references.
     */
    public ParticleSwarmOptimizer(
            final int populationSize, 
            final double[][] bounds,
            final ObjectiveFunction objective) {
        rng = new Random();
        
        this.populationSize = populationSize;
        
        this.objective = objective;
        
        this.bounds = bounds;
        
        dim = bounds.length;
        
        x = new double[populationSize][dim];
        v = new double[populationSize][dim];
        pbest = new double[populationSize][dim];
        pbestFitness = new double[populationSize];
        gbest = new double[dim];       
    }

    public void initializePopulation() {
        for(int i = 0; i < populationSize; i++) {
            for(int j = 0; j < dim; j++) {
                double lowerBound = bounds[i][0];
                double upperBound = bounds[i][1];
                double velocityRange = Math.abs(upperBound - lowerBound);
                
                x[i][j] = randomDoubleInRange(lowerBound, upperBound);
                pbest[i][j] = x[i][j];
                v[i][j] = randomDoubleInRange(
                    -velocityRange, 
                    velocityRange
                );
            }
            
            if (i == 0) {
                /* arbitrarily initialize gbest to the first individual */
                gbest = x[0];
                gbestFitness = objective.evaluate(gbest);
                pbestFitness[0] = gbestFitness;
                continue;
            }
            
            pbestFitness[i] = objective.evaluate(x[i]);
            
            if (pbestFitness[i] < gbestFitness) {
                double[] individual = x[i];
                gbest = Arrays.copyOf(individual, dim);
            }
        }
    }
    
    public List<EpochPerformanceResult> runForIterations(int iterations) {
        List<EpochPerformanceResult> results = new ArrayList<>(iterations);
        
        return results;
    }
    
    private double randomDoubleInRange(double lowerBound, double upperBound) {
        return rng.nextDouble()*(upperBound-lowerBound) + lowerBound;
    }
    
    public static void main(String[] args) {
        final int populationSize = 40;
        
        final double[][] bounds = {
            {-5.0, 5.0},
            {-5.0, 5.0},
        };
        
        /* simple 2D function with global minimum at (0, 0) */
        ObjectiveFunction sphere2d = (position) -> position[0] * position[0] + position[1] * position[1];
        
        ParticleSwarmOptimizer optimizer = new ParticleSwarmOptimizer(
            populationSize, 
            bounds,
            sphere2d
        );
    }
}
