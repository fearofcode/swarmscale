package org.wkh.swarmscale.physicalexample;

import java.util.List;
import org.wkh.swarmscale.optimization.ObjectiveFunction;

public class PIDControlledBallGravitySystemObjectiveFunction implements ObjectiveFunction {
    /**
     * Calculate the amount of time to spend simulating the ball system on a given iteration.
     * 
     * The idea is to start at a reasonable amount, then run longer and longer as the solutions become more refined.
     * 
     * This is intended to help test long-term stability while reducing the computational load that comes with.
     * 
     * Starts at 500, then goes to 510, 520, ..., up to a max of 5000
     * 
     * @param iteration The optimization epoch
     * @return milliseconds to simulate the system for
     */
    public static int getSimulationTime(final int iteration) {
        /* start at 500 ms, then run progressively longer until a max of 5 seconds is reached */
        
        return Math.min(Math.max((int)(iteration*10), 500), 5000);
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
        final double maxOutputMagnitude = position[3];
        final double controlInterval = position[4];
        
        final PIDControlledBallGravitySystem system = new PIDControlledBallGravitySystem(
                proportionalGain, 
                integralGain, 
                derivativeGain, 
                maxOutputMagnitude,
                controlInterval, 
                runTime);
        
        system.initializeWorld();
        
        system.runSimulationLoop(runTime);
        final List<Double> observedErrors = system.getObservedErrors();
        
        /* sum up the errors */

        return observedErrors.stream().mapToDouble(i -> i).sum();
    }
    
}
