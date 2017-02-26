package org.wkh.swarmscale.physics.invertedpendulum;

import java.awt.Color;
import org.dyn4j.dynamics.*;
import org.dyn4j.dynamics.joint.*;
import org.dyn4j.geometry.*;
import org.wkh.swarmscale.physics.AbstractPhysicalSystem;
import org.wkh.swarmscale.physics.PhysicalSystem;
import org.wkh.swarmscale.physics.PhysicalSystemRenderer;
import org.wkh.swarmscale.physics.RenderableBody;

public class InvertedPendulumSystem extends AbstractPhysicalSystem {
    protected long previousControlTime = 0;
    
    protected RenderableBody ground;
    protected RenderableBody cart;
    protected RenderableBody pole;
    protected RenderableBody leftWall;
    protected RenderableBody rightWall;

    @Override
    public void initializeWorld() {
        super.initializeWorld();

        // Ground
        ground = new RenderableBody(Color.DARK_GRAY);
        {// Fixture1
            Convex c = Geometry.createRectangle(15.0, 1.0);
            BodyFixture bf = new BodyFixture(c);
            ground.addFixture(bf);
        }
        ground.translate(new Vector2(0.0, -0.5));
        ground.setMass(MassType.INFINITE);
        ground.setLinearDamping(4.0);
        world.addBody(ground);

        // Cart
        cart = new RenderableBody(Color.LIGHT_GRAY);
        {// Fixture2
            Convex c = Geometry.createRectangle(1.0, 1.0);
            BodyFixture bf = new BodyFixture(c);
            cart.addFixture(bf);
        }
        cart.translate(new Vector2(0.0, 0.5));
        cart.setMass(MassType.NORMAL);
        cart.setLinearDamping(0.2);
        world.addBody(cart);

        // Pole
        pole = new RenderableBody(Color.RED);
        {// Fixture4
            Convex c = Geometry.createRectangle(0.25, 2.0);
            BodyFixture bf = new BodyFixture(c);
            pole.addFixture(bf);
        }
        pole.translate(new Vector2(0.0, 1.75));
        pole.setMass(MassType.NORMAL);
        world.addBody(pole);

        // Left Wall
        leftWall = new RenderableBody(Color.DARK_GRAY);
        {// Fixture5
            Convex c = Geometry.createRectangle(1.0, 5.0);
            c.translate(new Vector2(-7.0, 2.0));
            BodyFixture bf = new BodyFixture(c);
            leftWall.addFixture(bf);
        }
        leftWall.setMass(MassType.INFINITE);
        world.addBody(leftWall);

        // Right Wall
        rightWall = new RenderableBody(Color.DARK_GRAY);
        {// Fixture6
            Convex c = Geometry.createRectangle(1.0, 5.0);
            c.translate(new Vector2(7.0, 2.0));
            BodyFixture bf = new BodyFixture(c);
            rightWall.addFixture(bf);
        }
        rightWall.setMass(MassType.INFINITE);
        world.addBody(rightWall);

        // PrismaticJoint1
        PrismaticJoint joint1 = new PrismaticJoint(ground, cart, new Vector2(0.0, -0.5), new Vector2(1.0, 0.0));
        joint1.setLimitEnabled(false);
        joint1.setLimits(0.0, 0.0);
        joint1.setReferenceAngle(Math.toRadians(0.0));
        joint1.setMotorEnabled(true);
        joint1.setMotorSpeed(180.0);
        joint1.setMaximumMotorForce(0.0);
        joint1.setCollisionAllowed(false);
        world.addJoint(joint1);
        
        // RevoluteJoint2
        RevoluteJoint joint2 = new RevoluteJoint(cart, pole, new Vector2(0.0, 0.875));
        joint2.setLimitEnabled(false);
        joint2.setLimits(Math.toRadians(0.0), Math.toRadians(0.0));
        joint2.setReferenceAngle(Math.toRadians(0.0));
        joint2.setMotorEnabled(false);
        joint2.setMotorSpeed(Math.toRadians(0.0));
        joint2.setMaximumMotorTorque(0.0);
        joint2.setCollisionAllowed(false);
        world.addJoint(joint2);
    }
    
    @Override
    protected void beforeSimulationLoopStart() {
        previousControlTime = System.nanoTime();
        Transform transform = new Transform();
        transform.setRotation(Math.toRadians(-1.0));
        pole.setTransform(transform);
    }

    @Override
    protected void postSimulationStep() {
        long time = System.nanoTime();
        double timeSinceLastControlAction = (time - previousControlTime) / 1.0E6;

        if (timeSinceLastControlAction < 25.0) {
            return;
        }
        double currentRotation = pole.getTransform().getRotation();

        final double elapsedTime = getElapsedTime();
        
        final double proportionalGain = 1.0;
        double output = - (proportionalGain * currentRotation);
        
        cart.applyImpulse(new Vector2(output, 0));

        System.err.printf("%.2f\t%f\n", elapsedTime, currentRotation);

        previousControlTime = time;
        
        if (pole.isInContact(ground)) {
            stop();
        }
    }
    
    public static void main(String[] args) {
        PhysicalSystem system = new InvertedPendulumSystem();
        
        PhysicalSystemRenderer window = new PhysicalSystemRenderer(system);

        window.setVisible(true);
        
        window.start();
    }
}
