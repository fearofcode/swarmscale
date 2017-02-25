package org.wkh.swarmscale.physics.ballgravity;

import java.util.ArrayList;
import java.util.List;
import org.dyn4j.geometry.Vector2;
import org.wkh.swarmscale.optimization.PIDController;

public class PIDControlledBallGravitySystem extends BallGravitySystem {

    private final PIDController controller;

    private double targetPosition;
    /* ms between control actions */
    private final double controlInterval;
    private long previousControlTime = 0;
    private final List<Double> observedErrors;

    private boolean verbose;
    private final boolean recordErrors;

    public PIDControlledBallGravitySystem(double proportionalGain, double integralGain, double derivativeGain,
            double outputMagnitudeLimit, double controlInterval, int expectedRuntime) {
        this.controlInterval = controlInterval;
        controller = new PIDController(proportionalGain, integralGain, derivativeGain);
        controller.setOutputLimits(outputMagnitudeLimit);
        final int capacity = (int) (expectedRuntime / controlInterval);

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

        if (timeSinceLastControlAction < controlInterval) {
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
            System.err.printf("%.2f\t%f\t%.10f\t%.5f\n", getElapsedTime(), targetPosition, currentPosition, output);
        }

        previousPosition = currentPosition;
        previousControlTime = time;
    }

    public static void main(String[] args) {
        System.err.println("Time\tTarget\tPosition\tOutput");
        int runTime = 8000;
        PIDControlledBallGravitySystem system = StableControllersFactory.stablePSODerivedSystem(runTime);

        system.setVerbose(true);
        system.initializeWorld();
        system.runSimulationLoop(runTime);
    }
}
