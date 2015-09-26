package io.scalac.seed

import java.nio.ByteBuffer
import java.nio.charset.Charset

import akka.actor.{ActorRef, ExtendedActorSystem}
import akka.persistence.eventstore.EventStoreSerializer
import akka.persistence.eventstore.snapshot.EventStoreSnapshotStore.SnapshotEvent
import akka.persistence.eventstore.snapshot.EventStoreSnapshotStore.SnapshotEvent.Snapshot
import akka.persistence.{PersistentRepr, SnapshotMetadata}
import akka.util.ByteString
import eventstore.{Content, ContentType, Event, EventData}
import io.scalac.seed.domain.VehicleAggregate.{KeeperAssigned, VehicleInitialized, VrnAssigned}
import org.json4s.Extraction.decompose
import org.json4s._
import org.json4s.native.Serialization.{read, write}

class Json4sSerializer(val system: ExtendedActorSystem) extends EventStoreSerializer {
  import Json4sSerializer._

  implicit val formats = DefaultFormats + SnapshotSerializer + PersistentReprSerializer + ActorRefSerializer

  def identifier = Identifier

  def includeManifest = true

  def fromBinary(bytes: Array[Byte], manifestOpt: Option[Class[_]]) = {
    implicit val manifest = manifestOpt match {
      case Some(x) => Manifest.classType(x)
      case None    => Manifest.AnyRef
    }

    read(new String(bytes, UTF8))
  }

  def toBinary(o: AnyRef) = write(o).getBytes(UTF8)

  def toEvent(x: AnyRef) = x match {
    case x: PersistentRepr =>
      EventData(
      eventType = eventType(x.payload),
      data = Content(ByteString(toBinary(x)), ContentType.Json))

    case x: SnapshotEvent => EventData(
      eventType = classFor(x).getName,
      data = Content(ByteString(toBinary(x)), ContentType.Json))

    case _ => sys.error(s"Cannot serialize $x, SnapshotEvent expected")
  }

  def fromEvent(event: Event, manifest: Class[_]) = {
    val clazz = classOf[PersistentRepr]

    val result = fromBinary(event.data.data.value.toArray, clazz)
    if (manifest.isInstance(result)) result
    else sys.error(s"Cannot deserialize event as $manifest, event: $event")
  }

  def classFor(x: AnyRef) = x match {
    case x: PersistentRepr => classOf[PersistentRepr]
    case _                 => x.getClass
  }

  def eventType(x: Any) = x match {
    case x: VehicleInitialized => classOf[VehicleInitialized].getName
    case x: VrnAssigned => classOf[VrnAssigned].getName
    case x: KeeperAssigned => classOf[KeeperAssigned].getName
  }

  object ActorRefSerializer extends Serializer[ActorRef] {
    val Clazz = classOf[ActorRef]

    def deserialize(implicit format: Formats) = {
      case (TypeInfo(Clazz, _), JString(x)) => system.provider.resolveActorRef(x)
    }

    def serialize(implicit format: Formats) = {
      case x: ActorRef => JString(x.path.toSerializationFormat)
    }
  }
}

object Json4sSerializer {
  val UTF8: Charset = Charset.forName("UTF-8")
  val Identifier: Int = ByteBuffer.wrap("json4s".getBytes(UTF8)).getInt

  object SnapshotSerializer extends Serializer[Snapshot] {
    val Clazz = classOf[Snapshot]

    def deserialize(implicit format: Formats) = {
      case (TypeInfo(Clazz, _), JObject(List(
      JField("data", JString(x)),
      JField("metadata", metadata)))) => Snapshot(x, metadata.extract[SnapshotMetadata])
    }

    def serialize(implicit format: Formats) = {
      case Snapshot(data, metadata) => JObject("data" -> JString(data.toString), "metadata" -> decompose(metadata))
    }
  }

  object PersistentReprSerializer extends Serializer[PersistentRepr] {
    val Clazz = classOf[PersistentRepr]

    def deserialize(implicit format: Formats) = {
      case (TypeInfo(Clazz, _), json) =>

        val x = json.extract[Mapping]

        x.eventType match {
          case "io.scalac.seed.domain.VehicleAggregate$VehicleInitialized" =>
            val y = json.extract [VehicleInitialisedMapping]
            PersistentRepr(y.payload, y.sequenceNr, y.persistenceId, y.deleted, sender = y.sender)
          case "io.scalac.seed.domain.VehicleAggregate$VrnAssigned" =>
            val y = json.extract [VrnAssignedMapping]
            PersistentRepr(y.payload, y.sequenceNr, y.persistenceId, y.deleted, sender = y.sender)
          case "io.scalac.seed.domain.VehicleAggregate$KeeperAssigned" =>
            val y = json.extract [KeeperAssignedMapping]
            PersistentRepr(y.payload, y.sequenceNr, y.persistenceId, y.deleted, sender = y.sender)
        }
    }

    def serialize(implicit format: Formats) = {
      case x: PersistentRepr =>
        x.payload match {
          case y: VehicleInitialized =>
            val mapping = VehicleInitialisedMapping(y, x.sequenceNr, x.persistenceId, x.deleted, x.sender, x.payload.getClass.getName)
            decompose (mapping)
          case y: VrnAssigned =>
            val mapping = VrnAssignedMapping(y, x.sequenceNr, x.persistenceId, x.deleted, x.sender, x.payload.getClass.getName)
            decompose (mapping)
          case y: KeeperAssigned =>
            val mapping = KeeperAssignedMapping(y, x.sequenceNr, x.persistenceId, x.deleted, x.sender, x.payload.getClass.getName)
            decompose (mapping)
        }
    }

    case class Mapping(payload: Any, sequenceNr: Long, persistenceId: String, deleted: Boolean, sender: ActorRef, eventType: String)
    case class VehicleInitialisedMapping(payload: VehicleInitialized, sequenceNr: Long, persistenceId: String, deleted: Boolean, sender: ActorRef, eventType: String)
    case class VrnAssignedMapping(payload: VrnAssigned, sequenceNr: Long, persistenceId: String, deleted: Boolean, sender: ActorRef, eventType: String)
    case class KeeperAssignedMapping(payload: KeeperAssigned, sequenceNr: Long, persistenceId: String, deleted: Boolean, sender: ActorRef, eventType: String)
  }
}