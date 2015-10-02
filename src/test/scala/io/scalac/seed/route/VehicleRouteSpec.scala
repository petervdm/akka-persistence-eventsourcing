package io.scalac.seed.route

import akka.pattern.ask
import akka.util.Timeout
import io.scalac.seed.domain.AggregateRoot.Removed
import io.scalac.seed.domain.VehicleAggregate
import io.scalac.seed.service.{UserAggregateManager, VehicleAggregateManager}
import VehicleAggregate.Vehicle
import VehicleAggregateManager.{GetVehicle, RegisterVehicle}
import io.scalac.seed.service.UserAggregateManager.RegisterUser
import java.util.UUID
import org.json4s.{DefaultFormats, JObject}
import org.scalatest.{BeforeAndAfterAll, Matchers, FlatSpec}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import spray.http.{BasicHttpCredentials, StatusCodes}
import spray.testkit.ScalatestRouteTest

class VehicleRouteSpec extends FlatSpec with ScalatestRouteTest with Matchers with VehicleRoute with BeforeAndAfterAll {

  implicit val json4sFormats = DefaultFormats

  implicit val timeout = Timeout(2.seconds)

  implicit def executionContext = system.dispatcher
  
  def actorRefFactory = system

  val vehicleAggregateManager = system.actorOf(VehicleAggregateManager.props)

  val userAggregateManager = system.actorOf(UserAggregateManager.props)

  implicit val routeTestTimeout = RouteTestTimeout(5.seconds)

  val credentials = BasicHttpCredentials("test", "test")

  override def beforeAll: Unit = {
    val userFuture = userAggregateManager ? RegisterUser("test", "test")
    Await.result(userFuture, 5 seconds)
  }

  "VehicleRoute" should "return not found if non-existing vehicle is requested" in {
    Get("/vehicles/" + UUID.randomUUID().toString) ~> vehicleRoute ~> check {
      response.status shouldBe StatusCodes.NotFound
    }
  }

  it should "create a vehicle" in {
    val regNumber = "123"
    Post("/vehicles", Map("regNumber" -> regNumber)) ~> addCredentials(credentials) ~> vehicleRoute ~> check {
      response.status shouldBe StatusCodes.Created
      val id = (responseAs[JObject] \ "id").extract[String]
      val vehicle = getVehicleFromManager(id)
//      vehicle.vrn shouldEqual regNumber
    }
  }

  it should "return existing vehicle" in {
    val regNumber = "456"
    val vehicle = createVehicleInManager(regNumber)
    Get(s"/vehicles/" + vehicle.id) ~> vehicleRoute ~> check {
      response.status shouldBe StatusCodes.OK
      val responseJson = responseAs[JObject]
println(responseJson)
//      (responseJson \ "vrnList").extract[String] shouldEqual regNumber
    }
  }

//  it should "update vehicle's regNumber" in {
//    val vehicle = createVehicleInManager("123")
//    val newRegNumber = "456"
//    Post(s"/vehicles/${vehicle.id}/regnumber", Map("value" -> newRegNumber)) ~> vehicleRoute ~> check {
////      Post(s"/vehicles/${vehicle.id}/regnumber", Map("value" -> newRegNumber)) ~> addCredentials(credentials) ~> vehicleRoute ~> check {
//      response.status shouldBe StatusCodes.OK
//      val updatedVehicle = getVehicleFromManager(vehicle.id)
////      updatedVehicle.vrn shouldEqual newRegNumber
//    }
//  }

  private def getVehicleFromManager(id: String) = {
    val vehicleFuture = (vehicleAggregateManager ? GetVehicle(id)).mapTo[Vehicle]
    Await.result(vehicleFuture, 2.seconds)
  }

  private def createVehicleInManager(regNumber: String) = {
    val vehicleFuture = (vehicleAggregateManager ? RegisterVehicle(regNumber)).mapTo[Vehicle]
    Await.result(vehicleFuture, 2.seconds)
  }

}