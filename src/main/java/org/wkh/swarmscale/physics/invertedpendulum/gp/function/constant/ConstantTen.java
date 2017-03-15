package org.wkh.swarmscale.physics.invertedpendulum.gp.function.constant;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import org.wkh.swarmscale.physics.invertedpendulum.gp.ForceData;

public class ConstantTen extends GPNode {
    public String toString() {
        return "10";
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

        rd.force = 10.0;
    }
}

