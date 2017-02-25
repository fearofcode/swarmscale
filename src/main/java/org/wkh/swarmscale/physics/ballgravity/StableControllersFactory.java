package org.wkh.swarmscale.physics.ballgravity;

/**
 * A collection of controller parameter sets that effectively solve the control problem.
 */
public class StableControllersFactory {

    /**
     * derived from standard ZN procedure of using proportional control alone until system reached critical point
     * and measuring oscillation time
     * @return PIDControlledBallGravitySystem
     */
    public static PIDControlledBallGravitySystem stableZieglerNicholsSystem() {
        
        double criticalGain = 25.0;
        double oscillationTime = 0.1;
        return new PIDControlledBallGravitySystem(0.2 * criticalGain, 0.3 * oscillationTime, 0.5 * criticalGain, 10.0, 25.0);
    }

    /**
     * derived from running the optimizer to minimize absolute error. produces some overshoot
     * @return PIDControlledBallGravitySystem 
     */
    public static PIDControlledBallGravitySystem stablePSODerivedSystem() {
        /*  */
        return new PIDControlledBallGravitySystem(24.03976158272401, 10.033777480148322, 44.150628224180416, 25.0, 25.0);
    }

    /**
     * derived from running the optimizer with Ziegler Nichols seeds
     * @return PIDControlledBallGravitySystem
     */
    public static PIDControlledBallGravitySystem stablePSOZNSeedDerivedSystem() {
        return new PIDControlledBallGravitySystem(24.165633286024093, 7.257835911895666, 32.13029317549218, 25.0, 25.0);
    }
    
    public static PIDControlledBallGravitySystem lowOvershootController() {
        return new PIDControlledBallGravitySystem(24.76650758566322, 3.980240660248577, 58.57462400272237, 25.0, 25.0);
    }

}
