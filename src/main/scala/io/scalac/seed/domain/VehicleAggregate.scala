package io.scalac.seed.domain

import akka.actor._
import akka.persistence._

object VehicleAggregate {

  import AggregateRoot._

  case class Vehicle(id: String, keeperList: List[Keeper], vrnList: List[Vrn]) extends State
  case class Keeper(keeperUri: String, dateFrom: String, dateTo: String)
  case class Vrn(vrn: String, dateFrom: String, dateTo: String)

  case class Initialize(vrn: String) extends Command
  case class AssignVrn(newVrn: String, dateFrom: String) extends Command
  case class AssignKeeper(newKeeperUri: String, dateFrom: String) extends Command

  case class VehicleInitialized(vrn: String) extends Event
  case class VrnAssigned(vrn: String, dateFrom: String) extends Event
  case class KeeperAssigned(keeperUri: String, dateFrom: String) extends Event
  case object VehicleRemoved extends Event

  def props(id: String): Props = Props(new VehicleAggregate(id))
}

class VehicleAggregate(id: String) extends AggregateRoot {

  import AggregateRoot._
  import VehicleAggregate._

  override def persistenceId = "vehicle-" + id

  override def updateState(evt: Event): Unit = evt match {

    case VehicleInitialized(reg) =>
      context.become(created)
      state = Vehicle(id, keeperList = List(), vrnList = List())

    case VrnAssigned(reg, dateFrom) => state match {
      case s: Vehicle =>
        val newVrnList = s.vrnList ::: List(Vrn(reg, dateFrom, null))
        state = s.copy(vrnList = newVrnList)
      case _ => //nothing
    }

    case KeeperAssigned(keeperUri, dateFrom) => state match {
      case s: Vehicle =>
        val newKeeperList = s.keeperList ::: List(Keeper(keeperUri, dateFrom, null))
        state = s.copy(keeperList = newKeeperList)
      case _ => //nothing
    }

    case VehicleRemoved =>
      context.become(removed)
      state = Removed

    case _ =>
      println("Unknown message: ")
  }

  val initial: Receive = {
    case Initialize(reg) =>
      persist(VehicleInitialized(reg))(afterEventPersisted)
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
    case PersistenceFailure(payload, sequence, cause) =>
      println(cause.getMessage)
      println(cause.printStackTrace())
  }
  
  val created: Receive = {
    case AssignVrn(reg, dateFrom) =>
      persist(VrnAssigned(reg, dateFrom))(afterEventPersisted)
    case AssignKeeper(keeperUri, dateFrom) =>
      persist(KeeperAssigned(keeperUri, dateFrom))(afterEventPersisted)
    case Remove =>
      persist(VehicleRemoved)(afterEventPersisted)
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
  }
  
  val removed: Receive = {
    case GetState =>
      respond()
    case KillAggregate =>
      context.stop(self)
  }

  val receiveCommand: Receive = initial

  override def restoreFromSnapshot(metadata: SnapshotMetadata, state: State) = {
    this.state = state
    state match {
      case Uninitialized => context become initial
      case Removed => context become removed
      case _: Vehicle => context become created
    }
  }

}