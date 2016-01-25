package com.ocado.examplesimulation

import akka.actor.{ActorSystem, Props}
import com.ocado.event.scheduling.{ExecutorEventScheduler, SimpleDiscreteEventScheduler}
import com.ocado.examplecontroller.externalapi.outward.ToMechanismAPI
import com.ocado.examplecontroller.{AkkaBasedController, SimpleController}
import com.ocado.examplesimulation.AkkaSchedulerMode.AkkaSchedulerMode
import com.ocado.examplesimulation.ControllerMode.ControllerMode
import com.ocado.examplesimulation.CoreSchedulerMode.CoreSchedulerMode
import com.ocado.examplesimulation.controllerapiabstraction.{AkkaBasedToControllerApi, SimpleToControllerApi}
import com.ocado.javautils.Runnables.toRunnable
import com.ocado.time.{AdjustableTimeProvider, UtcTimeProvider}
import com.typesafe.config.ConfigFactory
import com.ocado.event.scheduling.AkkaDiscreteEventScheduler

object CoreSchedulerMode extends Enumeration {
  type CoreSchedulerMode = Value
  val RealTime, DiscreteEvent = Value
}

object ControllerMode extends Enumeration {
  type ControllerMode = Value
  val SimpleController, AkkaController = Value
}

object AkkaSchedulerMode extends Enumeration {
  type AkkaSchedulerMode = Value
  val Default, AkkaDiscreteEventScheduler = Value
}

object StartUp extends App {
  val coreSchedulerMode = CoreSchedulerMode.withName(args(0))
  val controllerMode = ControllerMode.withName(args(1))
  val akkaSchedulerMode = controllerMode match {
    case ControllerMode.AkkaController => Option(AkkaSchedulerMode.withName(args(2)))
    case other => Option.empty
  }

  val scheduler = createCoreScheduler()

  val toMechanismApi = new ToMechanismAPI()

  val controller = createController()

  val simulation = new Simulation(scheduler, controller)

  toMechanismApi.setSimulation(simulation)

  scheduler.doAt(0, () => simulation.run(), "run simulation")

  def createController() = controllerMode match {
    case ControllerMode.AkkaController =>
      val actorSystem = akkaSchedulerMode.getOrElse(AkkaSchedulerMode.Default) match {
        case AkkaSchedulerMode.Default => ActorSystem.create()
        case AkkaSchedulerMode.AkkaDiscreteEventScheduler =>
          val config = ConfigFactory
            .parseString(s"akka.scheduler.implementation=${classOf[AkkaDiscreteEventScheduler].getName}")
            .withFallback(ConfigFactory.load())
          ActorSystem.create("ControllerActorSystem", config)
      }

      val controller = actorSystem.actorOf(Props(new AkkaBasedController(toMechanismApi)), "Controller")

      new AkkaBasedToControllerApi(actorSystem, controller)
    case ControllerMode.SimpleController => new SimpleToControllerApi(new SimpleController(toMechanismApi))
  }

  def createCoreScheduler() = coreSchedulerMode match {
    case CoreSchedulerMode.DiscreteEvent => new SimpleDiscreteEventScheduler(new AdjustableTimeProvider(0), () => controller.shutdown())
    case CoreSchedulerMode.RealTime => new ExecutorEventScheduler(new UtcTimeProvider())
  }
}
