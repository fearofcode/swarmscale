package org.wkh.swarmscale.physics.invertedpendulum;

import org.wkh.swarmscale.optimization.ObjectiveFunction;

/**
 * An experiment to try to control both cart position and pole rotation in order to combat the cart drifting to one
 * direction or another over time.
 */
public class DualControllerObjectiveFunction implements ObjectiveFunction  {
    @Override
    public double evaluate(double[] position, int iteration) {
        final int runTime = 10000;
        final double controlInterval = 5.0;

        /* rotate the pole so that we have to take control action */
        final double initialRotation = -5.0;

        final DualControlledInvertedPendulumSystem system = new DualControlledInvertedPendulumSystem(
            position,
            controlInterval,
            initialRotation
        );

        system.initializeWorld();

        system.runContinuousLoop(runTime);
        return system.getErrorSum();
    }
    
}
