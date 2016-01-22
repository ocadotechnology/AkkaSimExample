package com.ocado.examplesimulation

import akka.actor.{ActorSystem, Props}
import com.ocado.event.scheduling.{ExecutorEventScheduler, SimpleDiscreteEventScheduler}
import com.ocado.examplecontroller.externalapi.outward.ControllerToMechanismAPI
import com.ocado.examplecontroller.{AkkaBasedController, SimpleController}
import com.ocado.examplesimulation.controllerapiabstraction.{AkkaBasedControllerApi, SimpleControllerApi}
import com.ocado.javautils.Runnables.toRunnable
import com.ocado.time.{AdjustableTimeProvider, UtcTimeProvider}

object StartUp extends App {
  val scheduler = createScheduler(args(0).toBoolean)

  val toMechanismApi = new ControllerToMechanismAPI()

  val controller = createController(args(1).toBoolean)

  val simulation = new Simulation(scheduler, controller)

  toMechanismApi.setSimulation(simulation)

  scheduler.doAt(0, () => simulation.run(), "run simulation")

  def createController(controlWithAkka: Boolean) = controlWithAkka match {
    case true =>
      val actorSystem = ActorSystem.create()
      val controller = actorSystem.actorOf(Props(new AkkaBasedController(toMechanismApi)), "Controller")
      new AkkaBasedControllerApi(actorSystem, controller)
    case false => new SimpleControllerApi(new SimpleController(toMechanismApi))
  }

  def createScheduler(discrete: Boolean) = discrete match {
    case true => new SimpleDiscreteEventScheduler(new AdjustableTimeProvider(0), () => controller.shutdown())
    case false => new ExecutorEventScheduler(new UtcTimeProvider())
  }
}
