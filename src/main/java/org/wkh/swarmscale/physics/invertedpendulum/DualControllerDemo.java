package org.wkh.swarmscale.physics.invertedpendulum;

import org.wkh.swarmscale.physics.PhysicalSystemRenderer;

public class DualControllerDemo {
    public static void main(String[] args) {
        final double[] position = {
                0.0, 250.0, 250.0, 0.0, 0.0, 250.0
        };

        final double controlInterval = 25.0;

        final double initialRotation = 15.0;
        
        final DualControlledInvertedPendulumSystem system = new DualControlledInvertedPendulumSystem(
            position,
            controlInterval,
            initialRotation
        );
        
        PhysicalSystemRenderer window = new PhysicalSystemRenderer(system);

        window.setVisible(true);
        
        window.start();
    }
            
}
