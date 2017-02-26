package org.wkh.swarmscale.physics.invertedpendulum;

import java.util.LinkedList;
import java.util.List;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;
import org.wkh.swarmscale.optimization.ControlPerformanceResult;
import org.wkh.swarmscale.optimization.PIDController;
import org.wkh.swarmscale.physics.PhysicalSystemRenderer;

public class PIDControlledInvertedPendulumSystem extends InvertedPendulumSystem {
    private final PIDController controller;

    private final double controlInterval;
    private final List<ControlPerformanceResult> observedErrors;

    private boolean verbose;

    private final double initialRotation;
    private double currentRotation;
    
    public PIDControlledInvertedPendulumSystem(double proportionalGain, double integralGain, double derivativeGain,
            double outputMagnitudeLimit, double controlInterval, double initialRotation) {
        this.controlInterval = controlInterval;
        controller = new PIDController(proportionalGain, integralGain, derivativeGain);
        controller.setOutputLimits(outputMagnitudeLimit);

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

        currentRotation = Math.toDegrees(pole.getTransform().getRotation());

        final double elapsedTime = getElapsedTime();
        
        observedErrors.add(new ControlPerformanceResult(elapsedTime, 0.0, currentRotation));
        
        double output = controller.getOutput(currentRotation, 0.0);
        
        cart.applyImpulse(new Vector2(output, 0));

        /* TODO turn this into a listener, and maybe factor out an abstract class this and the ball gravity class 
        can share */
        
        if (verbose) {
            System.err.printf("%.2f\t%f\t%f\n", elapsedTime, currentRotation, output);
        }

        previousControlTime = time;
        
        if (pole.isInContact(ground) || cart.isInContact(leftWall) || cart.isInContact(rightWall)) {
            stop();
        }
    }

    public static void main(String[] args) {
        PIDControlledInvertedPendulumSystem system = new PIDControlledInvertedPendulumSystem(5.0, 0.1, 0.2, 5.0, 25.0, -1.0);
        system.setVerbose(true);
        
        PhysicalSystemRenderer window = new PhysicalSystemRenderer(system);

        window.setVisible(true);
        
        window.start();
    }
    
}
