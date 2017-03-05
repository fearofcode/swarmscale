package org.wkh.swarmscale.physics.invertedpendulum;

import java.util.LinkedList;
import java.util.List;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;
import org.wkh.swarmscale.optimization.ControlPerformanceResult;
import org.wkh.swarmscale.optimization.PIDController;

public class DualControlledInvertedPendulumSystem extends InvertedPendulumSystem {
    private final PIDController rotationController;
    private final PIDController positionController;

    private final double controlInterval;
    private final List<ControlPerformanceResult> observedErrors;

    private boolean verbose;

    private final double initialRotation;
    private double currentRotation;

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
        
        observedErrors = new LinkedList<>();

        verbose = false;
        
        this.initialRotation = initialRotation;
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

        currentRotation = pole.getTransform().getRotation();
        
        final double elapsedTime = getElapsedTime();
        
        final double cartPosition = cart.getTransform().getTranslationX();
        
        double rotationOutput = rotationController.getOutput(currentRotation, 0.0);
        
        double positionOutput = positionController.getOutput(cartPosition, 0.0);
        cart.applyImpulse(new Vector2(rotationOutput, 0));
        cart.applyImpulse(new Vector2(positionOutput, 0));
        
        double cartPositionError = Math.abs(cartPosition) * 5.0;
        double rotationError = Math.abs(currentRotation);
        
        observedErrors.add(new ControlPerformanceResult(elapsedTime, 0.0, rotationError + cartPositionError));
        
        if (verbose) {
            System.err.printf("%.2f\t%f\t%f\t%f\t%f\t%f\n", elapsedTime, currentRotation, cartPosition, rotationOutput, positionOutput, cartPosition);
        }

        previousControlTime = time;
    }
}
