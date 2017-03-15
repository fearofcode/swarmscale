package org.wkh.swarmscale.physics.invertedpendulum.gp;

import ec.gp.koza.KozaFitness;

public class ThresholdKozaFitness extends KozaFitness {
    private static final double THRESHOLD = Math.toRadians(5);

    @Override
    public boolean isIdealFitness() {
        return this.standardizedFitness <= THRESHOLD;
    }
}
