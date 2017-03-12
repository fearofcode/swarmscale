package org.wkh.swarmscale.physics.invertedpendulum;

import org.wkh.swarmscale.optimization.ObjectiveFunction;

/**
 * An experiment to try to control both cart position and pole rotation in order to combat the cart drifting to one
 * direction or another over time.
 */
public class DualControllerObjectiveFunction implements ObjectiveFunction  {
    @Override
    public double evaluate(double[] position, int iteration) {
        final int runTime = 20000;
        final double controlInterval = 25.0;

        /* rotate the pole so that we have to take control action */
        final double initialRotation = -5.0;
        
        final double scheduleOffset = 0.25;
        
        if (position.length % (3*2) != 0) {
            throw new IllegalArgumentException("Must provide a list of (rotation, position) gains");
        }
        
        final DualControlledInvertedPendulumSystem system = new DualControlledInvertedPendulumSystem(
            position,
            scheduleOffset,
            controlInterval,
            initialRotation
        );

        system.initializeWorld();

        system.runSimulationLoop(runTime);
        return system.getErrorSum();
    }
    
}
