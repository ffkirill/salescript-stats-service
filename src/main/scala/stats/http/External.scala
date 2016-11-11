package stats.http

import akka.actor.ActorSystem

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.Cookie
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.unmarshalling.Unmarshal

import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol

import stats.utils.Config

import scala.concurrent.{ExecutionContext, Future}

case class UserCredential(id: Int, username: String, email: String)

trait BackendApiProtocols extends DefaultJsonProtocol {
  implicit val userCredentialFormat = jsonFormat3(UserCredential.apply)
}


object External extends BackendApiProtocols with Config {
  def fetchUserCredential(sessionId: String)
                         (implicit system: ActorSystem,
                          fm: ActorMaterializer,
                          ec: ExecutionContext): Future[Either[String, UserCredential]] = {
    Http().singleRequest(
      RequestBuilding.Get("http://127.0.0.1:8000/api/current_user")
        .addHeader(Cookie("sessionid" -> sessionId))
        .withEntity(ContentTypes.`application/json`, "")) flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[UserCredential].map(Right(_))
        case _ => Unmarshal(response.entity).to[String].map(Left(_))
      }}
  }
}
