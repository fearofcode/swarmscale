package org.wkh.swarmscale.physics.invertedpendulum.gp.problem;

import ec.*;
import ec.simple.*;
import ec.vector.*;

public class MaxOnes extends Problem implements SimpleProblemForm {
    public void evaluate(final EvolutionState state,
                         final Individual ind,
                         final int subpopulation,
                         final int threadnum) {
        if (ind.evaluated) {
            return;
        }

        int sum = 0;
        BitVectorIndividual ind2 = (BitVectorIndividual) ind;
        for (int x = 0; x < ind2.genome.length; x++)
            sum += (ind2.genome[x] ? 0 : 1);

        SimpleFitness simpleFitness = (SimpleFitness) ind2.fitness;

        simpleFitness.setFitness(
                state,
                sum / (double) ind2.genome.length,
                sum == ind2.genome.length
        );
        ind2.evaluated = true;
    }
}