package org.wkh.swarmscale.optimization;

@FunctionalInterface
public interface ObjectiveFunction {
    public double evaluate(double[] position);
}
