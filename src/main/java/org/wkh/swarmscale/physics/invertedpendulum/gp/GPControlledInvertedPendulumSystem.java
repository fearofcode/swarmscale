package org.wkh.swarmscale.physics.invertedpendulum.gp;

import org.dyn4j.geometry.Vector2;
import org.wkh.swarmscale.physics.invertedpendulum.InvertedPendulumSystem;

public class GPControlledInvertedPendulumSystem extends InvertedPendulumSystem {
    private final double initialRotation;

    public double cartPosition;
    public double cartDisplacement;
    public double cartVelocity;
    public double cartAcceleration;

    public double poleRotation;
    public double poleDisplacement;
    public double poleVelocity;
    public double poleAcceleration;

    private double previousCartVelocity;
    private double previousPoleVelocity;

    private double errorSum = 0.0;
    public static final double MAX_OUTPUT = 24.0;

    private double step;

    private GPForceController controller;

    public GPControlledInvertedPendulumSystem(double initialRotation, GPForceController controller) {
        this.initialRotation = initialRotation;
        this.controller = controller;
    }

    public double getErrorSum() {
        return errorSum;
    }

    @Override
    protected void beforeSimulationLoopStart() {
        step = world.getSettings().getStepFrequency();
        pole.rotate(Math.toRadians(initialRotation));
        poleMass.rotate(Math.toRadians(initialRotation));
    }

    @Override
    protected void preSimulationStep() {
        cartPosition = cart.getTransform().getTranslationX();
        cartDisplacement = Math.abs(cartPosition);
        cartVelocity = cart.getLinearVelocity().getMagnitude();

        cartAcceleration = (cartVelocity - previousCartVelocity) / step;

        poleRotation = pole.getTransform().getRotation();
        poleDisplacement = Math.abs(poleRotation);
        poleVelocity = pole.getAngularVelocity();
        poleAcceleration = (poleVelocity - previousPoleVelocity) / step;

        previousCartVelocity = cartVelocity;
        previousPoleVelocity = poleVelocity;
    }

    @Override
    protected void postSimulationStep(double elapsedTime) {
        final double timeSinceLastControlAction = (elapsedTime - previousControlTime);

        if (timeSinceLastControlAction < step) {
            return;
        }

        final double force = controller.getForce(this);
        cart.applyImpulse(new Vector2(force, 0));

        previousControlTime = elapsedTime;

        errorSum += (poleDisplacement*5.0 + cartDisplacement)*elapsedTime;

        /*if (poleMass.isInContact(ground) || poleMass.isInContact(leftWall) || poleMass.isInContact(rightWall) || cart.isInContact(leftWall) || cart.isInContact(rightWall)) {
            errorSum += 1000.0;
        }*/
    }
}
