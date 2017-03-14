package org.wkh.swarmscale.physics.invertedpendulum;

import org.wkh.swarmscale.optimization.ObjectiveFunction;

/**
 * An experiment to try to control both cart position and pole rotation in order to combat the cart drifting to one
 * direction or another over time.
 */
public class DualControllerObjectiveFunction implements ObjectiveFunction  {
    @Override
    public double evaluate(double[] position, int iteration) {
        final double runTime = 60.0;

        /* rotate the pole so that we have to take control action */
        final double initialRotation = -5.0;

        final DualControlledInvertedPendulumSystem system = new DualControlledInvertedPendulumSystem(
            position,
            initialRotation
        );

        system.initializeWorld();

        system.runDiscreteLoop(runTime);
        return system.getErrorSum();
    }
    
}
