package org.wkh.swarmscale;

import org.dyn4j.dynamics.World;

public abstract class AbstractPhysicalSystem implements PhysicalSystem {

    /**
     * The conversion factor from nanometer to base
     */
    public static final double NANO_TO_BASE = 1.0e9;

    /**
     * The dynamics engine
     */
    protected World world;

    /**
     * Whether the example is stopped or not
     */
    protected boolean stopped = false;

    /**
     * The time stamp for the lastTime iteration
     */
    protected long lastTime;

    protected boolean doRender = false;
    protected PhysicalSystemRenderer renderer = null;

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

    protected void beforeSimulationLoopStart() {
    }

    protected void postSimulationStep() {

    }

    @Override
    public void runSimulationLoop() {
        beforeSimulationLoopStart();

        // perform an infinite loop until stopped.
        // render as fast as possible.
        while (!isStopped()) {
            if (doRender) {
                renderer.renderSystem();
            }

            stepWorld();

            postSimulationStep();
        }
    }

    protected void stepWorld() {
        // update the World
        // get the current time
        long time = System.nanoTime();

        // get the elapsed time from the lastTime iteration
        long diff = time - lastTime;

        // set the lastTime time
        lastTime = time;

        // convert from nanoseconds to seconds
        double elapsedTime = diff / NANO_TO_BASE;

        // update the world with the elapsed time
        world.update(elapsedTime);
    }
}
