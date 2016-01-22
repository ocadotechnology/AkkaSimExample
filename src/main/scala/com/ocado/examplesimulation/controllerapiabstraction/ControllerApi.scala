package com.ocado.examplesimulation.controllerapiabstraction

trait ControllerApi {
  def receive(obj: Any)
  def shutdown()
}
