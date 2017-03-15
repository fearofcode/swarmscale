package org.wkh.swarmscale.physics;

import java.util.ArrayList;
import java.util.List;

import org.dyn4j.dynamics.Settings;
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
     * The time stamp for the previousTime iteration
     */
    protected long previousTime;

    protected boolean doRender = false;
    protected PhysicalSystemRenderer renderer = null;
    protected long startTime;

    private final List<PhysicalSystemStepListener> stepListeners;

    public AbstractPhysicalSystem() {
        stepListeners = new ArrayList<>();
    }

    public void addStepListener(PhysicalSystemStepListener listener) {
        stepListeners.add(listener);
    }

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

        previousTime = System.nanoTime();

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

    protected void beforeSimulationLoopStart() {
    }

    protected void preSimulationStep() {

    }

    protected void postSimulationStep(double currentTime) {
    }

    public double getElapsedTime() {
        long currentTime = System.nanoTime();
        return (currentTime - startTime) / 1.0E9;
    }

    @Override
    public void runContinuousLoop(double timeLimit) {
        boolean checkTime = timeLimit > 0;

        startTime = System.nanoTime();

        beforeSimulationLoopStart();

        // perform an infinite loop until stopped.
        // render as fast as possible.
        while (!isStopped()) {
            final double elapsedTime = getElapsedTime();

            if (checkTime && elapsedTime > timeLimit) {
                stop();
            }

            if (doRender) {
                renderer.renderSystem();
            }

            preSimulationStep();
            stepWorld();

            postSimulationStep(elapsedTime);

            stepListeners.forEach(PhysicalSystemStepListener::onStep);
        }
    }


    @Override
    public void runDiscreteLoop(double targetTime) {
        final double stepFrequency = world.getSettings().getStepFrequency();
        final int steps = (int) Math.ceil(targetTime / stepFrequency);
        beforeSimulationLoopStart();

        for(int i = 1; i <= steps && !isStopped(); i++) {
            final double elapsedTime = stepFrequency * i;

            if (doRender) {
                renderer.renderSystem();
            }

            preSimulationStep();

            world.update(stepFrequency);

            postSimulationStep(elapsedTime);

            stepListeners.forEach(PhysicalSystemStepListener::onStep);
        }

        stop();
    }

    private void stepWorld() {
        // update the World
        // get the current time
        long time = System.nanoTime();

        // get the elapsed time from the previousTime iteration
        long diff = time - previousTime;

        // set the previousTime time
        previousTime = time;

        // convert from nanoseconds to seconds
        double elapsedTime = diff / NANO_TO_BASE;

        // update the world with the elapsed time
        world.update(elapsedTime);
    }
}
