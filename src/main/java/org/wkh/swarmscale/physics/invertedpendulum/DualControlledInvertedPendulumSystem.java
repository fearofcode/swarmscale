package org.wkh.swarmscale.physics.invertedpendulum;

import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;
import org.wkh.swarmscale.optimization.PIDController;

public class DualControlledInvertedPendulumSystem extends InvertedPendulumSystem {
    private final PIDController rotationController;
    private final PIDController positionController;

    private final double controlInterval;

    private final double initialRotation;
    private double currentRotation;

    private double errorSum = 0.0;
    
    public DualControlledInvertedPendulumSystem(double rotationProportionalGain, 
            double rotationIntegralGain, 
            double rotationDerivativeGain,
            double positionProportionalGain, 
            double positionIntegralGain, 
            double positionDerivativeGain,
            double controlInterval,
            double initialRotation) {
        this.controlInterval = controlInterval;
        
        rotationController = new PIDController(rotationProportionalGain, rotationIntegralGain, rotationDerivativeGain);
        
        positionController = new PIDController(positionProportionalGain, positionIntegralGain, positionDerivativeGain);
        this.initialRotation = initialRotation;
        
    }

    public double getErrorSum() {
        return errorSum;
    }
    
    @Override
    protected void beforeSimulationLoopStart() {
        previousControlTime = System.nanoTime();
        
        Transform transform = new Transform();
        transform.setRotation(Math.toRadians(initialRotation));
        pole.setTransform(transform);
    }

    @Override
    protected void postSimulationStep() {
        long time = System.nanoTime();
        double timeSinceLastControlAction = (time - previousControlTime) / 1.0E6;

        if (timeSinceLastControlAction < controlInterval) {
            return;
        }
        previousControlTime = time;

        currentRotation = pole.getTransform().getRotation();
        
        final double cartPosition = cart.getTransform().getTranslationX();
        final double cartDisplacement = Math.abs(cartPosition);
        
        final double rotationOutput = rotationController.getOutput(currentRotation, 0.0);
        
        final double positionOutput = positionController.getOutput(cartPosition, 0.0);
        
        cart.applyImpulse(new Vector2(rotationOutput + positionOutput, 0));

        //System.out.printf("time: %f, rotation: %f, rotation output: %f, cart position: %f, position output: %f, net output: %f\n", 
        //getElapsedTime(), currentRotation, rotationOutput, cartPosition, positionOutput, rotationOutput + positionOutput);

        // emphasize rotation error
        final double rotationError = Math.abs(currentRotation) * 8.0;
        
        errorSum += cartDisplacement + rotationError;
        //controlCount++;
    }
}
