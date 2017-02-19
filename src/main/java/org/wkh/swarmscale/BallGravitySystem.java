package org.wkh.swarmscale;

import java.awt.Color;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Rectangle;
import org.dyn4j.geometry.Vector2;

/**
 * A simple physical system where a ball at the origin falls downward under the influence of gravity.
 * @author stampy
 */
public class BallGravitySystem implements PhysicalSystem {    
    /**
     * The conversion factor from nanometer to base
     */
    public static final double NANO_TO_BASE = 1.0e9;

    /**
     * The dynamics engine
     */
    private World world;

    /**
     * Whether the example is stopped or not
     */
    private boolean stopped = false;

    private RenderedBody circle;
    private RenderedBody floor;

    /**
     * The time stamp for the last iteration
     */
    private long last;

    private boolean doRender = false;
    private PhysicalSystemRenderer renderer = null;
    
    @Override
    public void setDoRender(boolean doRender) {
        this.doRender = doRender;
    }

    @Override
    public void setRenderer(PhysicalSystemRenderer renderer) {
        this.renderer = renderer;
    }
    
    
    @Override
    public World getWorld() {
        return world;
    }
    /**
     * Stops the example.
     */
    @Override
    public synchronized void stop() {
        stopped = true;
    }

    @Override
    public synchronized void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
    
    /**
     * Returns true if the example is stopped.
     *
     * @return boolean true if stopped
     */
    @Override
    public synchronized boolean isStopped() {
        return stopped;
    }
    
    /**
     * Creates game objects and adds them to the world.
     * <p>
     * Basically the same shapes from the Shapes test in the TestBed.
     */
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
        
        last = System.nanoTime();
    }
    
    @Override
    public void runSimulationLoop() {
        long start = System.currentTimeMillis();

        double lastPosition = circle.getTransform().getTranslationY();
        double startingPosition = lastPosition;

        // perform an infinite loop stopped
        // render as fast as possible
        while (!isStopped()) {
            if (doRender) {
                renderer.renderSystem();
            }
            
            stepWorld();
            
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - start;
            double currentPosition = circle.getTransform().getTranslationY();
            double positionDelta = currentPosition - startingPosition;

            if (currentPosition != lastPosition) {
                if (positionDelta < 0) {
                    /* produces a roughly stable position */
                    circle.applyImpulse(new Vector2(0, 0.129));
                }
                /* TODO replace all this shit with stepWorld listeners */
                System.out.println(elapsedTime + " " + positionDelta);
                lastPosition = currentPosition;

                if (circle.isInContact(floor)) {
                    break;
                }
            }
        }
    }
    
    private void stepWorld() {
        // update the World
        // get the current time
        long time = System.nanoTime();
        // get the elapsed time from the last iteration
        long diff = time - last;
        // set the last time
        last = time;
        // convert from nanoseconds to seconds
        double elapsedTime = diff / NANO_TO_BASE;
        // update the world with the elapsed time
        world.update(elapsedTime);
    }
    
    public static void main(String[] args) {
        PhysicalSystem system = new BallGravitySystem();
        system.initializeWorld();
        system.runSimulationLoop();
    }
}
