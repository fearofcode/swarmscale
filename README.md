swarmscale
==========

This is an experiment in using <a href="https://en.wikipedia.org/wiki/Particle_swarm_optimization">particle swarm optimization</a>,
a simple heuristic optimization algorithm inspired by natural behavior such as flocks of birds and insect swarms, to automatically
tune <a href="https://en.wikipedia.org/wiki/PID_controller>PID controllers</a>.

Eventually, I'd like to try to apply this technique towards software systems. But, for now, I'm working on simulated physical systems using a physics engine.

So far, the example I'm working on is of levitating a circular object in the air. It's intended to be a highly simplified
simulation of something like hovering an aircraft in the air.

To do that, I'm using a physics engine, <a href="http://www.dyn4j.org/">dyn4j</a>, to power a simulation that actually
becomes the objective function in a particle swarm optimization setup.

Check out the classes `PSOBallGravityDemo` for a demo of a controller whose parameters were derived by PSO.

`ZieglerNicholsBallGravityDemo` is an example of a controller using the more traditional 
<a href="https://en.wikipedia.org/wiki/Ziegler%E2%80%93Nichols_method">Ziegler-Nichols heuristic</a>.

Which is better? It depends on how you measure performance. The PSO optimizer uses total absolute error. By that metric,
the PSO system is substantially better, although both give decent performance.

`PSOvsZieglerNicholsBallGravityEvaluation` compares the two side by side in case you're curious about numbers.

Acknowledgments
----------------

<a href="https://thenounproject.com/term/quadcopter/301553/">Quadcopter image</a> is used under a 
<a href="https://creativecommons.org/licenses/by/3.0/us/">Creative Commons</a> license.

PID code in `PIDController.java` is originally from https://github.com/tekdemo/MiniPID-Java . The license for that can be found in 
`LICENSE.minipid`.