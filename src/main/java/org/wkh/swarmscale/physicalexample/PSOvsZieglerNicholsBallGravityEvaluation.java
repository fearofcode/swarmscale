package org.wkh.swarmscale.physicalexample;

import java.util.List;

public class PSOvsZieglerNicholsBallGravityEvaluation {
    public static double evaluateSystem(PIDControlledBallGravitySystem system, int runTime) {
        system.initializeWorld();
        double[] steps = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0, -1.0, -2.0, -1.0, 0.0};
        
        system.addStepListener(() -> {    
            for(int i = 1; i < steps.length; i++) {
                if (system.getElapsedTime() >= 2000.0*i && system.getTargetPosition() == steps[i-1]) {
                    system.setTargetPosition(steps[i]);
                }
            }
        });
        
        system.runSimulationLoop(runTime);
        final List<Double> observedErrors = system.getObservedErrors();
        
        /* sum up the errors */

        return observedErrors.stream().mapToDouble(i -> i).sum();
    }
    
    public static void main(String[] args) {
        final int runTime = 30000;
        
        final PIDControlledBallGravitySystem psoSystem = PIDControlledBallGravitySystem.stablePSODerivedSystem(runTime);
        
        final PIDControlledBallGravitySystem zieglerNicholsSystem = PIDControlledBallGravitySystem.stableZieglerNicholsSystem(runTime);
        
        System.out.println("PSO: " + evaluateSystem(psoSystem, runTime));
        System.out.println("Ziegler-Nichols: " + evaluateSystem(zieglerNicholsSystem, runTime));
    }
}
