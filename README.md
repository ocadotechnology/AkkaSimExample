# AkkaSimExample

This example demonstrates an Akka based controller or a non-Akka based controller, running against a discrete event simulation or a real-time simulation.

## Execution Cases

Non-Akka controller against discrete event simulation gives deterministic, faster than real-time simulation. To run, execute StartUp with arguments of "DiscreteEvent SimpleController".

Akka controller against real-time simulation gives non-deterministic, real-time simulation, where the events happen roughly when they should. To run, execute StartUp with arguments of "RealTime AkkaController Default".

Akka controller against discrete event simulation ___should___ give deterministic, faster than real-time simulation as per “non-Akka controller against discrete event simulation”, but currently instead gives non-deterministic, faster than real-time simulation, where the events happen much later than they should in terms of simulation time, because the discrete event simulation has run way ahead of the controller, because the controller is doing its own threading (via Akka). This is the case that requires alteration to function as desired. To run, execute StartUp with arguments of "DiscreteEvent AkkaController Default".

When running an Akka controller against a discrete event simulation, you can make Akka utilise a discrete event scheduler, by providing an alternative implementation of Akka’s Scheduler that is discrete event scheduler backed. This currently crashes due to a logging related Await call. To run, execute StartUp with arguments of "DiscreteEvent AkkaController AkkaDiscreteEventScheduler".
