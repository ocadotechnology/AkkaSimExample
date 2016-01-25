package com.ocado.examplecontroller

import akka.actor.Actor
import com.ocado.examplecontroller.externalapi.outward.ControllerToMechanismApi

class AkkaBasedController(api: ControllerToMechanismApi) extends Actor {
  var simpleController = new SimpleController(api)

  override def receive = {
    case x => simpleController.receive(x)
  }
}
