package org.wkh.swarmscale.physicalexample;

public class PSOBallGravityDemo {
    public static void main(String[] args) {
        final int runTime = 30000;
        final PIDControlledBallGravitySystem system = PIDControlledBallGravitySystem.stablePSODerivedSystem(runTime);
        
        system.initializeWorld();
        
        double[] steps = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0, -1.0, -2.0, -1.0, 0.0};
        system.addStepListener(() -> {    
            for(int i = 1; i < steps.length; i++) {
                if (system.getElapsedTime() >= 2000.0*i && system.getTargetPosition() == steps[i-1]) {
                    system.setTargetPosition(steps[i]);
                }
            }
        });
        
        PhysicalSystemRenderer window = new PhysicalSystemRenderer(system);

        // show it
        window.setVisible(true);

        // start it
        window.start();
    }
}
