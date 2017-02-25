package org.wkh.swarmscale.physics.ballgravity;

import java.util.LinkedList;
import java.util.List;
import org.dyn4j.geometry.Vector2;
import org.wkh.swarmscale.optimization.ControlPerformanceResult;
import org.wkh.swarmscale.optimization.PIDController;

public class PIDControlledBallGravitySystem extends BallGravitySystem {
    public static final double initialTargetPosition = 0.0;
    
    private final PIDController controller;

    private double targetPosition;
    /* ms between control actions */
    private final double controlInterval;
    private long previousControlTime = 0;
    private final List<ControlPerformanceResult> observedErrors;

    private boolean verbose;

    public PIDControlledBallGravitySystem(double proportionalGain, double integralGain, double derivativeGain,
            double outputMagnitudeLimit, double controlInterval) {
        this.controlInterval = controlInterval;
        controller = new PIDController(proportionalGain, integralGain, derivativeGain);
        controller.setOutputLimits(outputMagnitudeLimit);

        observedErrors = new LinkedList<>();

        verbose = false;

        targetPosition = initialTargetPosition;
    }

    public synchronized double getTargetPosition() {
        return targetPosition;
    }

    public synchronized void setTargetPosition(double targetPosition) {
        this.targetPosition = targetPosition;
    }

    public List<ControlPerformanceResult> getObservedErrors() {
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

        final double elapsedTime = getElapsedTime();
        
        observedErrors.add(new ControlPerformanceResult(elapsedTime, targetPosition, currentPosition));
        
        double output = controller.getOutput(currentPosition, targetPosition);
        
        circle.applyImpulse(new Vector2(0, output));

        /* TODO turn this into a listener */
        
        if (verbose) {
            System.err.printf("%.2f\t%f\t%.10f\t%.5f\n", elapsedTime, targetPosition, currentPosition, output);
        }

        previousPosition = currentPosition;
        previousControlTime = time;
    }

    public static void main(String[] args) {
        System.err.println("Time\tTarget\tPosition\tOutput");
        int runTime = 8000;
        PIDControlledBallGravitySystem system = StableControllersFactory.stablePSODerivedSystem();

        system.setVerbose(true);
        system.initializeWorld();
        system.runSimulationLoop(runTime);
    }
}
