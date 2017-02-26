package org.wkh.swarmscale.physics.invertedpendulum;

import java.util.List;
import org.wkh.swarmscale.optimization.ControlPerformanceResult;
import org.wkh.swarmscale.optimization.ObjectiveFunction;

public class MinimalAbsoluteErrorObjectiveFunction implements ObjectiveFunction {
    public static int getSimulationTime(final int iteration) {
        return 3000;
    }

    /**
     * Use the given candidate solution to run a PID-controlled inverted pendulum simulation.
     *
     * @param position - parameter set to evaluate
     * @param iteration - optimization epoch
     * @return
     */
    @Override
    public double evaluate(final double[] position, final int iteration) {
        final int runTime = getSimulationTime(iteration);

        final double proportionalGain = position[0];
        final double integralGain = 0.0; // make controller PD only
        final double derivativeGain = position[1];
        final double maxOutputMagnitude = 10.0;
        final double controlInterval = 25.0;

        /* rotate the pole so that we have to take control action */
        final double initialRotation = -15.0;
        
        final PIDControlledInvertedPendulumSystem system = new PIDControlledInvertedPendulumSystem(
            proportionalGain,
            integralGain,
            derivativeGain,
            maxOutputMagnitude,
            controlInterval,
            initialRotation
        );

        system.initializeWorld();

        system.runSimulationLoop(runTime);
        final List<ControlPerformanceResult> observedErrors = system.getObservedErrors();

        /* sum up the errors and add in max cart position */
        return observedErrors.stream().mapToDouble(result -> result.error).sum() + system.getMaxCartPosition();
    }
}
