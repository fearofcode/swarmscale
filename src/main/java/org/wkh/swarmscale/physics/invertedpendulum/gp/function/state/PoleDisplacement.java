package org.wkh.swarmscale.physics.invertedpendulum.gp.function.state;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import org.wkh.swarmscale.physics.invertedpendulum.gp.ForceData;
import org.wkh.swarmscale.physics.invertedpendulum.gp.problem.InvertedPendulumControlProblem;

public class PoleDisplacement extends GPNode {
    public String toString() {
        return "poleDisplacement";
    }

    public int expectedChildren() {
        return 0;
    }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem) {
        ForceData rd = ((ForceData) (input));
        rd.force = ((InvertedPendulumControlProblem) problem).poleDisplacement;
    }
}

