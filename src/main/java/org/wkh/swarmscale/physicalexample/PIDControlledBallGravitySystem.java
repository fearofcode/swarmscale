package org.wkh.swarmscale.physicalexample;

import java.util.ArrayList;
import java.util.List;
import org.dyn4j.geometry.Vector2;
import org.wkh.swarmscale.optimization.PIDController;

public class PIDControlledBallGravitySystem extends BallGravitySystem {
    private final PIDController controller;
    private static final double TARGET_POSITION = 0.0; /* want to try to keep the object at the origin */
    private final double controlInterval; /* ms between control actions */
    private long previousControlTime = 0;
    private final List<Double> observedErrors;

    private boolean verbose;
    private boolean recordErrors;
    
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
            observedErrors.add(Math.abs(currentPosition - TARGET_POSITION));
        }
        
        double output = controller.getOutput(currentPosition, TARGET_POSITION);
        
        circle.applyImpulse(new Vector2(0, output));
        
        if (verbose) {
            System.err.printf("%3.2f\t%3.2f\t%3.2f\n", getElapsedTime(), currentPosition, output);
        }
        
        previousPosition = currentPosition;
        previousControlTime = time;
    }
    
    public static void main(String[] args) {
        System.err.println("Time\tPosition\tOutput");
        /* arbitrary parameters, doesn't actually produce desired outcome */
        int runTime = 10000;
        PhysicalSystem system = new PIDControlledBallGravitySystem(0.4, 0.01, 0.0, 1, 20, runTime);
        system.initializeWorld();
        system.runSimulationLoop(runTime);
    }
}
