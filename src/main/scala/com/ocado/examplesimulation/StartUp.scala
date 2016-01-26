package com.ocado.examplesimulation

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.ocado.event.scheduling.{AkkaDiscreteEventScheduler, ExecutorEventScheduler, SimpleDiscreteEventScheduler}
import com.ocado.examplecontroller.externalapi.outward.ControllerToMechanismApi
import com.ocado.examplecontroller.{AkkaBasedController, SimpleController}
import com.ocado.examplesimulation.controllerapiabstraction.{AkkaBasedSimulationToControllerApi, SimpleSimulationToControllerApi}
import com.ocado.javautils.Runnables.toRunnable
import com.ocado.time.{AdjustableTimeProvider, UtcTimeProvider}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

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

object AkkaExecutionContextMode extends Enumeration {
  type AkkaSchedulerMode = Value
  val Default, Blocking = Value
}

object StartUp extends App {
  val coreSchedulerMode = CoreSchedulerMode.withName(args(0))

  val controllerMode = ControllerMode.withName(args(1))

  val akkaSchedulerMode = controllerMode match {
    case ControllerMode.AkkaController => Option(AkkaSchedulerMode.withName(args(2)))
    case other => Option.empty
  }

  val akkaExecutionContextMode = controllerMode match {
    case ControllerMode.AkkaController => Option(AkkaExecutionContextMode.withName(args(3)))
    case other => Option.empty
  }

  val scheduler = createCoreScheduler()

  val toMechanismApi = new ControllerToMechanismApi()

  val controller = createController()

  val simulation = new Simulation(scheduler, controller)

  toMechanismApi.setSimulation(simulation)

  scheduler.doAt(0, () => simulation.run(), "run simulation")

  def createController() = controllerMode match {
    case ControllerMode.AkkaController =>
      implicit val timeout = Timeout(1, TimeUnit.MINUTES)

      val config = akkaSchedulerMode.getOrElse(AkkaSchedulerMode.Default) match {
        case AkkaSchedulerMode.Default => None
        case AkkaSchedulerMode.AkkaDiscreteEventScheduler =>
          Option(ConfigFactory.parseString(s"akka.scheduler.implementation=${classOf[AkkaDiscreteEventScheduler].getName}")
            .withFallback(ConfigFactory.load()))
      }

      val executionContextOption: Option[ExecutionContext] = akkaExecutionContextMode.getOrElse(AkkaExecutionContextMode.Default) match {
        case AkkaExecutionContextMode.Default => None
        case AkkaExecutionContextMode.Blocking =>
          implicit val executionContext = new ExecutionContext {
            override def reportFailure(cause: Throwable): Unit = throw cause

            override def execute(runnable: Runnable): Unit = runnable.run()
          }
          Option(executionContext)
      }

      val actorSystem = ActorSystem("ControllerActorSystem", config, None, executionContextOption)

      val controller = actorSystem.actorOf(Props(new AkkaBasedController(toMechanismApi)), "Controller")

      new AkkaBasedSimulationToControllerApi(actorSystem, controller)
    case ControllerMode.SimpleController => new SimpleSimulationToControllerApi(new SimpleController(toMechanismApi))
  }

  def createCoreScheduler() = coreSchedulerMode match {
    case CoreSchedulerMode.DiscreteEvent => new SimpleDiscreteEventScheduler(new AdjustableTimeProvider(0), () => controller.shutdown())
    case CoreSchedulerMode.RealTime => new ExecutorEventScheduler(new UtcTimeProvider())
  }
}
