package org.wkh.swarmscale.physics.ballgravity.demos;

import org.wkh.swarmscale.physics.PhysicalSystemRenderer;
import org.wkh.swarmscale.physics.ballgravity.PIDControlledBallGravitySystem;
import org.wkh.swarmscale.physics.ballgravity.StableControllersFactory;

public class PSOBallGravityDemo {

    public static void main(String[] args) {
        final PIDControlledBallGravitySystem system = StableControllersFactory.lowOvershootController();
        system.setVerbose(true);
        system.initializeWorld();

        double[] steps = {0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0, -1.0, -2.0, -1.0, 0.0};
        system.addStepListener(() -> {
            for (int i = 1; i < steps.length; i++) {
                if (system.getElapsedTime() >= 2000.0 * i && system.getTargetPosition() == steps[i - 1]) {
                    system.setTargetPosition(steps[i]);
                }
            }
        });

        PhysicalSystemRenderer window = new PhysicalSystemRenderer(system);

        window.setVisible(true);
        
        window.start();
    }
}
