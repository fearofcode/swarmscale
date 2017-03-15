package org.wkh.swarmscale.physics.invertedpendulum.gp;

@FunctionalInterface
public interface GPForceController {
    double getForce(GPControlledInvertedPendulumSystem system);
}
