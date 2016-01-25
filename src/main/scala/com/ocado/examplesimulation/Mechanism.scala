package com.ocado.examplesimulation

import com.ocado.event.scheduling.EventScheduler
import com.ocado.examplecontroller.externalapi.inward.ObjectArrived
import com.ocado.examplesimulation.controllerapiabstraction.ControllerApi
import com.ocado.javautils.Runnables.toRunnable

class Mechanism(scheduler: EventScheduler, controller: ControllerApi, orderedObjectsToFeed: List[Int]) {
  var nextIndexToFeed = 0
  var currentObject: Option[Int] = None

  def feed() = {
    currentObject = Option(orderedObjectsToFeed(nextIndexToFeed))
    controller.receive(ObjectArrived(currentObject.get))

    nextIndexToFeed += 1
  }

  def moveTo(destination: Int) = {
    println(s"Moved ${currentObject.get} to $destination")
    currentObject = None

    if (!hasFinishedFeeding) {
      scheduler.doIn(1000, () => feed(), "feed")
    }
  }

  def hasFinishedFeeding = nextIndexToFeed == orderedObjectsToFeed.size

  def hasFinishedMovingTo = hasFinishedFeeding && currentObject.isEmpty
}
