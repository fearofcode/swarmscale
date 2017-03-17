package org.wkh.swarmscale.physics.invertedpendulum.gp.function.math;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import org.wkh.swarmscale.physics.invertedpendulum.gp.ForceData;
import org.wkh.swarmscale.physics.invertedpendulum.gp.GPControlledInvertedPendulumSystem;

public class SquareRoot extends GPNode {
    public String toString() {
        return "Math.sqrt(Math.abs(";
    }

    public int expectedChildren() {
        return 1;
    }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem) {
        ForceData rd = ((ForceData) (input));

        children[0].eval(state, thread, input, stack, individual, problem);
        rd.force = Math.sqrt(Math.abs(rd.force));

        if (Double.isNaN(rd.force)) {
            System.out.println("Overflow in sqrt!");
            rd.force = GPControlledInvertedPendulumSystem.MAX_OUTPUT;
        }
    }
}

