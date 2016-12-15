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
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import stats.models.User
import stats.utils.Config

import scala.concurrent.{ExecutionContext, Future}

trait BackendApiProtocols extends DefaultJsonProtocol {
  implicit val userCredentialFormat: RootJsonFormat[User] = jsonFormat3(User.apply)
}


class ExternalService()(implicit system: ActorSystem,
                      fm: ActorMaterializer,
                      ec: ExecutionContext) extends BackendApiProtocols with Config {
  def fetchUserCredential(sessionId: String)
                         : Future[Either[String, User]] = {
    Http().singleRequest(
      RequestBuilding
        .Get(s"$backendApiEndpoint/api/current_user")
        .addHeader(Cookie("sessionid" -> sessionId))
        .withEntity(ContentTypes.`application/json`, "")) flatMap { response =>
      response.status match {
        case OK => Unmarshal(response.entity).to[User].map(Right(_))
        case _ => Unmarshal(response.entity).to[String].map(Left(_))
      }}
  }
}

object ExternalService {
  def apply()(implicit system: ActorSystem,
              fm: ActorMaterializer,
              ec: ExecutionContext): ExternalService = new ExternalService()(system, fm, ec)
}
