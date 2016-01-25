# AkkaSimExample

This example demonstrates an Akka based controller or a non-Akka based controller, running against a discrete event simulation or a real-time simulation.

The test case has a controller that instructs a mechanism where to send objects that arrive at the mechanism.

## Execution Cases

Non-Akka controller against discrete event simulation gives deterministic, faster than real-time simulation. To run, execute StartUp with arguments of "DiscreteEvent SimpleController".

Akka controller against real-time simulation gives non-deterministic, real-time simulation, where the events happen roughly when they should. To run, execute StartUp with arguments of "RealTime AkkaController Default".

Akka controller against discrete event simulation ___should___ give deterministic, faster than real-time simulation as per “non-Akka controller against discrete event simulation”, but currently instead gives non-deterministic, faster than real-time simulation, where the events happen much later than they should in terms of simulation time, because the discrete event simulation has run way ahead of the controller, because the controller is doing its own threading (via Akka). This is the case that requires alteration to function as desired. To run, execute StartUp with arguments of "DiscreteEvent AkkaController Default".

When running an Akka controller against a discrete event simulation, you can make Akka utilise a discrete event scheduler, by providing an alternative implementation of Akka’s Scheduler that is discrete event scheduler backed. This currently crashes due to a logging related Await call. To run, execute StartUp with arguments of "DiscreteEvent AkkaController AkkaDiscreteEventScheduler".

## How it works

See the [architecture diagram](https://docs.google.com/drawings/d/15lsfo0Jk5hpzLO63QIYJOyPnwg-a1RAxbwk5Zj5yRjM/edit?usp=sharing) for a high level overview.

`StartUp` creates an `EventScheduler`, `Controller` and `Simulation`. It hooks the `Controller` and `Simulation` together via `ControllerToMechanismApi` and `SimulationToControllerApi`. It schedules a call to `run` the `Simulation` such that it executes via the `EventScheduler`.

The `Simulation` creates a `Mechanism`, and schedules telling the `Controller` about the `Plan` of what to send where. It seperately schedules the `Mechanism` to `feed` the first object. It also schedules an `Event` that either re-schedules itself when executed, or terminates the program if the `Mechanism` has run out of work.

The `Controller` receives the `Plan` and the message that the `Mechanism` sends to say that an `ObjectArrived`. It then decides where to send the object based on the `Plan` and tells this to the `Mechanism`.

The `Mechanism` schedules the object to move to the destination. When this `Event` executes, it schedules another `feed` to the `Mechanism`. When the feed `Event` is executed, another `ObjectReceived` message is sent to the `Controller` and the loop repeats.

