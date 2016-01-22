package com.ocado.examplesimulation.controllerapiabstraction

import akka.actor.{ActorRef, ActorSystem}

class AkkaBasedControllerApi(actorSystem: ActorSystem, controller: ActorRef) extends ControllerApi {
  override def receive(obj: Any): Unit = controller ! obj

  override def shutdown(): Unit = actorSystem.terminate()
}
