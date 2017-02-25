package org.wkh.swarmscale.physics.ballgravity;

/**
 * A collection of controller parameter sets that effectively solve the control problem.
 */
public class StableControllersFactory {

    public static PIDControlledBallGravitySystem stableZieglerNicholsSystem(int runTime) {
        /* derived from standard ZN procedure of using proportional control alone until system reached critical point
        and measuring oscillation time */
        double criticalGain = 25.0;
        double oscillationTime = 0.1;
        return new PIDControlledBallGravitySystem(0.2 * criticalGain, 0.3 * oscillationTime, 0.5 * criticalGain, 10.0, 25.0, runTime);
    }

    public static PIDControlledBallGravitySystem stablePSODerivedSystem(int runTime) {
        /* derived from running the optimizer */
        return new PIDControlledBallGravitySystem(24.03976158272401, 10.033777480148322, 44.150628224180416, 25.0, 25, runTime);
    }

    public static PIDControlledBallGravitySystem stablePSOZNSeedDerivedSystem(int runTime) {
        /* derived from running the optimizer with Ziegler Nichols seeds */
        return new PIDControlledBallGravitySystem(24.165633286024093, 7.257835911895666, 32.13029317549218, 25.0, 25, runTime);
    }

}
