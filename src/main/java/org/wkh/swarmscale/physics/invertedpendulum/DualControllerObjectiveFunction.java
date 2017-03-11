package org.wkh.swarmscale.physics.invertedpendulum;

import org.wkh.swarmscale.optimization.ObjectiveFunction;

/**
 * An experiment to try to control both cart position and pole rotation in order to combat the cart drifting to one
 * direction or another over time.
 */
public class DualControllerObjectiveFunction implements ObjectiveFunction  {
    @Override
    public double evaluate(double[] position, int iteration) {
        final int runTime = 20000; // fuck this

        final double rotationalProportionalGain = position[0];
        final double rotationalIntegralGain = position[1];
        final double rotationalDerivativeGain = position[2];
        
        final double positionProportionalGain = position[3];
        final double positionIntegralGain = position[4];
        final double positionDerivativeGain = position[5];
        
        final double controlInterval = 5.0;

        /* rotate the pole so that we have to take control action */
        final double initialRotation = -5.0;
        
        final DualControlledInvertedPendulumSystem system = new DualControlledInvertedPendulumSystem(
            rotationalProportionalGain,
            rotationalIntegralGain,
            rotationalDerivativeGain,
            positionProportionalGain,
            positionIntegralGain,
            positionDerivativeGain,
            controlInterval,
            initialRotation
        );

        system.initializeWorld();

        system.runSimulationLoop(runTime);
        return system.getErrorSum();
    }
    
}
