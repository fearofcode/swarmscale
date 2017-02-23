package org.wkh.swarmscale.physicalexample;

import java.util.ArrayList;
import java.util.List;
import org.dyn4j.geometry.Vector2;
import org.wkh.swarmscale.optimization.PIDController;

public class PIDControlledBallGravitySystem extends BallGravitySystem {
    private final PIDController controller;

    private double targetPosition;
    private final double controlInterval; /* ms between control actions */
    private long previousControlTime = 0;
    private final List<Double> observedErrors;

    private boolean verbose;
    private final boolean recordErrors;

    public PIDControlledBallGravitySystem(double proportionalGain, double integralGain, double derivativeGain,
            double outputMagnitudeLimit, double controlInterval, int expectedRuntime) {
        this.controlInterval = controlInterval;
        controller = new PIDController(proportionalGain, integralGain, derivativeGain);
        controller.setOutputLimits(outputMagnitudeLimit);
        final int capacity = (int) (expectedRuntime/controlInterval);

        recordErrors = capacity > 0;

        if (recordErrors) {
            observedErrors = new ArrayList<>(capacity);
        } else {
            observedErrors = new ArrayList<>();
        }

        verbose = false;
        
        targetPosition = 0.0;
    }

    public synchronized double getTargetPosition() {
        return targetPosition;
    }
    
    public synchronized void setTargetPosition(double targetPosition) {
        this.targetPosition = targetPosition;
    }
    
    public List<Double> getObservedErrors() {
        return observedErrors;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    protected void beforeSimulationLoopStart() {
        previousControlTime = System.nanoTime();
    }

    @Override
    protected void postSimulationStep() {
        long time = System.nanoTime();
        double timeSinceLastControlAction = (time - previousControlTime) / 1.0E6;

        if(timeSinceLastControlAction < controlInterval) {
            return;
        }

        currentPosition = circle.getTransform().getTranslationY();

        if (recordErrors) {
            final double error = Math.abs(currentPosition - targetPosition);
            observedErrors.add(error);
        }

        double output = controller.getOutput(currentPosition, targetPosition);

        circle.applyImpulse(new Vector2(0, output));

        if (verbose) {
            System.err.printf("%3.2f\t%.10f\t%3.2f\n", getElapsedTime(), currentPosition, output);
        }

        previousPosition = currentPosition;
        previousControlTime = time;
    }

    public static PIDControlledBallGravitySystem stableSystem(int runTime) {
        /* derived from running PIDControlledBallGravitySystemOptimizer. stable as long as setpoint is not changed */
         return new PIDControlledBallGravitySystem(
                24.03976158272401, 10.033777480148322, 44.150628224180416,
                10.0,
                25,
                runTime
         );
    }

    public static void main(String[] args) {
        System.err.println("Time\tPosition\tOutput");
        int runTime = 10000;
        PIDControlledBallGravitySystem system = PIDControlledBallGravitySystem.stableSystem(runTime);
        system.setVerbose(true);
        system.initializeWorld();
        system.runSimulationLoop(runTime);
    }
}
