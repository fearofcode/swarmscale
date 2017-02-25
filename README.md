swarmscale
==========

This is an experiment in using <a href="https://en.wikipedia.org/wiki/Particle_swarm_optimization">particle swarm optimization</a>,
a simple heuristic optimization algorithm inspired by natural behavior such as flocks of birds and insect swarms, to automatically
tune <a href="https://en.wikipedia.org/wiki/PID_controller">PID controllers</a>.

Eventually, I'd like to try to apply this technique towards software systems. But, for now, I'm working on simulated physical systems using a physics engine.

So far, the example I'm working on is of levitating a circular object in the air. It's intended to be a highly simplified
simulation of something like hovering an aircraft in the air.

To do that, I'm using a physics engine, <a href="http://www.dyn4j.org/">dyn4j</a>, to power a simulation that actually
becomes the objective function in a particle swarm optimization setup.

Check out the class `PSOBallGravityDemo` for a demo of a controller whose parameters were derived by PSO.

`ZieglerNicholsBallGravityDemo` is an example of a controller using the more traditional 
<a href="https://en.wikipedia.org/wiki/Ziegler%E2%80%93Nichols_method">Ziegler-Nichols heuristic</a>.

Which is better? It depends on how you measure performance. The PSO optimizer uses total absolute error. By that metric,
the PSO system is substantially better, although both give decent performance. The PSO controller has higher overshoot, 
though.

`PSOvsZieglerNicholsBallGravityEvaluation` compares the total absolute error quantitatively in case you're curious about numbers.

TODO
----

- Low-overshoot ball-gravity controller objective function/demo if it works
- Move stable system factories to its own helper class
- Inverted pendulum example
- Picking/mouse manipulation for physical systems
- Charting! Either <a href="http://knowm.org/open-source/xchart/">Xchart</a> or just write to file and call gnuplot
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