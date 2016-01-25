package com.ocado.examplesimulation

import com.ocado.event.scheduling.EventScheduler
import com.ocado.examplecontroller.externalapi.inward.Plan
import com.ocado.examplesimulation.controllerapiabstraction.ControllerApi
import com.ocado.javautils.Runnables.toRunnable

import scala.util.Random

class Simulation(scheduler: EventScheduler, controller: ControllerApi) {
  /**
    * Without this, the system would not be deterministic
    */
  val RANDOM_SEED = 0 //TODO: make this config to support mote-carlo sweeps

  val plan: Plan = new Plan(Map(1 -> 0, 3 -> 0, 4 -> 0, 5 -> 0, 7 -> 0, 2 -> 1, 6 -> 1, 8 -> 1, 9 -> 1))

  val objectsToFeedInRandomOrder: List[Int] = new Random(RANDOM_SEED).shuffle(plan.objectsToMove.keySet.toList)

  val mechanism: Mechanism = new Mechanism(scheduler, controller, objectsToFeedInRandomOrder)

  def run() {
    controller.receive("test")
    controller.receive("jibberish")

    println(s"Time: ${scheduler.getTimeProvider.getTime}")

    scheduler.doIn(2000, () => controller.receive(plan), "tell controller what the plan is")

    scheduler.doIn(4000, () => mechanism.feed(), "initial feed")

    val terminationEventDescription = "termination check event"
    scheduler.doIn(5000, new Runnable {
      override def run(): Unit = {
        if (mechanism.hasFinishedMovingTo) {
          controller.shutdown()
          scheduler.stop()
        } else {
          scheduler.doIn(1000, this, terminationEventDescription)
        }
      }
    }, terminationEventDescription)
  }

  def moveTo(obj: Int, destination: Int) = {
    if (!mechanism.currentObject.contains(obj)) {
      throw new IllegalStateException(s"Controller instructed mechanism to move $obj which is not present")
    }

    scheduler.doNow(
      () => scheduler.doIn(2000, () => mechanism.moveTo(destination), "move to"),
      "defer scheduling to avoid TimeProvider races")
  }
}
