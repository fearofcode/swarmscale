package org.wkh.swarmscale.physicalexample;

import java.awt.Color;
import org.dyn4j.dynamics.BodyFixture;
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


    private double previousPosition;
    private double currentPosition;
    private double startingPosition;

    @Override
    public void initializeWorld() {
        super.initializeWorld();
        
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
    }

    @Override
    protected void beforeSimulationLoopStart() {
        previousPosition = circle.getTransform().getTranslationY();
        startingPosition = previousPosition;
    }

    @Override
    protected void postSimulationStep() {
        currentPosition = circle.getTransform().getTranslationY();
        double positionDelta = currentPosition - startingPosition;

        if (currentPosition == previousPosition) {
            return;
        }
        
        if (positionDelta < 0) {
            /* apply proportional control; produces a somewhat stable position albeit an oscillating one */
            circle.applyImpulse(new Vector2(0, 0.5*-positionDelta));
        }

        System.out.println(getElapsedTime() + " " + positionDelta);
        previousPosition = currentPosition;

        if (circle.isInContact(floor)) {
            System.out.println("Circle hit floor, stopping");
            stop();
        }
    }

    public static void main(String[] args) {
        PhysicalSystem system = new BallGravitySystem();
        system.initializeWorld();
        system.runSimulationLoop(10000);
    }
}
