package org.wkh.swarmscale.physics.ballgravity;

import java.util.List;
import org.wkh.swarmscale.optimization.ObjectiveFunction;

public class PIDControlledBallGravitySystemObjectiveFunction implements ObjectiveFunction {

    public static int getSimulationTime(final int iteration) {
        return 5000;
    }

    /**
     * Use the given candidate solution to run a PID-controlled ball gravity system simulation.
     *
     * @param position - parameter set to evaluate
     * @param iteration - optimization epoch
     * @return
     */
    @Override
    public double evaluate(final double[] position, final int iteration) {
        final int runTime = getSimulationTime(iteration);

        final double proportionalGain = position[0];
        final double integralGain = position[1];
        final double derivativeGain = position[2];
        final double maxOutputMagnitude = 25.0;
        final double controlInterval = 25.0;

        final PIDControlledBallGravitySystem system = new PIDControlledBallGravitySystem(
                proportionalGain,
                integralGain,
                derivativeGain,
                maxOutputMagnitude,
                controlInterval,
                runTime);

        system.initializeWorld();

        /* change up the setpoint part way through to test adjustment */
        system.addStepListener(() -> {
            if (system.getElapsedTime() >= 1000.0 && system.getTargetPosition() == 0.0) {
                system.setTargetPosition(1.0);
            }
        });

        system.runSimulationLoop(runTime);
        final List<Double> observedErrors = system.getObservedErrors();

        /* sum up the errors */
        return observedErrors.stream().mapToDouble(i -> i).sum();
    }

}
