package org.wkh.swarmscale.optimization;

public class ControlPerformanceResult {
    public final double time;
    public final double target;
    public final double actual;
    public final double error;
    
    public ControlPerformanceResult(double time, double target, double actual) {
        this(time, target, actual, Math.abs(target - actual));
    }
    
    public ControlPerformanceResult(double time, double target, double actual, double error) {
        this.time = time;
        this.target = target;
        this.actual = actual;
        
        this.error = error;
    }
}
