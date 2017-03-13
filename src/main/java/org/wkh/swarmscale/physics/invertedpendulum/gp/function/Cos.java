package org.wkh.swarmscale.physics.invertedpendulum.gp.function;
import ec.*;
import ec.gp.*;
import org.wkh.swarmscale.physics.invertedpendulum.gp.DoubleData;

public class Cos extends GPNode
{
    public String toString() { return "cos"; }

    public int expectedChildren() { return 1; }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem)
    {
        DoubleData rd = ((DoubleData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        rd.x = Math.cos(rd.x);
    }
}

