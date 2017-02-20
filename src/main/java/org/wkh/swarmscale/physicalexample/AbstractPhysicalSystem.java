package org.wkh.swarmscale.physicalexample;

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
    protected long startTime;

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

    @Override
    public void initializeWorld() {
        // create the world
        world = new World();
        
        lastTime = System.nanoTime();
        
        /* subclasses will call this and then add other objects */
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

    protected void beforeSimulationLoopStart() { }

    protected void postSimulationStep() { }
    
    protected void preSimulationStep() { }

    protected long getElapsedTime() {
        long currentTime = System.currentTimeMillis();
        return currentTime - startTime;
    }
    
    @Override
    public void runSimulationLoop(long millisecondTimeLimit) {
        boolean checkTime = millisecondTimeLimit > 0;
        
        startTime = System.currentTimeMillis();

        beforeSimulationLoopStart();

        // perform an infinite loop until stopped.
        // render as fast as possible.
        while (!isStopped()) {
            if (checkTime && getElapsedTime() > millisecondTimeLimit) {
                stop();
            }
            
            if (doRender) {
                renderer.renderSystem();
            }
            
            preSimulationStep();
            
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
