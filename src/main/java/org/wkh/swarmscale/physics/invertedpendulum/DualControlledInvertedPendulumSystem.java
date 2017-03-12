package org.wkh.swarmscale.physics.invertedpendulum;

import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;
import org.wkh.swarmscale.optimization.PIDController;

public class DualControlledInvertedPendulumSystem extends InvertedPendulumSystem {
    private final List<Pair<Double, Pair<PIDController, PIDController>>> controllers;
    
    private final double controlInterval;

    private final double initialRotation;
    private double currentRotation;
    private final double scheduleOffset;
    private double errorSum = 0.0;
    
    public static final double MAX_OUTPUT = 15.0;
    public DualControlledInvertedPendulumSystem(double[] position,
            double scheduleOffset,
            double controlInterval,
            double initialRotation) {
        this.controlInterval = controlInterval;
        this.initialRotation = initialRotation;
        this.scheduleOffset = scheduleOffset;
        
        final int controllerCount = position.length / (3*2);
        
        controllers = new ArrayList<>(controllerCount);
        
        for(int i = 0; i < position.length; i += 3*2) {
            
            final PIDController rotationController = new PIDController(position[i], position[i+1], position[i+2]);
            rotationController.setOutputLimits(MAX_OUTPUT);
            final PIDController positionController = new PIDController(position[i+3], position[i+4], position[i+5]);
            positionController.setOutputLimits(MAX_OUTPUT);
            
            final int controllerIndex = controllers.size();
            
            final double offsetUpper = scheduleOffset*(controllerIndex + 1);
            
            controllers.add(new Pair<>(offsetUpper, new Pair<>(rotationController, positionController)));
        }
    }

    public Pair<Double, Pair<PIDController, PIDController>> getControllerPair(final double cartDisplacement) {
        for(int i = 0; i < controllers.size(); i++) {
            Pair<Double, Pair<PIDController, PIDController>> pair = controllers.get(i);
            
            if (cartDisplacement <= pair.getKey()) {
                return pair;
            }
        }
        
        /* use the outermost one */
        return controllers.get(controllers.size() - 1);
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
        final long time = System.nanoTime();
        double timeSinceLastControlAction = (time - previousControlTime) / 1.0E6;

        if (timeSinceLastControlAction < controlInterval) {
            return;
        }
        previousControlTime = time;

        currentRotation = pole.getTransform().getRotation();
        
        final double cartPosition = cart.getTransform().getTranslationX();
        final double cartDisplacement = Math.abs(cartPosition);
        
        final Pair<Double, Pair<PIDController, PIDController>> effectiveControlPolicy = getControllerPair(cartDisplacement);
        final double positionSetpoint = cartPosition > 0 ? effectiveControlPolicy.getKey() - scheduleOffset : scheduleOffset -effectiveControlPolicy.getKey();
        final double cartError = Math.abs(cartDisplacement - positionSetpoint);
        
        final PIDController rotationController = effectiveControlPolicy.getValue().getKey();
        final PIDController positionController = effectiveControlPolicy.getValue().getValue();
        
        final double rotationOutput = rotationController.getOutput(currentRotation, 0.0);
        
        final double positionOutput = positionController.getOutput(cartPosition, positionSetpoint);
        
        cart.applyImpulse(new Vector2(rotationOutput + positionOutput, 0));

        //System.out.printf("time: %f, rotation: %f, rotation output: %f, cart position: %f, position output: %f, net output: %f\n", 
        //getElapsedTime(), currentRotation, rotationOutput, cartPosition, positionOutput, rotationOutput + positionOutput);

        errorSum += cartError + Math.abs(currentRotation) * 2.0;
    }
}
