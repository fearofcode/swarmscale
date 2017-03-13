package org.wkh.swarmscale.physics.invertedpendulum;

import org.dyn4j.geometry.Vector2;
import org.wkh.swarmscale.optimization.PIDController;

public class DualControlledInvertedPendulumSystem extends InvertedPendulumSystem {
    
    private final double controlInterval;

    private final double initialRotation;
    private double errorSum = 0.0;
    
    public static final double MAX_OUTPUT = 20.0;

    private final PIDController rotationController;

    public DualControlledInvertedPendulumSystem(double[] gains,
            double controlInterval,
            double initialRotation) {
        this.controlInterval = controlInterval;
        this.initialRotation = initialRotation;
        
        rotationController = new PIDController(gains[0], gains[1], gains[2]);
        rotationController.setOutputLimits(MAX_OUTPUT);
    }

    public double getErrorSum() {
        return errorSum;
    }
    
    @Override
    protected void beforeSimulationLoopStart() {
        previousControlTime = System.nanoTime();
        
        //cart.applyImpulse(new Vector2(initialRotation, 0.0));
        cart.rotate(Math.toRadians(initialRotation), 0.0, 0.0);
    }

    @Override
    protected void postSimulationStep() {
        final long time = System.nanoTime();
        double timeSinceLastControlAction = (time - previousControlTime) / 1.0E6;

        if (timeSinceLastControlAction < controlInterval) {
            return;
        }
        previousControlTime = time;

        final double currentRotation = pole.getTransform().getRotation();

        /*if (Math.abs(currentRotation) < 1.0E-3) {
            return;
        }
        */
        final double rotationOutput = rotationController.getOutput(currentRotation, 0.0);
        System.err.println(currentRotation + ", " + rotationOutput);
        cart.applyImpulse(new Vector2(rotationOutput, 0));

        errorSum += Math.abs(currentRotation);
    }
}
