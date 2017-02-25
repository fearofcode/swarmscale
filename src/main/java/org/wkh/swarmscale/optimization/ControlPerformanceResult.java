package org.wkh.swarmscale.optimization;

public class ControlPerformanceResult {
    public final double time;
    public final double target;
    public final double actual;
    public final double error;
    
    public ControlPerformanceResult(double time, double target, double actual) {
        this.time = time;
        this.target = target;
        this.actual = actual;
        
        error = Math.abs(target - actual);
    }
}
