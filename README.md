# AkkaSimExample

This example demonstrates an Akka based controller or a non-Akka based controller, running against a discrete event simulation or a real-time simulation.

The test case has a controller that instructs a mechanism where to send objects that arrive at the mechanism.

## Execution Cases

### Non-Akka controller against discrete event simulation

This gives deterministic, faster than real-time simulation. To run, execute StartUp with arguments of "DiscreteEvent SimpleController".

### Akka controller against real-time simulation

This gives non-deterministic, real-time simulation, where the events happen roughly when they should. To run, execute StartUp with arguments of "RealTime AkkaController Default Default".

### Akka controller against discrete event simulation

This ___should___ give deterministic, faster than real-time simulation as per “non-Akka controller against discrete event simulation”, but without overriding some Akka behaviour, gives non-deterministic, faster than real-time simulation, where the events happen much later than they should in terms of simulation time, because the discrete event simulation has run way ahead of the controller, because the controller is doing its own threading (via Akka). This is the case that requires alteration to function as desired. To run, execute StartUp with arguments of "DiscreteEvent AkkaController Default Default".

#### Solution 1: Make Akka block instead of multi-thread

When running an Akka controller against a discrete event simulation, you can make Akka block on `tell` and `ask`, bu providing a system ExecutionContext that blocks on `execute` instead of delegating to other threads. This currently causes the system to give deterministic, faster than real-time simulation, however it will not work in a variety of cases. For example, A iterates over collection C, asking B something about each item. B responds to A, causing C to be updated. ConcurrentModificationExceptions abound in a blocking implementation. An example of this situation should be added. ___For this reason, this solution is not likely to be of use in many applications.___ To run, execute StartUp with arguments of "DiscreteEvent AkkaController Default Blocking".

#### Solution 2: Make Akka schedule events instead of multi-thread

When running an Akka controller against a discrete event simulation, you can make Akka utilise a discrete event scheduler, by providing an alternative implementation of Akka’s Scheduler that is discrete event scheduler backed, and an ExecutionContext that is discrete event scheduler backed. This means that Await calls will fail, as they will be awaiting things that can only happen after the current Event. To make this run correctly, Akka's `Logging` has been hacked (copied into this codebased and modified) to not Await on the result of initialising a logger. This should be altered to depend on a fork of Akka so as to avoid copying the code. This currently causes the system to give deterministic, faster than real-time simulation. To run, execute StartUp with arguments of "DiscreteEvent AkkaController AkkaDiscreteEventScheduler Default".

## How it works

See the [architecture diagram](https://docs.google.com/drawings/d/15lsfo0Jk5hpzLO63QIYJOyPnwg-a1RAxbwk5Zj5yRjM/edit?usp=sharing) for a high level overview.

`StartUp` creates an `EventScheduler`, `Controller` and `Simulation`. It hooks the `Controller` and `Simulation` together via `ControllerToMechanismApi` and `SimulationToControllerApi`. It schedules a call to `run` the `Simulation` such that it executes via the `EventScheduler`.

The `Simulation` creates a `Mechanism`, and schedules telling the `Controller` about the `Plan` of what to send where. It seperately schedules the `Mechanism` to `feed` the first object. It also schedules an `Event` that either re-schedules itself when executed, or terminates the program if the `Mechanism` has run out of work.

The `Controller` receives the `Plan` and the message that the `Mechanism` sends to say that an `ObjectArrived`. It then decides where to send the object based on the `Plan` and tells this to the `Mechanism`.

The `Mechanism` schedules the object to move to the destination. When this `Event` executes, it schedules another `feed` to the `Mechanism`. When the feed `Event` is executed, another `ObjectReceived` message is sent to the `Controller` and the loop repeats.

