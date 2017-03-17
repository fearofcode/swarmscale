package org.wkh.swarmscale.physics.invertedpendulum;

import java.awt.Color;
import org.dyn4j.dynamics.*;
import org.dyn4j.dynamics.joint.*;
import org.dyn4j.geometry.*;
import org.wkh.swarmscale.optimization.PIDController;
import org.wkh.swarmscale.physics.AbstractPhysicalSystem;
import org.wkh.swarmscale.physics.PhysicalSystem;
import org.wkh.swarmscale.physics.PhysicalSystemRenderer;
import org.wkh.swarmscale.physics.RenderableBody;

public class InvertedPendulumSystem extends AbstractPhysicalSystem {
    protected double previousControlTime = 0;
    
    protected RenderableBody ground;
    protected RenderableBody cart;
    protected RenderableBody pole;
    protected RenderableBody poleMass;
    protected RenderableBody leftWall;
    protected RenderableBody rightWall;

    public static final double CART_MASS = 1.0;
    public static final double POLE_MASS = 0.2;
    public static final double POLE_INERTIA = 0.01;
    public static final double POLE_LENGTH = 2.0;

    @Override
    public void initializeWorld() {
        super.initializeWorld();

        // Left Wall
        leftWall = new RenderableBody(Color.BLACK);
        {// Fixture5
            Convex c = Geometry.createRectangle(1.0, 5.0);
            c.translate(new Vector2(-12.0, 1.5));
            BodyFixture bf = new BodyFixture(c);
            leftWall.addFixture(bf);
        }
        leftWall.setMass(MassType.INFINITE);
        world.addBody(leftWall);

        // Right Wall
        rightWall = new RenderableBody(Color.BLACK);
        {// Fixture6
            Convex c = Geometry.createRectangle(1.0, 5.0);
            c.translate(new Vector2(12.0, 1.5));
            BodyFixture bf = new BodyFixture(c);
            rightWall.addFixture(bf);
        }
        rightWall.setMass(MassType.INFINITE);
        world.addBody(rightWall);

        // ground
        ground = new RenderableBody(Color.BLACK);
        {// Fixture5
            Convex c = Geometry.createRectangle(25.0, 1.0);
            c.translate(new Vector2(0.0, -1.0));
            BodyFixture bf = new BodyFixture(c);
            ground.addFixture(bf);
        }
        ground.translate(new Vector2(0.0, -0.5));
        ground.setMass(MassType.INFINITE);
        ground.setLinearDamping(0.0);
        world.addBody(ground);

        // cart
        cart = new RenderableBody(Color.BLACK);
        {// Fixture6
            Convex c = Geometry.createRectangle(1.0, 1.0);
            c.translate(new Vector2(0.0, -0.5));
            BodyFixture bf = new BodyFixture(c);
            cart.addFixture(bf);
        }
        cart.setMass(MassType.NORMAL);
        world.addBody(cart);

        // pole
        pole = new RenderableBody(Color.BLACK);
        {// Fixture7
            Convex c = Geometry.createRectangle(0.1, POLE_LENGTH);
            c.translate(new Vector2(0.0, 1.0));
            BodyFixture bf = new BodyFixture(c);
            pole.addFixture(bf);
        }
        pole.setMass(new Mass(new Vector2(0.0, 1.0), 0.01, POLE_INERTIA));
        pole.setMassType(MassType.NORMAL);
        world.addBody(pole);

        // pole mass
        poleMass = new RenderableBody(Color.BLACK);
        {// Fixture8
            Convex c = Geometry.createCircle(0.15);
            c.translate(new Vector2(0.0, 2.0));
            BodyFixture bf = new BodyFixture(c);
            poleMass.addFixture(bf);
        }
        poleMass.setMass(new Mass(new Vector2(0.0, 2.0), POLE_MASS, 0.01));
        poleMass.setMassType(MassType.NORMAL);
        world.addBody(poleMass);

        // PrismaticJoint4
        PrismaticJoint joint1 = new PrismaticJoint(ground, cart, new Vector2(0.0, -0.5), new Vector2(1.0, 0.0));
        joint1.setLimitEnabled(false);
        joint1.setLimits(0.0, 0.0);
        joint1.setReferenceAngle(Math.toRadians(0.0));
        joint1.setMotorEnabled(false);
        joint1.setMotorSpeed(0.0);
        joint1.setMaximumMotorForce(0.0);
        joint1.setCollisionAllowed(false);
        world.addJoint(joint1);
        // WeldJoint2
        WeldJoint joint2 = new WeldJoint(pole, poleMass, new Vector2(0.0, 2.0));
        joint2.setFrequency(0.0);
        joint2.setDampingRatio(0.0);
        joint2.setReferenceAngle(Math.toRadians(0.0));
        joint2.setCollisionAllowed(false);
        world.addJoint(joint2);
        // RevoluteJoint3
        RevoluteJoint joint3 = new RevoluteJoint(cart, pole, new Vector2(0.0, 0.0));
        joint3.setLimitEnabled(false);
        joint3.setLimits(Math.toRadians(0.0), Math.toRadians(0.0));
        joint3.setReferenceAngle(Math.toRadians(0.0));
        joint3.setMotorEnabled(false);
        joint3.setMotorSpeed(Math.toRadians(0.0));
        joint3.setMaximumMotorTorque(0.0);
        joint3.setCollisionAllowed(false);
        world.addJoint(joint3);

    }
    
    @Override
    protected void beforeSimulationLoopStart() {
        //pole.rotate(Math.toRadians(-15.0), 0.0, 0.0);
        cart.applyImpulse(new Vector2(-1.0, 0.0));
        //printState();
    }

    @Override
    protected void postSimulationStep(double elapsedTime) {
        double timeSinceLastControlAction = (elapsedTime - previousControlTime);

        if (timeSinceLastControlAction < world.getSettings().getStepFrequency()) {
            return;
        }

        if (poleMass.isInContact(ground)) {
            System.out.println("stopping");
            stop();
        }

        PIDController rotationController = new PIDController(1.0, 0.00, 0.0);

        final double currentRotation = pole.getTransform().getRotation();
        final double rotationOutput = rotationController.getOutput(currentRotation, 0.0);

        System.err.println(elapsedTime + "\t" + pole.getTransform().getRotation());

        cart.applyImpulse(new Vector2(rotationOutput, 0));

        previousControlTime = elapsedTime;
    }

    private void printState() {
        assert(cart.getMass().getMass() == CART_MASS);
        assert(pole.getMass().getMass() == POLE_MASS);
        double currentRotation = pole.getTransform().getRotation();

        //cart.applyImpulse(new Vector2(-currentRotation, 0));

        System.err.println("Rotation: " + Math.toDegrees(currentRotation));
        System.err.println("Rotation velocity: " + pole.getAngularVelocity());
        System.err.println("Position: " + cart.getTransform().getTranslationX());
        System.err.println("Position velocity: " + pole.getLinearVelocity().getMagnitude());
        System.err.println();
    }

    public static void main(String[] args) {
        InvertedPendulumSystem system = new InvertedPendulumSystem();
        system.initializeWorld();

        long start = System.nanoTime();
        system.runDiscreteLoop(10.0);
        //system.runContinuousLoop(10.0);

        long elapsed = System.nanoTime() - start;
        double milliseconds = elapsed / 1.0E6;
        System.err.println("ms: " + milliseconds);
    }
}
