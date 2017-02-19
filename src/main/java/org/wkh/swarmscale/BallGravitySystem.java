package org.wkh.swarmscale;

import java.awt.Color;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

/**
 * A simple physical system where a circle at the origin falls downward under the influence of gravity.
 */
public class BallGravitySystem extends AbstractPhysicalSystem {

    private RenderedBody circle;
    private RenderedBody floor;

    private long startTime;

    private double lastPosition;
    private double startingPosition;

    @Override
    public void initializeWorld() {
        // create the world
        world = new World();

        // create the floor
        Rectangle floorRect = new Rectangle(15.0, 1.0);
        floor = new RenderedBody(Color.DARK_GRAY);
        floor.addFixture(new BodyFixture(floorRect));
        floor.setMass(MassType.INFINITE);
        // move the floor down a bit
        floor.translate(0.0, -4.0);
        world.addBody(floor);

        // create a circle
        Circle cirShape = new Circle(0.5);
        circle = new RenderedBody(Color.BLACK);
        circle.addFixture(cirShape);
        circle.setMass(MassType.NORMAL);

        circle.setLinearDamping(0.05);
        world.addBody(circle);

        lastTime = System.nanoTime();
    }

    @Override
    protected void beforeSimulationLoopStart() {
        startTime = System.currentTimeMillis();

        lastPosition = circle.getTransform().getTranslationY();
        startingPosition = lastPosition;
    }

    @Override
    protected void postSimulationStep() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        double currentPosition = circle.getTransform().getTranslationY();
        double positionDelta = currentPosition - startingPosition;

        if (currentPosition != lastPosition) {
            if (positionDelta < 0) {
                /* produces a roughly stable position */
                circle.applyImpulse(new Vector2(0, 0.129));
            }

            System.out.println(elapsedTime + " " + positionDelta);
            lastPosition = currentPosition;

            if (circle.isInContact(floor)) {
                stop();
            }
        }
    }

    public static void main(String[] args) {
        PhysicalSystem system = new BallGravitySystem();
        system.initializeWorld();
        system.runSimulationLoop();
    }
}
