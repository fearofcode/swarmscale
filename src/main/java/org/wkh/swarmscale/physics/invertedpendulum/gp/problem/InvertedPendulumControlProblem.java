package org.wkh.swarmscale.physics.invertedpendulum.gp.problem;

import ec.*;
import ec.gp.*;
import ec.simple.*;
import org.wkh.swarmscale.physics.invertedpendulum.gp.*;

import java.util.List;

public class InvertedPendulumControlProblem extends GPProblem implements SimpleProblemForm {
    public static final int STEPS = 1000;

    private static final long serialVersionUID = 1;

    public StateObservation systemState;

    public void evaluate(final EvolutionState evolutionState,
                         final Individual ind,
                         final int subpopulation,
                         final int threadnum) {
        if (ind.evaluated) {
            return;
        }

        ForceData input = (ForceData) (this.input);

        final GPForceController controller = system -> {
            /* we need to transfer the system state over to the GP objects so they can use them to evaluate trees */

            /* now we can actually evaluate the tree */

            systemState = system.currentState;
            ((GPIndividual)ind).trees[0].child.eval(
                    evolutionState,
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

        final double rotationThreshold = Math.toRadians(36.0); // pi/5
        final double displacementThreshold = 10.0;

        final FitnessCalculator gruauFitnessCalculator = new FitnessCalculator() {
            @Override
            public double getFitness(List<StateObservation> states) {
                // section 3.5 of the NEAT paper

                final int balancedSteps = states.size();
                final double f1 = ((double)balancedSteps) / STEPS;

                double f2 = 0.0;

                if (balancedSteps >= 100) {
                    for(int i = balancedSteps - 100; i < balancedSteps; i++) {
                        final StateObservation state = states.get(i);
                        f2 += 0.75/(state.cartDisplacement + Math.abs(state.cartVelocity) + state.poleDisplacement + Math.abs(state.poleVelocity + 0.000001));
                    }
                }
                return 0.1*f1 + 0.9*f2;
            }

            @Override
            public boolean shouldStop(StateObservation state) {
                return state.poleDisplacement > rotationThreshold || state.cartDisplacement > displacementThreshold;
            }
        };

        double fitnessSum = 0.0;
        for(final double rotation : new double[] { 5.0 }) {
            final GPControlledInvertedPendulumSystem physicalSystem = new GPControlledInvertedPendulumSystem(
                    rotation,
                    0.0,
                    controller,
                    gruauFitnessCalculator
            );

            physicalSystem.initializeWorld();
            physicalSystem.runDiscreteLoopForSteps(STEPS);

            fitnessSum += gruauFitnessCalculator.getFitness(physicalSystem.states);
        }

        SimpleFitness simpleFitness = (SimpleFitness) ind.fitness;

        simpleFitness.setFitness(
                evolutionState,
                fitnessSum,
                false // just always run
        );

        ind.evaluated = true;
    }
}

