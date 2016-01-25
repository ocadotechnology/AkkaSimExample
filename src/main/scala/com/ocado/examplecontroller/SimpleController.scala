package com.ocado.examplecontroller

import com.ocado.examplecontroller.externalapi.inward.{ObjectArrived, Plan}
import com.ocado.examplecontroller.externalapi.outward.ToMechanismAPI

class SimpleController(api: ToMechanismAPI) {
  var currentPlan: Option[Plan] = None

  var currentObject: Option[Int] = None

  def receive(any: Any) = any match {
    case "test" => println("Controller received test message")

    case plan: Plan =>
      println("Controller received " + plan)
      currentPlan = Option(plan)
      sendInstructionsForCurrentObject()

    case objectArrived: ObjectArrived =>
      println(s"Controller received $objectArrived")
      currentObject = Option(objectArrived.obj)
      sendInstructionsForCurrentObject()


    case unknown => println(s"Controller received unknown message: $unknown")
  }

  def sendInstructionsForCurrentObject() = {
    currentPlan.foreach(plan => {
      currentObject.foreach(obj => {
        plan.objectsToMove.get(obj).foreach(destination => {
          println(s"moving $obj to $destination")
          api.moveTo(obj, destination)
        })
      })
    })
  }
}
