package org.wkh.swarmscale.physics.invertedpendulum.gp;

import com.sun.xml.internal.ws.policy.spi.PolicyAssertionValidator;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import org.wkh.swarmscale.physics.invertedpendulum.InvertedPendulumSystem;
import org.wkh.swarmscale.physics.invertedpendulum.gp.problem.InvertedPendulumControlProblem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GPControlledInvertedPendulumSystem extends InvertedPendulumSystem {
    private double initialRotation;
    private double initialPosition;

    private double cartPosition;
    private double cartDisplacement;
    private double cartVelocity;

    private double poleRotation;
    private double poleDisplacement;
    private double poleVelocity;

    // you mad, hater? you mad?
    public final List<StateObservation> states;

    public static final double MAX_OUTPUT = 24.0;

    private double step;

    private GPForceController controller;

    private final FitnessCalculator calculator;

    public StateObservation currentState;

    public GPControlledInvertedPendulumSystem(double initialRotation, double initialPosition, GPForceController controller, FitnessCalculator calculator) {
        this.initialRotation = initialRotation;
        this.initialPosition = initialPosition;
        this.controller = controller;
        this.calculator = calculator;

        states = new ArrayList<>(InvertedPendulumControlProblem.STEPS);

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

        currentState = new StateObservation(
                cartPosition, cartDisplacement, cartVelocity, poleRotation, poleDisplacement, poleVelocity
        );

        states.add(currentState);

        if (calculator.shouldStop(currentState)) {
            stop();
        }
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
    }
}
