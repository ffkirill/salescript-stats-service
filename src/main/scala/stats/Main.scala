package stats

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext
import http.HttpService
import http.WsService
import stats.utils.{Config, FlywayService, DatabaseService}
import stats.services.EventsService

object Main extends App with Config {
  implicit val actorSystem = ActorSystem()
  implicit val executor: ExecutionContext = actorSystem.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = Logging(actorSystem, getClass)

  val flywayService = FlywayService(jdbcUrl, dbUser, dbPassword)
  flywayService.migrateDatabaseSchema()

  val dbService = DatabaseService(jdbcUrl, dbUser, dbPassword)
  val eventsService = EventsService(dbService)

  val wsService = WsService()
  val httpService = HttpService(wsService.createProcessor(eventsService))
  Http().bindAndHandle(httpService.route, httpHost, httpPort)

}