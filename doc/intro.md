# Genetic Sandbox

High level overview:

- Genetically encode brains as signal graphs
- Implement simple mutation and cross-over of brains
- Design a simulation program to test fitness of brains
- Create a program for visualizing simulation results
- Borrow inspiration from HTM theory and HyperNEAT when it makes sense to do so
- Potentially distribute signal graph processing over many (virtual) machines

### Brains as Signal Graphs

It has come to my attention that brains can be modeled as large, recurrent
signal graphs of reactive computation. In this model, neurons act as
time-varying sources (and sinks) of data. Modeling recurrent neural networks in
a purely mathematical (i.e. matrices) manner with simultaneous genetic
representation is a difficult problem, and I argue that in its current state,
does not mimic true neural behavior. It is my belief that matrix representations
of neural networks make massive assumptions regarding the _timing_ in which
neural processing takes place, preferring implementations suited for running on
modern GPUs. While these implementations are efficient for certain problem sets,
I see them as lacking in accurate organic simulation, which is inherently
a time-sensitive domain.

As an example, in a matrix representation of a recurrent neural network, the
idea of a "layer" and a "time step" are required in order to carry out the
computation. A layer can be defined as a grouping of neurons sharing
hierarchically similar positions in the network, and are all simulated
_simultaneously_ via a matrix multiplication operation. 

The time step is normally defined by `dt` in the context of a differential
equation, relating the change in a given neuron's activity with its weighted,
upstream connections over time. In practical computational settings, the value
of `dt` must be explicitly chosen in order to step the simulation forward, a
choice I deem to be dangerously arbitrary.

These two concepts do not match biological intuition. In biology, it has been
found that, while neurons may be categorically grouped based on their position
in the hierarchy, they can differ drastically in their _actual physical positions_
within the brain. When `dt` is selected to emulate the speed of electric impulse,
physical position within the brain has a large effect on order of computation. Put
another way, at this scale, the physical distance between neurons _matters_.

An alternative to the classic matrix representation is found in reactive signal
graphs, in which neurons are treated as independent electrical impulse devices.
In this arrangement, the traditional notion of "connection weights" determining
the _magnitude_ of impulses is instead interpreted as the _travel time_ of
impulses between neurons. In this way, "connection weights" are analogous to
"connection distance". Practically speaking, these connection distances could be
implemented using scheduled computation. Naively, the thread of computation for
any given neuron could be made to sleep for _`D`_ milliseconds before performing
its computation and relaying the signal to downstream neurons, where _`D`_ is
the "connection distance". Or more interestingly, these connection distances
could be reified by distributing physically distant neurons over physically
distant computers/machines.

This repository will serve as an experimental bed for testing these theories.
