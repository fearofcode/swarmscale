package org.wkh.swarmscale.physics.invertedpendulum.gp;
import ec.*;
import ec.gp.*;

public class Div extends GPNode
{
    public String toString() { return "/"; }

    public int expectedChildren() { return 2; }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem)
    {
        double result;
        DoubleData rd = ((DoubleData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        result = rd.x;

        children[1].eval(state,thread,input,stack,individual,problem);
        // protect against dividing by zero
        rd.x = rd.x != 0 ? result / rd.x : 1.0;
    }
}

