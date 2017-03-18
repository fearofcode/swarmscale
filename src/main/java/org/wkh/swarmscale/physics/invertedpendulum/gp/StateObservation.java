package org.wkh.swarmscale.physics.invertedpendulum.gp;

public class StateObservation {
    public final double cartPosition;
    public final double cartDisplacement;
    public final double cartVelocity;

    public final double poleRotation;
    public final double poleDisplacement;
    public final double poleVelocity;

    public StateObservation(double cartPosition, double cartDisplacement, double cartVelocity, double poleRotation, double poleDisplacement, double poleVelocity) {
        this.cartPosition = cartPosition;
        this.cartDisplacement = cartDisplacement;
        this.cartVelocity = cartVelocity;
        this.poleRotation = poleRotation;
        this.poleDisplacement = poleDisplacement;
        this.poleVelocity = poleVelocity;
    }
}
