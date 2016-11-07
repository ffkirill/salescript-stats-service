package stats

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

import http.HttpService
import http.WsService

object Main extends App {
  implicit val actorSystem = ActorSystem()
  implicit val executor: ExecutionContext = actorSystem.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val wsService = WsService()
  val httpService = HttpService(wsService.createProcessor)
  Http().bindAndHandle(httpService.route, "127.0.0.1", 8081)
}