package org.wkh.swarmscale.physics.invertedpendulum;

import org.wkh.swarmscale.physics.PhysicalSystemRenderer;

public class DualControllerDemo {
    public static void main(String[] args) {
        final double[] position = {20.613785351311567, 1.6351492770852374, 35.45599214880067, 46.27577217997525, 2.0232244579537073, 186.81629218629976};
        
        final double scheduleOffset = 0.25;
        
        final double controlInterval = 5.0;

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
