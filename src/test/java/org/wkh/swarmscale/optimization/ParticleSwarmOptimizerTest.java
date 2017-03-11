package org.wkh.swarmscale.optimization;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author stampy
 */
public class ParticleSwarmOptimizerTest {
    @Test
    public void testDistance() {
        double[] x = new double[] { 0.0 };
        double[] y = new double[] { 1.0 };
        double expResult = 1.0;
        double result = ParticleSwarmOptimizer.distance(x, y);
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of minimumPoint method, of class ParticleSwarmOptimizer.
     */
    @Test
    public void testMinimumPoint() {
        final int populationSize = 1;

        final double[][] bounds = {
            {-5.12, 5.12},
            {-5.12, 5.12},};


        ParticleSwarmOptimizer optimizer = new ParticleSwarmOptimizer(
                populationSize,
                2,
                bounds,
                0.01,
                0.2,
                (x, iteration) -> x[0] * x[0] + x[1] * x[1]
        );
        double[] expResult = new double[] {-5.12, -5.12};
        double[] result = optimizer.minimumPoint();
        assertArrayEquals(expResult, result, 0.00001);
        
    }

    /**
     * Test of maximumPoint method, of class ParticleSwarmOptimizer.
     */
    @Test
    public void testMaximumPoint() {
        final int populationSize = 1;

        final double[][] bounds = {
            {-5.12, 5.12},
            {-5.12, 5.12},};


        ParticleSwarmOptimizer optimizer = new ParticleSwarmOptimizer(
                populationSize,
                2,
                bounds,
                0.01,
                0.2,
                (x, iteration) -> x[0] * x[0] + x[1] * x[1]
        );
        double[] expResult = new double[] {5.12, 5.12};
        double[] result = optimizer.maximumPoint();
        assertArrayEquals(expResult, result, 0.00001);
    }

    /**
     * Test of boundsLength method, of class ParticleSwarmOptimizer.
     */
    @Test
    public void testBoundsLength() {
        final int populationSize = 1;

        final double[][] bounds = {
            {-1.0, 1.0},
            {-1.0, 1.0},};


        ParticleSwarmOptimizer optimizer = new ParticleSwarmOptimizer(
                populationSize,
                2,
                bounds,
                0.01,
                0.2,
                (x, iteration) -> x[0] * x[0] + x[1] * x[1]
        );
        double expResult = 2.0*Math.sqrt(2);
        double result = optimizer.boundsLength();
        assertEquals(result, expResult, 0.01);
    }
}
