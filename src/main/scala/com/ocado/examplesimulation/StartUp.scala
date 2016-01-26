package com.ocado.examplesimulation

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.ocado.event.scheduling._
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
  val Default, Blocking, DiscreteEvent = Value
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

  val terminator = new Terminator()
  val scheduler = createCoreScheduler(terminator)
  EventSchedulerHolder.scheduler = scheduler //TODO: find a better way of exposing a scheduler to AkkaDiscreteEventScheduler

  scheduler.doNow(() => startUp(), "start up")

  def startUp(): Unit = {
    val toMechanismApi = new ControllerToMechanismApi()

    val controller = createController(toMechanismApi)
    terminator.setController(controller)

    val simulation = new Simulation(scheduler, controller)

    toMechanismApi.setSimulation(simulation)

    scheduler.doAt(0, () => simulation.run(), "run simulation")
  }

  def createController(toMechanismApi: ControllerToMechanismApi) = controllerMode match {
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
        case AkkaExecutionContextMode.DiscreteEvent =>
          implicit val executionContext = new ExecutionContext {
            override def reportFailure(cause: Throwable): Unit = throw cause

            override def execute(runnable: Runnable): Unit = scheduler.doNow(runnable, "execution context invocation")
          }
          Option(executionContext)
      }

      val actorSystem = ActorSystem("ControllerActorSystem", config, None, executionContextOption)

      val controller = actorSystem.actorOf(Props(new AkkaBasedController(toMechanismApi)), "Controller")

      new AkkaBasedSimulationToControllerApi(actorSystem, controller)
    case ControllerMode.SimpleController => new SimpleSimulationToControllerApi(new SimpleController(toMechanismApi))
  }

  def createCoreScheduler(terminator: Terminator): EventScheduler = coreSchedulerMode match {
    case CoreSchedulerMode.DiscreteEvent => new SimpleDiscreteEventScheduler(new AdjustableTimeProvider(0), () => terminator.shutdown())
    case CoreSchedulerMode.RealTime => new ExecutorEventScheduler(new UtcTimeProvider())
  }
}
