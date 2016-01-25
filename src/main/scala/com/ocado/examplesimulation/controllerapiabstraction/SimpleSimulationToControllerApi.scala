package com.ocado.examplesimulation.controllerapiabstraction

import com.ocado.examplecontroller.SimpleController

class SimpleSimulationToControllerApi(controller: SimpleController) extends SimulationToControllerApi {
  override def receive(obj: Any): Unit = controller.receive(obj)

  /**
    * Nothing required to shut down a "simple" controller
    */
  override def shutdown(): Unit = {}
}
