package com.ocado.examplesimulation.controllerapiabstraction

trait SimulationToControllerApi {
  def receive(obj: Any)
  def shutdown()
}
