package org.wkh.swarmscale.optimization;

@FunctionalInterface
public interface ObjectiveFunction {

    /**
     *
     * @param position The value to compute the fitness of
     * @param iteration Current iteration, in order to change the fitness function as time progresses
     * @return
     */
    public double evaluate(double[] position, int iteration);
}
