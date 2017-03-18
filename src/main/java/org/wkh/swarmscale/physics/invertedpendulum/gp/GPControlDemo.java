package org.wkh.swarmscale.physics.invertedpendulum.gp;

import org.wkh.swarmscale.physics.PhysicalSystemRenderer;
import org.wkh.swarmscale.physics.invertedpendulum.InvertedPendulumSystem;

import java.util.List;

public class GPControlDemo {
    public static double sigmoid(double x) { return 1.0/(1.0 + Math.exp(-x)); }

    public static void main(String[] args) {

        /* rotate the pole so that we have to take control action */
        final double initialRotation = -5.0;
        final double initialPosition = 0.0;

        final GPForceController controller = system -> {
            /* we need to transfer the system state over to the GP objects so they can use them to evaluate trees */

            final double cartPosition = system.currentState.cartPosition;
            final double cartDisplacement = system.currentState.cartDisplacement;
            final double cartVelocity = system.currentState.cartVelocity;
            final double poleRotation = system.currentState.poleRotation;
            final double poleDisplacement = system.currentState.poleDisplacement;
            final double poleVelocity = system.currentState.poleVelocity;
            final double g = 9.81;

            // control rotation:
            //double force = (((((1 + -1) / ((cartPosition + ((poleDisplacement - poleVelocity) - (((g * 1) + (4 + poleRotation)) / Math.cos((cartVelocity + 4) + (poleRotation - poleRotation))))) * ((Math.cos(poleDisplacement) + g) + cartVelocity))) + (((poleVelocity - poleDisplacement) / ((((Math.cos(Math.sin(Math.cos(poleDisplacement) + poleRotation) - (Math.sin(Math.cos(poleDisplacement) + Math.cos(poleDisplacement)) - (g * 1))) / Math.cos(Math.cos(((cartPosition + g) + g) + g))) + g) * ((Math.sin(poleVelocity) * 1) + ((((cartPosition + g) - Math.cos(cartVelocity + 4)) - 1) / cartVelocity))) * (Math.cos(Math.sin((poleRotation - poleRotation) + (Math.cos(Math.sin(poleRotation)) + Math.cos(poleDisplacement)))) + (g * 1)))) + (poleDisplacement - poleVelocity))) + (((-1 / (((poleVelocity - poleDisplacement) / (4 + poleRotation)) - (g * cartVelocity))) * (-1 * Math.sin(poleRotation))) * (Math.cos(Math.sin((poleRotation - poleRotation) + poleRotation)) + Math.cos(Math.cos(Math.sin(poleVelocity)) * cartVelocity)))) + (poleDisplacement - poleVelocity)) + ((((((-1 / (Math.cos(Math.sin(Math.cos((cartVelocity + 4) + (cartPosition + g))) - Math.cos((Math.cos(poleDisplacement) + g) + g)) - ((InvertedPendulumSystem.POLE_MASS / -1) - (((g * 1) + (poleVelocity / (4 + poleRotation))) / Math.cos((cartVelocity + 4) + (cartPosition + g)))))) + Math.cos(-1)) - 1) + g) * (-1 * Math.sin(poleRotation))) * (Math.cos(Math.cos(poleDisplacement) + g) + (Math.cos(Math.sin((((Math.sin(poleVelocity) + (cartPosition + g)) - Math.cos(cartVelocity + 4)) - 1) * cartVelocity)) + Math.cos((((poleRotation - poleRotation) + poleRotation) + Math.cos(-1)) * cartVelocity))));

            // try to control both rotation and position
            double force = (cartPosition + (((Math.cos(1) - (Math.cos(cartPosition + poleVelocity) - Math.cos(Math.cos(((g * poleRotation) + ((cartDisplacement * (InvertedPendulumSystem.POLE_INERTIA + poleDisplacement)) - (poleVelocity + InvertedPendulumSystem.POLE_INERTIA))) - Math.cos(InvertedPendulumSystem.POLE_LENGTH))))) * Math.sin(Math.sin(Math.sin(cartPosition)))) - (poleRotation * g))) + (((((InvertedPendulumSystem.POLE_INERTIA * (-(((poleRotation * InvertedPendulumSystem.POLE_MASS) - (cartPosition + poleVelocity)) - (cartPosition + poleVelocity)))) - poleRotation) - ((Math.sin(cartVelocity) * Math.cos(InvertedPendulumSystem.POLE_INERTIA)) + poleVelocity)) * Math.cos(Math.cos(Math.cos(Math.sin(Math.cos(((poleRotation * InvertedPendulumSystem.POLE_MASS) - (cartPosition + poleVelocity)) + g)) - (poleVelocity - poleDisplacement))))) - (poleRotation * g));

            // this is the one from the paper and it doesn't work
            // double force = cartVelocity + 9.09*poleRotation + (6.0*poleVelocity + 19.1*poleRotation + cartDisplacement)*(Math.cos(poleRotation) - Math.sin(Math.cos((cartDisplacement*(Math.cos(poleRotation) - 0.02))/Math.cos(2*Math.cos(poleRotation)))));

            //double force = ((poleRotation - poleRotation) - poleVelocity) - ((poleRotation + ((((((-1 / (poleVelocity * ((10 + (cartVelocity - cartDisplacement)) + 1))) * cartDisplacement) - (poleVelocity + ((10 + 1) * poleRotation))) / 10) - (poleVelocity + ((10 + 1) * poleRotation))) / 10)) + (((10 + (cartVelocity - cartDisplacement)) + (10 + 1)) * (poleRotation + poleRotation)));
            //double force = ((((((poleVelocity + (((poleVelocity + (((poleVelocity * cartVelocity) + (poleVelocity * -0.03128818810873213)) + ((-0.03128818810873213 * (((poleVelocity * cartVelocity) + (poleVelocity * -0.03128818810873213)) * -0.03128818810873213)) + ((-0.03128818810873213 * ((poleVelocity * -0.03128818810873213) * -0.03128818810873213)) + (poleVelocity * -0.03128818810873213))))) * -0.03128818810873213) - poleRotation)) * -0.03128818810873213) - poleRotation) - poleRotation) + ((poleVelocity * -0.03128818810873213) - poleRotation)) + (((((cartVelocity - 0.37058410386291607) * (((((poleVelocity + ((poleVelocity * cartVelocity) + (cartVelocity - 0.37058410386291607))) * -0.03128818810873213) - poleRotation) * -0.03128818810873213) - poleRotation)) + (poleVelocity * -0.03128818810873213)) + (poleVelocity * -0.03128818810873213)) * sigmoid(((cartVelocity - 0.37058410386291607) * ((((poleVelocity * poleVelocity) * -0.03128818810873213) - poleRotation) - poleRotation)) + (poleVelocity * -0.03128818810873213)))) + ((poleVelocity * -0.03128818810873213) - poleRotation)
            //        ;
            if (Double.isNaN(force)) {
                System.out.println("NaN, not emitting");
                return 0.0;
            }

            // constrain to be within [-MAX_OUTPUT, MAX_OUTPUT]
            force = Math.min(force, GPControlledInvertedPendulumSystem.MAX_OUTPUT);
            force = Math.max(force, -GPControlledInvertedPendulumSystem.MAX_OUTPUT);
            System.out.println(force);
            return force;
        };

        final GPControlledInvertedPendulumSystem system = new GPControlledInvertedPendulumSystem(
                initialRotation,
                initialPosition,
                controller,
                new FitnessCalculator() {
                    @Override
                    public double getFitness(List<StateObservation> states) {
                        return 0;
                    }

                    @Override
                    public boolean shouldStop(StateObservation state) {
                        return false;
                    }
                }
        );

        PhysicalSystemRenderer window = new PhysicalSystemRenderer(system);

        window.setVisible(true);

        window.start();
    }
}
