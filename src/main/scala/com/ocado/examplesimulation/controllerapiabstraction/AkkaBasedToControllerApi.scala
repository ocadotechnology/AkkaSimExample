package com.ocado.examplesimulation.controllerapiabstraction

import akka.actor.{ActorRef, ActorSystem}

class AkkaBasedToControllerApi(actorSystem: ActorSystem, controller: ActorRef) extends ToControllerApi {
  override def receive(obj: Any): Unit = controller ! obj

  override def shutdown(): Unit = actorSystem.terminate()
}
