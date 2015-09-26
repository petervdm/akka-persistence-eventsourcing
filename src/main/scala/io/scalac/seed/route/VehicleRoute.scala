package io.scalac.seed.route

import akka.actor._
import io.scalac.seed.domain.VehicleAggregate
import io.scalac.seed.service._
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import spray.httpx.Json4sSupport
import spray.routing._
import spray.routing.authentication.BasicAuth

object VehicleRoute {
  case class UpdateVehicleRegNumber(value: String, dateFrom: String)
  case class UpdateVehicleKeeper(value: String, dateFrom: String)
}

trait VehicleRoute extends HttpService with Json4sSupport with RequestHandlerCreator with UserAuthenticator {

  import VehicleRoute._

  import VehicleAggregateManager._

  val vehicleAggregateManager: ActorRef

//  val formatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
//  valDateTime dt = formatter.parseDateTime(string);

  val vehicleRoute =
    path("vehicles" / Segment / "vrn" ) { id =>
      post {
//        authenticate(BasicAuth(userAuthenticator _, realm = "secure site")) { user =>
          entity(as[UpdateVehicleRegNumber]) { cmd =>
            serveUpdate(UpdateRegNumber(id, cmd.value, cmd.dateFrom))
          }
        }
//      }
    } ~
    path("vehicles" / Segment / "keeper" ) { id =>
      post {
//        authenticate(BasicAuth(userAuthenticator _, realm = "secure site")) { user =>
          entity(as[UpdateVehicleKeeper]) { cmd =>
            serveUpdate(UpdateKeeper(id, cmd.value, cmd.dateFrom))
//            serveUpdate(UpdateKeeper(id, cmd.value, formatter.parseDateTime(cmd.dateFrom)))
          }
        }
//      }
    } ~
    path("vehicles" / Segment ) { id =>
      get {
        serveGet(GetVehicle(id))
      } ~
      delete {
        authenticate(BasicAuth(userAuthenticator _, realm = "secure site")) { user =>
          serveDelete(DeleteVehicle(id))
        }
      }
    } ~
    path("vehicles") {
    //  authenticate(BasicAuth(userAuthenticator _, realm = "secure site")) { user =>
        post {
          entity(as[RegisterVehicle]) { cmd =>
            serveRegister(cmd)
          }
        }
   //   }
    }

  private def serveUpdate(message : AggregateManager.Command): Route =
    ctx => handleUpdate[VehicleAggregate.Vehicle](ctx, vehicleAggregateManager, message)

  private def serveRegister(message : AggregateManager.Command): Route =
    ctx => handleRegister[VehicleAggregate.Vehicle](ctx, vehicleAggregateManager, message)

  private def serveDelete(message : AggregateManager.Command): Route =
    ctx => handleDelete(ctx, vehicleAggregateManager, message)

  private def serveGet(message : AggregateManager.Command): Route =
    ctx => handleGet[VehicleAggregate.Vehicle](ctx, vehicleAggregateManager, message)

}