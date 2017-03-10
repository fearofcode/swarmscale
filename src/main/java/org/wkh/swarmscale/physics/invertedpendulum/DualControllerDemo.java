package org.wkh.swarmscale.physics.invertedpendulum;

import org.wkh.swarmscale.physics.PhysicalSystemRenderer;

public class DualControllerDemo {
    public static void main(String[] args) {
        final double[] position = {1.604174313045244, 0.0, 80.98216608474266, 5.0, 0.009827316069225836, 212.97162266849858};
        
        /* TODO write factory helper to use positions in visualized simulations */
        
        final double rotationalProportionalGain = position[0];
        final double rotationalIntegralGain = position[1];
        final double rotationalDerivativeGain = position[2];
        
        final double positionProportionalGain = position[3];
        final double positionIntegralGain = position[4];
        final double positionDerivativeGain = position[5];
        
        final double controlInterval = 1.0;

        /* rotate the pole so that we have to take control action */
        final double initialRotation = -5.0;
        
        final DualControlledInvertedPendulumSystem system = new DualControlledInvertedPendulumSystem(
            rotationalProportionalGain,
            rotationalIntegralGain,
            rotationalDerivativeGain,
            positionProportionalGain,
            positionIntegralGain,
            positionDerivativeGain,
            controlInterval,
            initialRotation
        );
        
        PhysicalSystemRenderer window = new PhysicalSystemRenderer(system);

        window.setVisible(true);
        
        window.start();
    }
            
}
