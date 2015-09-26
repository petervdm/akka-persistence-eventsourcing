package io.scalac.seed.service

import akka.actor._
import io.scalac.seed.domain._
import java.util.UUID

object VehicleAggregateManager {

  import AggregateManager._

  case class RegisterVehicle(regNumber: String) extends Command
  case class GetVehicle(id: String) extends Command
  case class UpdateRegNumber(id: String, regNumber: String, dateFrom: String) extends Command
  case class UpdateKeeper(id: String, keeperUri: String, dateFrom: String) extends Command
  case class DeleteVehicle(id: String) extends Command
  
  def props: Props = Props(new VehicleAggregateManager)
}

class VehicleAggregateManager extends AggregateManager {

  import AggregateRoot._
  import VehicleAggregateManager._
  import VehicleAggregate._

  def processCommand = {
    case RegisterVehicle(rn) =>
      val id = UUID.randomUUID().toString
      processAggregateCommand(id, Initialize(rn))
    case GetVehicle(id) =>
      processAggregateCommand(id, GetState)
    case UpdateRegNumber(id, regNumber, dateFrom) =>
      processAggregateCommand(id, AssignVrn(regNumber, dateFrom))
    case UpdateKeeper(id, keeperUri, dateFrom) =>
      processAggregateCommand(id, AssignKeeper(keeperUri, dateFrom))
    case DeleteVehicle(id) =>
      processAggregateCommand(id, Remove)
  }

  override def aggregateProps(id: String) = VehicleAggregate.props(id)
}