package com.ocado.examplesimulation.controllerapiabstraction

import akka.actor.{ActorRef, ActorSystem}

class AkkaBasedSimulationToControllerApi(actorSystem: ActorSystem, controller: ActorRef) extends SimulationToControllerApi {
  override def receive(obj: Any): Unit = controller ! obj

  override def shutdown(): Unit = actorSystem.terminate()
}
