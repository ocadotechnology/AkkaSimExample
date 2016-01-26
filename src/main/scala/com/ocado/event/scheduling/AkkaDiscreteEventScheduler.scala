package com.ocado.event.scheduling

import java.util.concurrent.ThreadFactory

import akka.actor.{Cancellable, Scheduler}
import akka.event.LoggingAdapter
import com.ocado.event.Event
import com.ocado.javautils.Runnables.toRunnable
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class AkkaDiscreteEventScheduler(config: Config, log: LoggingAdapter, threadFactory: ThreadFactory) extends Scheduler {
  val discreteEventScheduler = EventSchedulerHolder.scheduler

  override def scheduleOnce(delay: FiniteDuration, runnable: Runnable)(implicit executor: ExecutionContext): Cancellable =
    new CancellableEvent(discreteEventScheduler.doIn(delay.length, runnable, "Akka scheduleOnce"))

  override def schedule(initialDelay: FiniteDuration, interval: FiniteDuration, runnable: Runnable)(implicit executor: ExecutionContext): Cancellable =
    new Repeater(runnable, initialDelay, interval)

  override def maxFrequency = 1

  private class CancellableEvent(event: Event) extends Cancellable {
    var cancelled = false

    def cancel() = {
      discreteEventScheduler.cancel(event)
      cancelled = true
      true
    }

    def isCancelled = cancelled
  }

  private class Repeater(runnable: Runnable, initialDelay: FiniteDuration, interval: FiniteDuration) extends Cancellable {
    var cancelled = false

    var event = discreteEventScheduler.doIn(initialDelay.length, () => runEvent(), "Akka schedule first execution")

    def runEvent() {
      runnable.run()
      event = discreteEventScheduler.doIn(interval.length, () => runEvent(), "Akka schedule repeated execution")
    }

    def cancel() = {
      discreteEventScheduler.cancel(event)
      cancelled = true
      true
    }

    override def isCancelled = cancelled
  }
}