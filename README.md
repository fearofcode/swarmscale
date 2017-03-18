swarmscale
==========

This started as an experiment in using <a href="https://en.wikipedia.org/wiki/Particle_swarm_optimization">particle swarm optimization</a>,
a simple heuristic optimization algorithm inspired by natural behavior such as flocks of birds and insect swarms, to automatically
tune <a href="https://en.wikipedia.org/wiki/PID_controller">PID controllers</a>.

The goal was to work towards automatically scaling queue systems, such as Apache Kafka consumers.

The project has since become a general investigation of using evolutionary methods for control.

The current problem to be tackled is the <a href="https://en.wikipedia.org/wiki/Inverted_pendulum">inverted pendulum</a>, also known as the pole-balancing problem, a classic control problem.