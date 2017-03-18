package org.wkh.swarmscale.physics.invertedpendulum.gp;

import org.wkh.swarmscale.physics.PhysicalSystemRenderer;
import org.wkh.swarmscale.physics.invertedpendulum.InvertedPendulumSystem;

public class GPControlDemo {

    public static void main(String[] args) {

        /* rotate the pole so that we have to take control action */
        final double initialRotation = -15.0;
        final double initialPosition = 0.0;

        final GPForceController controller = system -> {
            /* we need to transfer the system state over to the GP objects so they can use them to evaluate trees */

            final double cartPosition = system.cartPosition;
            final double cartDisplacement = system.cartDisplacement;
            final double cartVelocity = system.cartVelocity;
            final double poleRotation = system.poleRotation;
            final double poleDisplacement = system.poleDisplacement;
            final double poleVelocity = system.poleVelocity;
            final double g = 9.81;

            // control rotation:
            //double force = (((((1 + -1) / ((cartPosition + ((poleDisplacement - poleVelocity) - (((g * 1) + (4 + poleRotation)) / Math.cos((cartVelocity + 4) + (poleRotation - poleRotation))))) * ((Math.cos(poleDisplacement) + g) + cartVelocity))) + (((poleVelocity - poleDisplacement) / ((((Math.cos(Math.sin(Math.cos(poleDisplacement) + poleRotation) - (Math.sin(Math.cos(poleDisplacement) + Math.cos(poleDisplacement)) - (g * 1))) / Math.cos(Math.cos(((cartPosition + g) + g) + g))) + g) * ((Math.sin(poleVelocity) * 1) + ((((cartPosition + g) - Math.cos(cartVelocity + 4)) - 1) / cartVelocity))) * (Math.cos(Math.sin((poleRotation - poleRotation) + (Math.cos(Math.sin(poleRotation)) + Math.cos(poleDisplacement)))) + (g * 1)))) + (poleDisplacement - poleVelocity))) + (((-1 / (((poleVelocity - poleDisplacement) / (4 + poleRotation)) - (g * cartVelocity))) * (-1 * Math.sin(poleRotation))) * (Math.cos(Math.sin((poleRotation - poleRotation) + poleRotation)) + Math.cos(Math.cos(Math.sin(poleVelocity)) * cartVelocity)))) + (poleDisplacement - poleVelocity)) + ((((((-1 / (Math.cos(Math.sin(Math.cos((cartVelocity + 4) + (cartPosition + g))) - Math.cos((Math.cos(poleDisplacement) + g) + g)) - ((InvertedPendulumSystem.POLE_MASS / -1) - (((g * 1) + (poleVelocity / (4 + poleRotation))) / Math.cos((cartVelocity + 4) + (cartPosition + g)))))) + Math.cos(-1)) - 1) + g) * (-1 * Math.sin(poleRotation))) * (Math.cos(Math.cos(poleDisplacement) + g) + (Math.cos(Math.sin((((Math.sin(poleVelocity) + (cartPosition + g)) - Math.cos(cartVelocity + 4)) - 1) * cartVelocity)) + Math.cos((((poleRotation - poleRotation) + poleRotation) + Math.cos(-1)) * cartVelocity))));

            // try to control both rotation and position
            //double force = (cartPosition + (((Math.cos(1) - (Math.cos(cartPosition + poleVelocity) - Math.cos(Math.cos(((g * poleRotation) + ((cartDisplacement * (InvertedPendulumSystem.POLE_INERTIA + poleDisplacement)) - (poleVelocity + InvertedPendulumSystem.POLE_INERTIA))) - Math.cos(InvertedPendulumSystem.POLE_LENGTH))))) * Math.sin(Math.sin(Math.sin(cartPosition)))) - (poleRotation * g))) + (((((InvertedPendulumSystem.POLE_INERTIA * (-(((poleRotation * InvertedPendulumSystem.POLE_MASS) - (cartPosition + poleVelocity)) - (cartPosition + poleVelocity)))) - poleRotation) - ((Math.sin(cartVelocity) * Math.cos(InvertedPendulumSystem.POLE_INERTIA)) + poleVelocity)) * Math.cos(Math.cos(Math.cos(Math.sin(Math.cos(((poleRotation * InvertedPendulumSystem.POLE_MASS) - (cartPosition + poleVelocity)) + g)) - (poleVelocity - poleDisplacement))))) - (poleRotation * g));

            // this is the one from the paper and it doesn't work
            // double force = cartVelocity + 9.09*poleRotation + (6.0*poleVelocity + 19.1*poleRotation + cartDisplacement)*(Math.cos(poleRotation) - Math.sin(Math.cos((cartDisplacement*(Math.cos(poleRotation) - 0.02))/Math.cos(2*Math.cos(poleRotation)))));

            //double force = ((poleRotation - poleRotation) - poleVelocity) - ((poleRotation + ((((((-1 / (poleVelocity * ((10 + (cartVelocity - cartDisplacement)) + 1))) * cartDisplacement) - (poleVelocity + ((10 + 1) * poleRotation))) / 10) - (poleVelocity + ((10 + 1) * poleRotation))) / 10)) + (((10 + (cartVelocity - cartDisplacement)) + (10 + 1)) * (poleRotation + poleRotation)));
            double force = Math.sin(Math.sin(poleRotation + Math.PI)) - (((Math.sin(Math.sin(((2 * Math.PI) - Math.cos(cartVelocity)) + ((poleVelocity / 1) * (Math.sin(Math.sin(poleRotation + Math.PI)) - ((((Math.cos(poleRotation) * (2 * (1 * cartVelocity))) + Math.PI) / (2 * Math.PI)) * poleVelocity)))) * (((2 * Math.PI) - Math.cos(cartVelocity)) + (((((1 * cartVelocity) * (Math.PI + Math.PI)) + (Math.cos(poleRotation) * (1 * cartVelocity))) + poleRotation) * (Math.sin(Math.sin(3)) - ((Math.sin((2 * Math.PI) - Math.cos(cartVelocity)) / (2 * Math.PI)) * poleVelocity))))) / ((((1 * cartVelocity) * (((poleVelocity / 1) * (Math.sin(2 * Math.sin(Math.sin(poleRotation + Math.PI))) - ((((((1 * cartVelocity) * (Math.PI + Math.PI)) + (Math.cos(poleRotation) * (1 * cartVelocity))) + poleRotation) / (2 * Math.PI)) * poleVelocity))) + (1 / cartDisplacement))) + Math.sin((Math.cos(Math.PI + Math.PI) - (1 * cartVelocity)) + Math.sin((((1 * cartVelocity) * (Math.PI + Math.PI)) + (3 - -1)) + poleRotation))) + poleRotation)) / ((((1 * cartVelocity) * cartVelocity) + (3 - -1)) + poleRotation)) * poleVelocity)
                    ;
            if (Double.isNaN(force)) {
                System.out.println("NaN, not emitting");
                return 0.0;
            }

            // constrain to be within [-MAX_OUTPUT, MAX_OUTPUT]
            force = Math.min(force, GPControlledInvertedPendulumSystem.MAX_OUTPUT);
            force = Math.max(force, -GPControlledInvertedPendulumSystem.MAX_OUTPUT);

            return force;
        };

        final GPControlledInvertedPendulumSystem system = new GPControlledInvertedPendulumSystem(
                initialRotation,
                initialPosition,
                controller
        );

        PhysicalSystemRenderer window = new PhysicalSystemRenderer(system);

        window.setVisible(true);

        window.start();
    }
}
