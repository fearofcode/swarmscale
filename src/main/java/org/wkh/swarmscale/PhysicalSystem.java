package org.wkh.swarmscale;

import org.dyn4j.dynamics.World;

public interface PhysicalSystem {

    public World getWorld();

    public void initializeWorld();

    /**
     * Run the main simulation loop.
     * 
     * @param millisecondTimeLimit - time limit to execute loop for, in milliseconds. Enforced approximately. Negative
     * values will cause the simulation to run forever.
     */
    public void runSimulationLoop(long millisecondTimeLimit);

    public void setDoRender(boolean doRender);

    public void setRenderer(PhysicalSystemRenderer renderer);

    /**
     * Returns true if the system is stopped.
     *
     * @return boolean true if stopped
     */
    public boolean isStopped();

    public void setStopped(boolean stopped);

    /**
     * Stops the example.
     */
    public void stop();
}
