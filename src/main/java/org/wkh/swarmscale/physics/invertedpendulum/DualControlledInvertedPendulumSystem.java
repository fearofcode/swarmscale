package org.wkh.swarmscale.physics.invertedpendulum;

import org.dyn4j.geometry.Vector2;
import org.wkh.swarmscale.optimization.PIDController;

public class DualControlledInvertedPendulumSystem extends InvertedPendulumSystem {
    
    private final double controlInterval;

    private final double initialRotation;
    private double errorSum = 0.0;
    
    public static final double MAX_OUTPUT = 1.0;

    private final PIDController rotationController;
    private final PIDController positionController;

    public DualControlledInvertedPendulumSystem(double[] gains,
            double controlInterval,
            double initialRotation) {
        this.controlInterval = controlInterval;
        this.initialRotation = initialRotation;
        
        rotationController = new PIDController(gains[0], gains[1], gains[2]);
        rotationController.setOutputLimits(MAX_OUTPUT);
        positionController = new PIDController(gains[3], gains[4], gains[5]);
        positionController.setOutputLimits(MAX_OUTPUT);
    }

    public double getErrorSum() {
        return errorSum;
    }
    
    @Override
    protected void beforeSimulationLoopStart() {
        pole.rotate(Math.toRadians(initialRotation));
        poleMass.rotate(Math.toRadians(initialRotation));
    }

    @Override
    protected void postSimulationStep(double elapsedTime) {
        final double timeSinceLastControlAction = (elapsedTime - previousControlTime);

        if (timeSinceLastControlAction < controlInterval) {
            return;
        }
        previousControlTime = elapsedTime;

        final double currentRotation = pole.getTransform().getRotation();

        final double rotationOutput = rotationController.getOutput(currentRotation, 0.0);
        final double cartPosition = cart.getTransform().getTranslationX();
        final double positionOutput = positionController.getOutput(cartPosition, 0.0);
        //System.err.println(Math.toDegrees(currentRotation) + ", " + rotationOutput);
        cart.applyImpulse(new Vector2(rotationOutput + positionOutput, 0));
        //System.err.println(Math.toDegrees(currentRotation) + ", " + rotationOutput);
        //cart.applyImpulse(new Vector2(rotationOutput, 0));

        errorSum += Math.abs(currentRotation) + Math.abs(cartPosition)*5; // + 0.1*(Math.abs(rotationOutput) + Math.abs(positionOutput));

        if (poleMass.isInContact(ground) || poleMass.isInContact(leftWall) || poleMass.isInContact(rightWall)) {
            errorSum += 10000.0;
        }
    }
}
