package org.wkh.swarmscale.physics.invertedpendulum.gp;

import org.wkh.swarmscale.physics.PhysicalSystemRenderer;
import org.wkh.swarmscale.physics.invertedpendulum.InvertedPendulumSystem;

public class GPControlDemo {
    public static void main(String[] args) {

        /* rotate the pole so that we have to take control action */
        final double initialRotation = 5.0;

        final GPForceController controller = system -> {
            /* we need to transfer the system state over to the GP objects so they can use them to evaluate trees */

            final double cartPosition = system.cartPosition;
            final double cartDisplacement = system.cartDisplacement;
            final double cartVelocity = system.cartVelocity;
            final double cartAcceleration = system.cartAcceleration;
            final double poleRotation = system.poleRotation;
            final double poleDisplacement = system.poleDisplacement;
            final double poleVelocity = system.poleVelocity;
            final double poleAcceleration = system.poleAcceleration;
            final double g = 9.81;

            // control rotation:
            double force = (((((1 + -1) / ((cartPosition + ((poleDisplacement - poleVelocity) - (((g * 1) + (4 + poleRotation)) / Math.cos((cartVelocity + 4) + (poleRotation - poleRotation))))) * ((Math.cos(poleDisplacement) + g) + cartVelocity))) + (((poleVelocity - poleDisplacement) / ((((Math.cos(Math.sin(Math.cos(poleDisplacement) + poleRotation) - (Math.sin(Math.cos(poleDisplacement) + Math.cos(poleDisplacement)) - (g * 1))) / Math.cos(Math.cos(((cartPosition + g) + g) + g))) + g) * ((Math.sin(poleVelocity) * 1) + ((((cartPosition + g) - Math.cos(cartVelocity + 4)) - 1) / cartVelocity))) * (Math.cos(Math.sin((poleRotation - poleRotation) + (Math.cos(Math.sin(poleRotation)) + Math.cos(poleDisplacement)))) + (g * 1)))) + (poleDisplacement - poleVelocity))) + (((-1 / (((poleVelocity - poleDisplacement) / (4 + poleRotation)) - (g * cartVelocity))) * (-1 * Math.sin(poleRotation))) * (Math.cos(Math.sin((poleRotation - poleRotation) + poleRotation)) + Math.cos(Math.cos(Math.sin(poleVelocity)) * cartVelocity)))) + (poleDisplacement - poleVelocity)) + ((((((-1 / (Math.cos(Math.sin(Math.cos((cartVelocity + 4) + (cartPosition + g))) - Math.cos((Math.cos(poleDisplacement) + g) + g)) - ((InvertedPendulumSystem.POLE_MASS / -1) - (((g * 1) + (poleVelocity / (4 + poleRotation))) / Math.cos((cartVelocity + 4) + (cartPosition + g)))))) + Math.cos(-1)) - 1) + g) * (-1 * Math.sin(poleRotation))) * (Math.cos(Math.cos(poleDisplacement) + g) + (Math.cos(Math.sin((((Math.sin(poleVelocity) + (cartPosition + g)) - Math.cos(cartVelocity + 4)) - 1) * cartVelocity)) + Math.cos((((poleRotation - poleRotation) + poleRotation) + Math.cos(-1)) * cartVelocity))));

            // constrain to be within [-MAX_OUTPUT, MAX_OUTPUT]
            force = Math.min(force, GPControlledInvertedPendulumSystem.MAX_OUTPUT);
            force = Math.max(force, -GPControlledInvertedPendulumSystem.MAX_OUTPUT);

            return force;
        };

        final GPControlledInvertedPendulumSystem system = new GPControlledInvertedPendulumSystem(
                initialRotation,
                controller
        );

        PhysicalSystemRenderer window = new PhysicalSystemRenderer(system);

        window.setVisible(true);

        window.start();
    }
}
