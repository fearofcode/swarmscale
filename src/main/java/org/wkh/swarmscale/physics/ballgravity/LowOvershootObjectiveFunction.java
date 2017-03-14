package org.wkh.swarmscale.physics.ballgravity;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.wkh.swarmscale.optimization.ControlPerformanceResult;
import org.wkh.swarmscale.optimization.ObjectiveFunction;

public class LowOvershootObjectiveFunction implements ObjectiveFunction {

    public static int getSimulationTime(final int iteration) {
        return 5000;
    }

    /**
     * Use the given candidate solution to run a PID-controlled ball gravity system simulation.
     *
     * Here is the difference between this function and MinimalAbsoluteErrorObjectiveFunction:
     * 
     * This function gives the system a grace period after the set point is changed by excluding errors for a certain 
     * period of time while the system moves the ball up to the new setpoint. During this grace period, actual positions
     * less than the new setpoint are not counted as errors. After the grace period is over, errors are counted as usual
     * so that the system cannot get away with never moving the ball.
     * 
     * The grace period effectively acts as our desired settling time.
     * 
     * @param position - parameter set to evaluate
     * @param iteration - optimization epoch
     * @return sum of squared error, excluding grace period results described above
     */
    @Override
    public double evaluate(final double[] position, final int iteration) {
        final int runTime = getSimulationTime(iteration);

        final double gracePeriod = 200.0;
        final double setPointChangeTime = 1000.0;
        final double newSetPoint = 1.0;
        final double gracePeriodEnd = setPointChangeTime + gracePeriod;
        
        /* needed for grace period filtering logic below to work */
        assert (newSetPoint > PIDControlledBallGravitySystem.INITIAL_TARGET_POSITION);
        
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
                controlInterval);

        system.initializeWorld();

        /* change up the setpoint part way through to test adjustment */
        system.addStepListener(() -> {
            if (system.getElapsedTime() >= setPointChangeTime && 
                    system.getTargetPosition() == PIDControlledBallGravitySystem.INITIAL_TARGET_POSITION) {
                system.setTargetPosition(newSetPoint);
            }
        });

        system.runContinuousLoop(runTime);
        final List<ControlPerformanceResult> observedErrors = system.getObservedErrors();
        
        /* select results that are: (a) before set point change; (b) after set point change; or (c), during grace period, 
           but with actual < target (so that it's still moving the object up to the new set point
        */
        Predicate<ControlPerformanceResult> nonGracePeriodResults = result -> {
            final boolean duringSetpointButNotOvershoot = (result.time >= setPointChangeTime) && 
                    (result.time <= gracePeriodEnd) && 
                    (result.actual < result.target);
            return result.time < setPointChangeTime || result.time > gracePeriodEnd || duringSetpointButNotOvershoot;
        };
        
        final Stream<ControlPerformanceResult> filteredResults = observedErrors.stream().filter(nonGracePeriodResults);

        return filteredResults.mapToDouble(result -> result.error * result.error).sum();
    }

}
