package com.ocado.examplesimulation

import com.ocado.examplesimulation.controllerapiabstraction.SimulationToControllerApi

class Terminator {
  var simulationToControllerApi: SimulationToControllerApi = null

  def setController(simulationToControllerApi: SimulationToControllerApi) = this.simulationToControllerApi = simulationToControllerApi

  def shutdown() = simulationToControllerApi.shutdown()
}
