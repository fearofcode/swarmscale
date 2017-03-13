/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package org.wkh.swarmscale.physics.invertedpendulum.gp;

import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

public class MultiValuedRegression extends GPProblem implements SimpleProblemForm {
    private static final long serialVersionUID = 1;

    public double currentX;
    public double currentY;

    public void evaluate(final EvolutionState state,
                         final Individual ind,
                         final int subpopulation,
                         final int threadnum) {
        if (ind.evaluated) {
            return;
        }

        DoubleData input = (DoubleData) (this.input);

        int hits = 0;
        double sum = 0.0;
        double expectedResult;
        double result;
        for (int y=0;y<10;y++)
        {
            currentX = state.random[threadnum].nextDouble();
            currentY = state.random[threadnum].nextDouble();
            // (+ (cos y) (+ (* (* (sin x) x) y) (/ x y)))
            expectedResult = Math.sin(currentX)*currentX*currentY + currentX/currentY + Math.cos(currentY);
            ((GPIndividual)ind).trees[0].child.eval(
                    state,threadnum,input,stack,((GPIndividual)ind),this);

            result = Math.abs(expectedResult - input.x);
            if (result <= 0.001) hits++;
            sum += result;
        }

        KozaFitness f = ((KozaFitness)ind.fitness);
        f.setStandardizedFitness(state, sum);
        f.hits = hits;
        ind.evaluated = true;
    }
}

