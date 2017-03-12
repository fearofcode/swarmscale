package org.wkh.swarmscale.physics.invertedpendulum;

import org.wkh.swarmscale.physics.PhysicalSystemRenderer;

public class DualControllerDemo {
    public static void main(String[] args) {
        final double[] position = {
            25.0, 1.0, 18.011976096621435, 0.0, 0.0, 0.0, 25.0, 0.0, 24.347029094832724, 0.0, 0.0, 25.0, 25.0, 1.0, 23.50542291861912, 0.0, 0.0, 25.0
        };
        
        final double scheduleOffset = 0.25;
        
        final double controlInterval = 25.0;

        /* rotate the pole so that we have to take control action */
        final double initialRotation = -5.0;
        
        final DualControlledInvertedPendulumSystem system = new DualControlledInvertedPendulumSystem(
            position,
            scheduleOffset,
            controlInterval,
            initialRotation
        );
        
        PhysicalSystemRenderer window = new PhysicalSystemRenderer(system);

        window.setVisible(true);
        
        window.start();
    }
            
}
