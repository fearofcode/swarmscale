package org.wkh.swarmscale.physics.invertedpendulum.gp;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import org.wkh.swarmscale.physics.invertedpendulum.InvertedPendulumSystem;

public class GPControlledInvertedPendulumSystem extends InvertedPendulumSystem {
    private final double initialRotation;
    private final double initialPosition;

    public double cartPosition;
    public double cartDisplacement;
    public double cartVelocity;

    public double poleRotation;
    public double poleDisplacement;
    public double poleVelocity;

    private double previousCartPosition;
    private double errorSum = 0.0;
    public static final double MAX_OUTPUT = 24.0;
    public int hits;

    private double step;

    private GPForceController controller;

    public GPControlledInvertedPendulumSystem(double initialRotation, double initialPosition, GPForceController controller) {
        this.initialRotation = initialRotation;
        this.initialPosition = initialPosition;
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

        for (final Body body: new Body[] { pole, poleMass, cart}) {
            body.translate(initialPosition, 0.0);
        }
    }

    @Override
    protected void preSimulationStep() {
        cartPosition = cart.getTransform().getTranslationX();
        cartDisplacement = Math.abs(cartPosition);
        cartVelocity = cart.getLinearVelocity().getXComponent().getMagnitude();

        poleRotation = pole.getTransform().getRotation();
        poleDisplacement = Math.abs(poleRotation);
        poleVelocity = pole.getAngularVelocity();
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

        final double cartMovement = Math.abs(cartPosition - previousCartPosition);

        final double poleMovement = Math.abs(poleVelocity);

        final double output = Math.abs(force);
        final boolean failure = cart.isInContact(leftWall) || cart.isInContact(rightWall) || poleMass.isInContact(ground) || poleMass.isInContact(leftWall) || poleMass.isInContact(rightWall);

        if (!failure && poleDisplacement < Math.toRadians(0.0001) && cartMovement < 0.000001 && poleMovement < 0.00001 && output < 0.0001) {
            hits++;
        }

        errorSum += poleDisplacement + (cartDisplacement + cartMovement)*elapsedTime;

        if (failure) {
            errorSum += 50.0;
        }

        previousCartPosition = cartPosition;

    }
}
