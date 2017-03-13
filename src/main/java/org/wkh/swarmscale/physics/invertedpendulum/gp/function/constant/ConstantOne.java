package org.wkh.swarmscale.physics.invertedpendulum.gp.function.constant;
import ec.*;
import ec.gp.*;
import org.wkh.swarmscale.physics.invertedpendulum.gp.DoubleData;

public class ConstantOne extends GPNode
{
    public String toString() { return "1"; }

    public int expectedChildren() { return 0; }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem)
    {
        DoubleData rd = ((DoubleData)(input));

        rd.x = 1.0;
    }
}

