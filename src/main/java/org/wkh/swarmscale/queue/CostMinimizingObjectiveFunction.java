package org.wkh.swarmscale.queue;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wkh.swarmscale.optimization.ObjectiveFunction;
import org.wkh.swarmscale.optimization.PIDController;

public class CostMinimizingObjectiveFunction implements ObjectiveFunction {
    public final static Logger LOGGER = Logger.getLogger(CostMinimizingObjectiveFunction.class.getName());
    
    private final int timesteps;
    private final long initialWorkLoad;
    private final int commissionTimeLower;
    private final int commissionTimeUpper;
    private final int initialCapacity;
    private final int minimumCapacity;
    private final int maximumCapacity;
    private final double parallelizablePortion;
    private final int baseWorkRateLower;
    private final int baseWorkRateUpper;
    
    private final Level logLevel;
    
    public CostMinimizingObjectiveFunction(int timesteps,
            long initialWorkLoad,
            int initialCapacity,
            int minimumCapacity,
            int maximumCapacity,
            int commissionTimeLower,
            int commissionTimeUpper,
            int baseWorkRateLower,
            int baseWorkRateUpper,
            double parallelizablePortion,
            Level logLevel) {
        this.timesteps = timesteps;
        this.initialWorkLoad = initialWorkLoad;
        this.initialCapacity = initialCapacity;
        this.commissionTimeLower = commissionTimeLower;
        this.commissionTimeUpper = commissionTimeUpper;
        this.minimumCapacity = minimumCapacity;
        this.maximumCapacity = maximumCapacity;
        this.parallelizablePortion = parallelizablePortion;
        this.baseWorkRateLower = baseWorkRateLower;
        this.baseWorkRateUpper = baseWorkRateUpper;
        
        this.logLevel = logLevel;
    }
    
    @Override
    public double evaluate(double[] position, int iteration) {
        double proportionalGain = position[0];
        double integralGain = position[1];
        double derivativeGain = position[2];
        
        // we actually don't care about keeping the queue at a setpoint as long as everything gets processed
        // so rather than guess what it should be, just let the computer search for it
        int setpoint = (int)Math.round(position[3]);
        
        final boolean reverseOutput = true;
        PIDController controller = new PIDController(proportionalGain, integralGain, derivativeGain, reverseOutput);
        controller.setOutputLimits(minimumCapacity, maximumCapacity);
        
        QueueSimulation queue = new QueueSimulation(
            initialCapacity,
            minimumCapacity,
            maximumCapacity,
            commissionTimeLower,
            commissionTimeUpper,
            baseWorkRateLower,
            baseWorkRateUpper,
            parallelizablePortion
        );
        
        QueueSimulation.LOGGER.setLevel(logLevel);
        
        PIDControlledQueueSimulation simulation = new PIDControlledQueueSimulation(controller, setpoint, queue, logLevel);
        
        queue.enqueueBatchWorkload(initialWorkLoad);
        
        for(int timestep = 1; timestep <= timesteps; timestep++) {
            /* don't commission additional consumers while other ones are waiting to come online to prevent overprovisioning */
            final boolean canCommission = queue.getQueuedConsumers() == 0;
            LOGGER.log(Level.INFO, "In timestep {0}, canComission = {1}", new Object[]{timestep, canCommission});
            simulation.stepSystem(timestep, canCommission);
        }
        final List<QueueConfigurationSnapshot> observedErrors = simulation.getObservedErrors();
        
        final int consumerTimeSum = observedErrors.stream().mapToInt(error -> error.activeConsumers).sum();
        final long totalProcessedJobs = observedErrors.get(observedErrors.size() - 1).totalProcessedJobs;
        
        final long leftoverJobs = initialWorkLoad - totalProcessedJobs;
        
        if (leftoverJobs < 0) {
            System.out.println("uh oh, something went wrong");
        }
        
        // penalize leftover jobs to encourage provisioning enough to process everything within the allotted timespan.
        // penalty factor of 100 is fairly arbitrary
        
        return 100.0*((double) leftoverJobs) + consumerTimeSum;
    }
    
    public static void main(String[] args) {
        double[] position = new double[] { 8.199568303501572, 380.26954000886576, 161.53074999638432, 0.0 };
        
        final int initialWorkload = 50000;
        final int timesteps = 250;
        
        final int initialCapacity = 1;
        final int minimumCapacity = 1;
        final int maximumCapacity = 20;
        
        final int commissionTimeLower = 1;
        final int commissionTimeUpper = 2;
        
        final int baseWorkRateLower = 15;
        final int baseWorkRateUpper = 20;
        
        final double parallelizablePortion = 0.9;
        
        final ObjectiveFunction pidSystemSimulator = new CostMinimizingObjectiveFunction(
            timesteps, 
            initialWorkload, 
            initialCapacity, 
            minimumCapacity, 
            maximumCapacity, 
            commissionTimeLower, 
            commissionTimeUpper, 
            baseWorkRateLower, 
            baseWorkRateUpper, 
            parallelizablePortion,
            Level.INFO
        );
        
        pidSystemSimulator.evaluate(position, 0);
    }
}
