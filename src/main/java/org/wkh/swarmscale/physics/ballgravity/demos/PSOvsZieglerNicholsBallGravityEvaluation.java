package org.wkh.swarmscale.physics.ballgravity.demos;

import java.util.List;
import org.wkh.swarmscale.optimization.ControlPerformanceResult;
import org.wkh.swarmscale.physics.ballgravity.PIDControlledBallGravitySystem;
import org.wkh.swarmscale.physics.ballgravity.StableControllersFactory;

public class PSOvsZieglerNicholsBallGravityEvaluation {

    public static double evaluateSystem(PIDControlledBallGravitySystem system, int runTime) {
        system.initializeWorld();
        double[] steps = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0, -1.0, -2.0, -1.0, 0.0};

        system.addStepListener(() -> {
            for (int i = 1; i < steps.length; i++) {
                if (system.getElapsedTime() >= 2000.0 * i && system.getTargetPosition() == steps[i - 1]) {
                    system.setTargetPosition(steps[i]);
                }
            }
        });

        system.runSimulationLoop(runTime);
        final List<ControlPerformanceResult> observedErrors = system.getObservedErrors();

        /* sum up the errors */
        return observedErrors.stream().mapToDouble(result -> result.error).sum();
    }

    public static void main(String[] args) {
        final int runTime = 30000;

        final PIDControlledBallGravitySystem psoSystem = StableControllersFactory.stablePSODerivedSystem();

        final PIDControlledBallGravitySystem zieglerNicholsSystem = StableControllersFactory.stableZieglerNicholsSystem();

        System.out.println("PSO: " + evaluateSystem(psoSystem, runTime));
        System.out.println("Ziegler-Nichols: " + evaluateSystem(zieglerNicholsSystem, runTime));
    }
}
