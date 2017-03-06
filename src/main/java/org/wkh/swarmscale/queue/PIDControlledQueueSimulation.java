package org.wkh.swarmscale.queue;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wkh.swarmscale.optimization.PIDController;

public class PIDControlledQueueSimulation  {
    public final static Logger LOGGER = Logger.getLogger(CostMinimizingObjectiveFunction.class.getName());
    private final PIDController controller;
    private final QueueSimulation queueSimulation;
    private final int setpoint;
    private final List<QueueConfigurationSnapshot> observedErrors;
    
    public List<QueueConfigurationSnapshot> getObservedErrors() {
        return observedErrors;
    }
    
    public PIDControlledQueueSimulation(PIDController controller, int setpoint, QueueSimulation queueSimulation,
            Level logLevel) {
        this.controller = controller;
        this.setpoint = setpoint;
        this.queueSimulation = queueSimulation;
        
        observedErrors = new LinkedList<>();
        LOGGER.setLevel(logLevel);
    }
    
    public void stepSystem(int timestamp, boolean canCommissionConsumers) {
        queueSimulation.stepSystem(timestamp);
        final long totalLag = queueSimulation.getTotalLag();
        final double controllerOutput = controller.getOutput((double)totalLag, (double)setpoint);
        
        final int targetConsumers = (int) Math.round(controllerOutput);
        
        final int actualConsumers = queueSimulation.getActiveConsumers();
        
        if (actualConsumers < targetConsumers) {
            if (!canCommissionConsumers) {
                return;
            }
            
            for(int i = 1; i <= targetConsumers - actualConsumers; i++) {
                if (queueSimulation.getActiveConsumers() + queueSimulation.getQueuedConsumers() < queueSimulation.getMaximumCapacity()) {
                    queueSimulation.commissionConsumer(timestamp);
                }
            }
        } else if (targetConsumers < actualConsumers) {
            for(int i = 1; i <= actualConsumers - targetConsumers; i++) {
                /* we can't directly take down a consumer. we have to mark the partition as inactive and let the system
                deactivate it when there are no jobs left
                */
                queueSimulation.deactivatePartition();
            }
        }
        
        if (queueSimulation.getBatchLag() > 0 && !queueSimulation.consumersQueued()) {
            LOGGER.log(Level.INFO, "Queueing {0} jobs to batch partition", queueSimulation.getBatchLag());
            queueSimulation.distributeBatchWorkToPartitions(queueSimulation.getBatchLag());
        }
        
        // actually don't care about setpoint error, so not going to record it
        // why don't we care about setpoint error? because of the goals of the project: to provision the minimal
        // number of consumers for the least amount of time needed to process a set of queued jobs.
        observedErrors.add(new QueueConfigurationSnapshot(actualConsumers, totalLag, queueSimulation.getProcessedJobs()));
    }
    
    public static void main(String[] args) {
        double[] position = new double[] { 427.10876628818085, 225.17153480024274, 0.0, 1000.0};
        
        double proportionalGain = position[0];
        double integralGain = position[1];
        double derivativeGain = position[2];
        
        int setpoint = (int)Math.round(position[3]);
        
        final int initialWorkload = 50000;
        final int timesteps = 500;
        
        final int initialCapacity = 1;
        final int minimumCapacity = 1;
        final int maximumCapacity = 20;
        
        final int commissionTimeLower = 1;
        final int commissionTimeUpper = 2;
        
        final int baseWorkRateLower = 15;
        final int baseWorkRateUpper = 20;
        
        final double parallelizablePortion = 0.9;
        
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
        
        QueueSimulation.LOGGER.setLevel(Level.SEVERE);
        
        PIDControlledQueueSimulation simulation = new PIDControlledQueueSimulation(controller, setpoint, queue, Level.SEVERE);
        
        queue.enqueueBatchWorkload(initialWorkload);
        
        for(int timestep = 1; timestep <= timesteps; timestep++) {
            /* don't commission additional consumers while other ones are waiting to come online to prevent overprovisioning */
            final boolean canCommission = queue.getQueuedConsumers() == 0;
            LOGGER.log(Level.INFO, "In timestep {0}, canComission = {1}", new Object[]{timestep, canCommission});
            simulation.stepSystem(timestep, canCommission);
        }
        final List<QueueConfigurationSnapshot> observedErrors = simulation.getObservedErrors();
        
        int i = 1;
        System.out.printf("Time\tActive Consumers\tTotal Lag\n");
        for(QueueConfigurationSnapshot error : observedErrors) {
            System.out.printf("%d\t%d\t%d\n", i, error.activeConsumers, error.totalLag);
            i++;
        }
    }
}
