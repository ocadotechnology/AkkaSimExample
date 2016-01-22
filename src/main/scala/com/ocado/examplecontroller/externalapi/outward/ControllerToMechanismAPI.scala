package com.ocado.examplecontroller.externalapi.outward

import com.ocado.examplesimulation.Simulation

class ControllerToMechanismAPI {
  var simulation: Simulation = null
  def setSimulation(simulation: Simulation) = this.simulation = simulation

  def moveTo(obj: Int, destination: Integer) = {
    simulation.moveTo(obj, destination)
  }
}
