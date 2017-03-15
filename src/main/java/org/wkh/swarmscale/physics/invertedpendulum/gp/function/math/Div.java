package org.wkh.swarmscale.physics.invertedpendulum.gp.function.math;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import org.wkh.swarmscale.physics.invertedpendulum.gp.ForceData;

public class Div extends GPNode {
    public String toString() {
        return "/";
    }

    public int expectedChildren() {
        return 2;
    }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem) {
        double result;
        ForceData rd = ((ForceData) (input));

        children[0].eval(state, thread, input, stack, individual, problem);
        result = rd.force;

        children[1].eval(state, thread, input, stack, individual, problem);
        // protect against dividing by zero
        rd.force = rd.force != 0 ? result / rd.force : 1.0;
    }
}

