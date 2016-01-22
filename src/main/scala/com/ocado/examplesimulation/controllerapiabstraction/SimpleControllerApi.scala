package com.ocado.examplesimulation.controllerapiabstraction

import com.ocado.examplecontroller.SimpleController

class SimpleControllerApi(controller: SimpleController) extends ControllerApi {
  override def receive(obj: Any): Unit = controller.receive(obj)

  /**
    * Nothing required to shut down a "simple" controller
    */
  override def shutdown(): Unit = {}
}
