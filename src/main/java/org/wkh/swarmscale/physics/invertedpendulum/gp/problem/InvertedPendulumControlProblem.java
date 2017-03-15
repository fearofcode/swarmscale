package org.wkh.swarmscale.physics.invertedpendulum.gp.problem;

import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;
import org.wkh.swarmscale.physics.invertedpendulum.gp.GPControlledInvertedPendulumSystem;
import org.wkh.swarmscale.physics.invertedpendulum.gp.GPForceController;
import org.wkh.swarmscale.physics.invertedpendulum.gp.ForceData;

import java.util.Random;

public class InvertedPendulumControlProblem extends GPProblem implements SimpleProblemForm {
    private static final long serialVersionUID = 1;

    public double cartPosition;
    public double cartDisplacement;
    public double cartVelocity;
    public double cartAcceleration;

    public double poleRotation;
    public double poleDisplacement;
    public double poleVelocity;
    public double poleAcceleration;

    public void evaluate(final EvolutionState state,
                         final Individual ind,
                         final int subpopulation,
                         final int threadnum) {
        if (ind.evaluated) {
            return;
        }

        ForceData input = (ForceData) (this.input);

        final double runTime = 30.0;

        /* rotate the pole randomly so that we have to take control action -- [2, 7] or [-7, -2] */
        Random rng = new Random();
        double sign = rng.nextBoolean() ? -1.0 : 1.0;
        final double initialRotation = sign*(rng.nextDouble() * 5.0 + 2.0);

        final GPForceController controller = system -> {
            /* we need to transfer the system state over to the GP objects so they can use them to evaluate trees */

            cartPosition = system.cartPosition;
            cartDisplacement = system.cartDisplacement;
            cartVelocity = system.cartVelocity;
            cartAcceleration = system.cartAcceleration;

            poleRotation = system.poleRotation;
            poleDisplacement = system.poleDisplacement;
            poleVelocity = system.poleVelocity;
            poleAcceleration = system.poleAcceleration;

            /* now we can actually evaluate the tree */

            ((GPIndividual)ind).trees[0].child.eval(
                    state,
                    threadnum,
                    input,
                    this.stack,
                    (GPIndividual)ind,
                    this
            );

            double force = input.force;

            // constrain to be within [-MAX_OUTPUT, MAX_OUTPUT]
            force = Math.min(force, GPControlledInvertedPendulumSystem.MAX_OUTPUT);
            force = Math.max(force, -GPControlledInvertedPendulumSystem.MAX_OUTPUT);

            return force;
        };

        final GPControlledInvertedPendulumSystem physicalSystem = new GPControlledInvertedPendulumSystem(
                initialRotation,
                controller
        );

        physicalSystem.initializeWorld();

        physicalSystem.runDiscreteLoop(runTime);
        KozaFitness f = (KozaFitness) ind.fitness;

        f.setStandardizedFitness(state, physicalSystem.getErrorSum());
        f.hits = 0;

        ind.evaluated = true;
    }
}

