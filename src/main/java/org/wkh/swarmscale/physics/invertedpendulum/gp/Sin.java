package org.wkh.swarmscale.physics.invertedpendulum.gp;
import ec.*;
import ec.gp.*;

public class Sin extends GPNode
{
    public String toString() { return "sin"; }

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
        rd.x = Math.sin(rd.x);
    }
}

