package com.ocado.examplecontroller

import akka.actor.Actor
import com.ocado.examplecontroller.externalapi.outward.ControllerToMechanismAPI

class AkkaBasedController(api: ControllerToMechanismAPI) extends Actor {
  var simpleController = new SimpleController(api)

  override def receive = {
    case x => simpleController.receive(x)
  }
}
