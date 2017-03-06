swarmscale
==========

This is an experiment in using <a href="https://en.wikipedia.org/wiki/Particle_swarm_optimization">particle swarm optimization</a>,
a simple heuristic optimization algorithm inspired by natural behavior such as flocks of birds and insect swarms, to automatically
tune <a href="https://en.wikipedia.org/wiki/PID_controller">PID controllers</a>.

The goal is to be able to scale an Apache Kafka cluster up and down dynamically.

So far, there are a few working examples: levitating a circular object in the air (what I'm calling the "ball-gravity system"), the inverted pendulum, and the queue simulator

Ball-gravity system
-------------------

The ball-gravity system consists of a small circular object levitating in the air. It's intended to be a highly simplified
simulation of something like hovering an aircraft in the air.

To do that, I'm using a physics engine, <a href="http://www.dyn4j.org/">dyn4j</a>, to power a simulation that actually
becomes the objective function in a particle swarm optimization setup.

Check out the class `PSOBallGravityDemo` for a demo of a controller whose parameters were derived by PSO.

`StableControllersFactory` has other sets of parameters that more or less solve the problem but display different behavior:
more or less overshoot, faster convergence to setpoint changes, higher or lower steady-state error, etc.

`ZieglerNicholsBallGravityDemo` is an example of a controller using the more traditional 
<a href="https://en.wikipedia.org/wiki/Ziegler%E2%80%93Nichols_method">Ziegler-Nichols heuristic</a>.

Which is better? It depends on how you measure performance. The PSO optimizer uses total absolute error. By that metric,
the PSO system is substantially better, although both give decent performance. The PSO controller has higher overshoot, 
though.

`PSOvsZieglerNicholsBallGravityEvaluation` compares the total absolute error quantitatively in case you're curious about numbers.

Inverted pendulum
-----------------

`PIDControlledInvertedPendulumSystem` has a demo of a PD (no integral) controller. The cart drifts to the right for some
reason, but the pole stays stationary.

Queue simulator
---------------

`CostMinimizingOptimizer` will simulate a cluster that works similarly to Apache Kafka with consumers and partitions.

It will try to process the workload it's given in the time allotted while minimizing consumers used over time.

`PIDControlledQueueSimulation` will run an example with values returned from an optimization run.

It seems to correctly handle scaling stuff up and down.

The clear next step is running this on a real cluster.

TODO
----

- Coordinate ascent for refining/local search on optimizer results
- End to end pipeline of PSO -> coordinate ascent -> visualized/verbose output
- Picking/mouse manipulation for physical systems
- Charting! Either <a href="http://knowm.org/open-source/xchart/">Xchart</a> or just write to file and call gnuplot
- Setup Maven fat JAR builds to make this project easier for people to run/to create releases off of
- Actually document code and write actual tests once there's evidence this can even work
- Other interactivity stuff (e.g. set control parameters; enable/disable control, etc)
- Visualize population positions for dimension <= 3; write out whole population to allow animation creation
- Durable checkpoint/restart to break up long sessions into multiple runs
- Clean up output stuff and log stuff to files instead of/in addition to stdout

Acknowledgments
----------------

<a href="https://thenounproject.com/term/quadcopter/301553/">Quadcopter image</a> is used under a 
<a href="https://creativecommons.org/licenses/by/3.0/us/">Creative Commons</a> license.

PID code in `PIDController.java` is originally from https://github.com/tekdemo/MiniPID-Java . The license for that can be found in 
`LICENSE.minipid`.