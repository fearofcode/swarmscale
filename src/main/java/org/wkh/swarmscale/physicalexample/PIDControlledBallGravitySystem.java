package org.wkh.swarmscale.physicalexample;

import org.dyn4j.geometry.Vector2;
import org.wkh.swarmscale.optimization.PIDController;

public class PIDControlledBallGravitySystem extends BallGravitySystem {
    private final PIDController controller;
    private static final double TARGET_POSITION = 0.0; /* want to try to keep the object at the origin */
    private double controlInterval; /* ms between control actions */
    private long previousControlTime = 0;
    
    public PIDControlledBallGravitySystem(double proportionalGain, double integralGain, double derivativeGain, 
            double outputMagnitudeLimit, double controlInterval) {
        this.controlInterval = controlInterval;
        controller = new PIDController(proportionalGain, integralGain, derivativeGain);
        controller.setOutputLimits(outputMagnitudeLimit);
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
        
        double output = controller.getOutput(currentPosition, TARGET_POSITION);
        
        circle.applyImpulse(new Vector2(0, output));
        
        System.err.printf("%3.2f\t%3.2f\t%3.2f\n", getElapsedTime(), currentPosition, output);
        
        previousPosition = currentPosition;
        previousControlTime = time;
    }
    
    public static void main(String[] args) {
        System.err.println("Time\tPosition\tOutput");
        /* arbitrary parameters, doesn't actually produce desired outcome */
        PhysicalSystem system = new PIDControlledBallGravitySystem(0.4, 0.01, 0.0, 1, 20);
        system.initializeWorld();
        system.runSimulationLoop(10000);
    }
}
