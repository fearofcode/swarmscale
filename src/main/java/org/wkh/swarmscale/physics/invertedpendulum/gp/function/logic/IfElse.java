package org.wkh.swarmscale.physics.invertedpendulum.gp.function.logic;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import org.wkh.swarmscale.physics.invertedpendulum.gp.ForceData;

public class IfElse extends GPNode {
    public String toString() {
        return "ifElse";
    }

    public int expectedChildren() {
        return 3;
    }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem) {
        double condition;
        ForceData rd = ((ForceData) (input));

        children[0].eval(state, thread, input, stack, individual, problem);
        condition = rd.force;

        if (condition > 0) {
            children[1].eval(state, thread, input, stack, individual, problem);
        } else {
            children[2].eval(state, thread, input, stack, individual, problem);
        }
    }
}