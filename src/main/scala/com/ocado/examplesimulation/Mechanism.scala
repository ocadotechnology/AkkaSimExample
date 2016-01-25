package com.ocado.examplesimulation

import com.ocado.event.scheduling.EventScheduler
import com.ocado.examplecontroller.externalapi.inward.ObjectArrived
import com.ocado.examplesimulation.controllerapiabstraction.SimulationToControllerApi
import com.ocado.javautils.Runnables.toRunnable

class Mechanism(scheduler: EventScheduler, controller: SimulationToControllerApi, orderedObjectsToFeed: List[Int]) {
  var nextIndexToFeed = 0
  var currentObject: Option[Int] = None

  def feed() = {
    currentObject = Option(orderedObjectsToFeed(nextIndexToFeed))
    controller.receive(ObjectArrived(currentObject.get))

    nextIndexToFeed += 1
  }

  def moveTo(obj: Int, destination: Int) = {
    scheduler.doNow(() => {
      if (!currentObject.contains(obj)) {
        throw new IllegalStateException(s"Controller instructed mechanism to move $obj which is not present")
      }

      scheduler.doIn(2000, () => performMoveTo(destination), "move to")
    },
    "defer scheduling to avoid TimeProvider races")
  }

  private def performMoveTo(destination: Int) = {
    println(s"Moved ${currentObject.get} to $destination")
    currentObject = None

    if (!hasFinishedFeeding) {
      scheduler.doIn(1000, () => feed(), "feed")
    }
  }

  def hasFinishedFeeding = nextIndexToFeed == orderedObjectsToFeed.size

  def hasFinishedMovingTo = hasFinishedFeeding && currentObject.isEmpty
}
