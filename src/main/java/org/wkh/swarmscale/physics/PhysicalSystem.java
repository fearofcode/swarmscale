package org.wkh.swarmscale.physics;

import org.dyn4j.dynamics.World;

public interface PhysicalSystem {
    World getWorld();

    void initializeWorld();

    /**
     * Run the main simulation loop.
     *
     * @param timeLimit - time limit to execute loop for, in seconds. Enforced approximately. Negative values will cause
     *                  the simulation to run forever.
     */
    void runContinuousLoop(double timeLimit);

    void runDiscreteLoop(double targetTime);

    void setDoRender(boolean doRender);

    void setRenderer(PhysicalSystemRenderer renderer);

    /**
     * Returns true if the system is stopped.
     *
     * @return boolean true if stopped
     */
    boolean isStopped();

    void setStopped(boolean stopped);

    /**
     * Stops the example.
     */
    void stop();
}
