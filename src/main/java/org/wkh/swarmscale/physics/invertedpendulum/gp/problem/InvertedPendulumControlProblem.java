package org.wkh.swarmscale.physics.invertedpendulum.gp.problem;

import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;
import org.wkh.swarmscale.physics.invertedpendulum.gp.GPControlledInvertedPendulumSystem;
import org.wkh.swarmscale.physics.invertedpendulum.gp.GPForceController;
import org.wkh.swarmscale.physics.invertedpendulum.gp.ForceData;

public class InvertedPendulumControlProblem extends GPProblem implements SimpleProblemForm {
    private static final long serialVersionUID = 1;

    public double cartPosition;
    public double cartDisplacement;
    public double cartVelocity;

    public double poleRotation;
    public double poleDisplacement;
    public double poleVelocity;

    public void evaluate(final EvolutionState state,
                         final Individual ind,
                         final int subpopulation,
                         final int threadnum) {
        if (ind.evaluated) {
            return;
        }

        ForceData input = (ForceData) (this.input);

        final double runTime = 30.0;

        final GPForceController controller = system -> {
            /* we need to transfer the system state over to the GP objects so they can use them to evaluate trees */

            cartPosition = system.cartPosition;
            cartDisplacement = system.cartDisplacement;
            cartVelocity = system.cartVelocity;

            poleRotation = system.poleRotation;
            poleDisplacement = system.poleDisplacement;
            poleVelocity = system.poleVelocity;

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

        double errorSum = 0.0;
        int hits = 0;
        for(final double rotation : new double[] {-5.0, 5.0 }) {
            final GPControlledInvertedPendulumSystem physicalSystem = new GPControlledInvertedPendulumSystem(
                    rotation,
                    0.0,
                    controller
            );

            physicalSystem.initializeWorld();

            physicalSystem.runDiscreteLoop(runTime);
            errorSum += physicalSystem.getErrorSum();
            hits += physicalSystem.hits;
        }

        KozaFitness f = (KozaFitness) ind.fitness;

        //final double parsimony = 1.0;
        //f.setStandardizedFitness(state, errorSum + parsimony*ind.size());
        f.setStandardizedFitness(state, errorSum + ind.size()*0.00001);
        f.hits = hits;

        ind.evaluated = true;
    }
}

