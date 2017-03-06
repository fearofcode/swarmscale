package org.wkh.swarmscale.queue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wkh.swarmscale.optimization.ObjectiveFunction;
import org.wkh.swarmscale.optimization.PIDController;

public class CostMinimizingObjectiveFunction implements ObjectiveFunction {
    public final static Logger LOGGER = Logger.getLogger(CostMinimizingObjectiveFunction.class.getName());
    
    private final int timesteps;
    private final int commissionTimeLower;
    private final int commissionTimeUpper;
    private final Map<Integer, Integer> workloads;
    private final int initialCapacity;
    private final int minimumCapacity;
    private final int maximumCapacity;
    private final double parallelizablePortion;
    private final int baseWorkRateLower;
    private final int baseWorkRateUpper;
    
    private final Level logLevel;
    private final Random rng;
    
    public CostMinimizingObjectiveFunction(int timesteps,
            Map<Integer, Integer> workloads,
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
        this.workloads = workloads;
        this.initialCapacity = initialCapacity;
        this.commissionTimeLower = commissionTimeLower;
        this.commissionTimeUpper = commissionTimeUpper;
        this.minimumCapacity = minimumCapacity;
        this.maximumCapacity = maximumCapacity;
        this.parallelizablePortion = parallelizablePortion;
        this.baseWorkRateLower = baseWorkRateLower;
        this.baseWorkRateUpper = baseWorkRateUpper;
        
        this.logLevel = logLevel;
        
        rng = new Random();
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
        
        for(int timestep = 1; timestep <= timesteps; timestep++) {
            /* don't commission additional consumers while other ones are waiting to come online to prevent overprovisioning */
            final boolean canCommission = queue.getQueuedConsumers() == 0;
            LOGGER.log(Level.INFO, "In timestep {0}, canCommission = {1}", new Object[]{timestep, canCommission});
            
            if (workloads.containsKey(timestep)) {
                queue.enqueueBatchWorkload(workloads.get(timestep));
            }
            
            simulation.stepSystem(timestep, canCommission);
        }
        final List<QueueConfigurationSnapshot> observedErrors = simulation.getObservedErrors();
        
        final int consumerTimeSum = observedErrors.stream().mapToInt(error -> error.activeConsumers).sum();
        
        final long leftoverJobs = queue.getEnqueuedJobs() - queue.getProcessedJobs();
        
        if (leftoverJobs < 0) {
            System.out.println("uh oh, something went wrong");
        }
        
        // penalize leftover jobs to encourage provisioning enough to process everything within the allotted timespan.
        // penalty factor of 100 is fairly arbitrary
        
        return 100.0*((double) leftoverJobs) + consumerTimeSum;
    }
    
    public static void main(String[] args) {
        final Map<Integer, Integer> workloads = new HashMap<>();
        final int timesteps = 500;
        Random rng = new Random();
        for(int i = 1; i <= timesteps; i += 25) {
            workloads.put(i, rng.nextInt(100) + 1000);
        }
        
        final int initialCapacity = 1;
        final int minimumCapacity = 1;
        final int maximumCapacity = 20;
        
        final int commissionTimeLower = 1;
        final int commissionTimeUpper = 2;
        
        final int baseWorkRateLower = 25;
        final int baseWorkRateUpper = 30;
        
        final double parallelizablePortion = 0.9;
        
        final ObjectiveFunction pidSystemSimulator = new CostMinimizingObjectiveFunction(
                timesteps, 
                workloads,
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
        
        System.out.println(pidSystemSimulator.evaluate(new double[] { 184.76064053278117, 58.607163034580715, 211.41643944338776, 3696.285636245719}, 0));
    }
}
