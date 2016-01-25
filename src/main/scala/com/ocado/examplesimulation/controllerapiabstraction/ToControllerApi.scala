package com.ocado.examplesimulation.controllerapiabstraction

trait ToControllerApi {
  def receive(obj: Any)
  def shutdown()
}
