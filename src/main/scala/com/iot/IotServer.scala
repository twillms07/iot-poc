package com.iot

import akka.actor
import akka.actor.Scheduler
import akka.actor.typed.{ActorSystem, Behavior}
import akka.event.{Logging, LoggingAdapter}

import scala.io.StdIn
import scala.util.control.NonFatal
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import com.iot.IoTSupervisor.{IotCommand, IotSupervisorRequest, IotSupervisorResponse}
import com.iot.utils.JsonSupport
import util.{Success, Failure}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class IotServer

object IotServer extends App
    with Routes {

    import akka.actor.typed.scaladsl.adapter._
    val actorSystemTyped = ActorSystem[IotCommand](IoTSupervisor(),"IoTServer-A")


    implicit val executionContext: ExecutionContext = actorSystemTyped.executionContext
    implicit val actorSystem: actor.ActorSystem = actorSystemTyped.toUntyped
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val timeout: Timeout = 3 seconds
    implicit val scheduler: Scheduler = actorSystem.scheduler

    val logger: LoggingAdapter = Logging(actorSystem, classOf[IotServer])
    val interface = "0.0.0.0"
    val port = 8080

    Http().bindAndHandle(routes, interface = interface, port = port)


    logger.debug("Iot System up")

    try {
        println(">>> Press ENTER to exit <<<")
        StdIn.readLine()
    } catch {
        case NonFatal(e) => actorSystemTyped.terminate()
    }finally {
        actorSystemTyped.terminate()
    }

}

trait Routes extends JsonSupport {

    implicit val scheduler: Scheduler
    implicit val timeout: Timeout
    implicit val executionContext: ExecutionContext

    val actorSystemTyped:ActorSystem[IotCommand]
    val logger: LoggingAdapter

    val getAllMeasurements: Route = path(pm = "iot" / "measurements") {
        get {
            val resultMaybe: Future[IotSupervisorResponse] = actorSystemTyped.ask(ref ⇒ IotSupervisorRequest(ref))
            onComplete(resultMaybe) {
                case Success(result) ⇒
                    complete(StatusCodes.OK, result)
                case Failure(exception) ⇒
                    complete(StatusCodes.InternalServerError)
            }
        }
    }

    val registerDevice: Route = path(pm = "iot" / "group" / Segment / "device" / Segment) { (groupId,deviceId) ⇒
        post {
            ???
        }
    }

    val routes: Route = getAllMeasurements
}
