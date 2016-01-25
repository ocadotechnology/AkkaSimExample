package com.ocado.examplecontroller

import akka.actor.Actor
import com.ocado.examplecontroller.externalapi.outward.ToMechanismAPI

class AkkaBasedController(api: ToMechanismAPI) extends Actor {
  var simpleController = new SimpleController(api)

  override def receive = {
    case x => simpleController.receive(x)
  }
}
