package org.wkh.swarmscale;

import org.dyn4j.dynamics.World;

public interface PhysicalSystem {

    public World getWorld();

    public void initializeWorld();

    public void runSimulationLoop();

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
