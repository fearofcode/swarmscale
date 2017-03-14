package org.wkh.swarmscale.physics.invertedpendulum;

import org.dyn4j.geometry.Vector2;
import org.wkh.swarmscale.optimization.PIDController;

public class DualControlledInvertedPendulumSystem extends InvertedPendulumSystem {

    private final double initialRotation;
    private double errorSum = 0.0;
    private double previousCartPosition = 0.0;
    public static final double MAX_OUTPUT = 1.0;

    private final PIDController rotationController;
    private final PIDController positionController;

    public DualControlledInvertedPendulumSystem(double[] gains,
            double initialRotation) {
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

        if (timeSinceLastControlAction < world.getSettings().getStepFrequency()) {
            return;
        }
        previousControlTime = elapsedTime;

        final double currentRotation = pole.getTransform().getRotation();

        final double rotationOutput = rotationController.getOutput(currentRotation, 0.0);
        final double cartPosition = cart.getTransform().getTranslationX();
        final double positionOutput = positionController.getOutput(cartPosition, 0.0);
        cart.applyImpulse(new Vector2(rotationOutput, 0));
        cart.applyImpulse(new Vector2(positionOutput, 0));

        if (elapsedTime < 1.0) { return; }

        errorSum += (Math.abs(currentRotation)*8.0 + Math.abs(cartPosition))*elapsedTime;

        /*
        if (poleMass.isInContact(ground) || cart.isInContact(leftWall) || cart.isInContact(rightWall) || poleMass.isInContact(leftWall) || poleMass.isInContact(rightWall)) {
            errorSum += 10000.0;
        }
        */
        previousCartPosition = cartPosition;
    }
}
