package org.wkh.swarmscale.physics.invertedpendulum.gp;

import java.util.List;

public interface FitnessCalculator {
    double getFitness(List<StateObservation> states);

    boolean shouldStop(StateObservation state);
}
